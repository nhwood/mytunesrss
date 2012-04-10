package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.LuceneTrackService
 */
public class LuceneTrackService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneTrackService.class);

    private static final int MAX_RESULTS = 10000;

    private Directory getDirectory() throws IOException {
        return FSDirectory.open(new File(MyTunesRss.CACHE_DATA_PATH + "/lucene/track"));
    }

    public void indexAllTracks() throws IOException, SQLException {
        IndexWriter iwriter = null;
        Directory directory = null;
        DataStoreSession session = null;
        try {
            LOGGER.debug("Indexing all tracks.");
            long start = System.currentTimeMillis();
            directory = getDirectory();
            Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
            iwriter = new IndexWriter(directory, analyzer, true, new IndexWriter.MaxFieldLength(300));
            session = MyTunesRss.STORE.getTransaction();
            final Map<String, List<String>> trackTagMap = new HashMap<String, List<String>>();
            session.executeQuery(new DataStoreQuery<Object>() {
                @Override
                public Object execute(Connection connection) throws SQLException {
                    execute(MyTunesRssUtils.createStatement(connection, "getTrackTagMap"), new ResultBuilder<Object>() {
                        public Object create(ResultSet resultSet) throws SQLException {
                            List<String> tags = trackTagMap.get(resultSet.getString(1));
                            if (tags == null) {
                                tags = new ArrayList<String>();
                                trackTagMap.put(resultSet.getString(1), tags);
                            }
                            tags.add(resultSet.getString(2));
                            return null;
                        }
                    }).getResults(); // call #getResults() just to make result builder run
                    return null;
                }
            });
            FindPlaylistTracksQuery query = new FindPlaylistTracksQuery(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM, SortOrder.KeepOrder);
            query.setResultSetType(ResultSetType.TYPE_FORWARD_ONLY);
            query.setFetchSize(10000);
            DataStoreQuery.QueryResult<Track> queryResult = session.executeQuery(query);
            for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
                if (track.getMediaType() != MediaType.Image) {
                    Document document = createTrackDocument(track, trackTagMap);
                    iwriter.addDocument(document);
                }
            }
            LOGGER.debug("Finished indexing all tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
        } finally {
            if (iwriter != null) {
                iwriter.close();
            }
            if (directory != null) {
                directory.close();
            }
            if (session != null) {
                session.rollback();
            }
        }
    }

    /**
     * Create a document for a track.
     *
     * @param track       A track to create a document from.
     * @param trackTagMap Track to tag mapping.
     * @return Document for the track.
     */
    private Document createTrackDocument(Track track, Map<String, List<String>> trackTagMap) {
        Document document = new Document();
        document.add(new Field("id", track.getId(), Field.Store.YES, Field.Index.NO));
        document.add(new Field("name", StringUtils.lowerCase(track.getName()), Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("album", StringUtils.lowerCase(track.getAlbum()), Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("artist", StringUtils.lowerCase(track.getArtist()), Field.Store.NO, Field.Index.ANALYZED));
        if (StringUtils.isNotBlank(track.getSeries())) {
            document.add(new Field("series", StringUtils.lowerCase(track.getSeries()), Field.Store.NO, Field.Index.ANALYZED));
        }
        document.add(new Field("filename", StringUtils.lowerCase(track.getFilename()), Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (StringUtils.isNotBlank(track.getComment())) {
            document.add(new Field("comment", StringUtils.lowerCase(track.getComment()), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (StringUtils.isNotBlank(track.getAlbumArtist())) {
            document.add(new Field("album_artist", StringUtils.lowerCase(track.getAlbumArtist()), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (StringUtils.isNotBlank(track.getGenre())) {
            document.add(new Field("genre", StringUtils.lowerCase(track.getGenre()), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (StringUtils.isNotBlank(track.getComposer())) {
            document.add(new Field("composer", StringUtils.lowerCase(track.getComposer()), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (trackTagMap.get(track.getId()) != null) {
            document.add(new Field("tags", StringUtils.lowerCase(StringUtils.join(trackTagMap.get(track.getId()), " ")), Field.Store.NO, Field.Index.ANALYZED));
        }
        return document;
    }

    public void updateTracks(String[] trackIds) throws IOException, SQLException {
        DataStoreSession session = null;
        Directory directory = null;
        IndexWriter iwriter = null;
        try {
            LOGGER.debug("Indexing " + trackIds.length + " tracks.");
            long start = System.currentTimeMillis();
            directory = getDirectory();
            Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
            iwriter = new IndexWriter(directory, analyzer, false, new IndexWriter.MaxFieldLength(300));
            final Map<String, List<String>> trackTagMap = new HashMap<String, List<String>>();
            session = MyTunesRss.STORE.getTransaction();
            session.executeQuery(new DataStoreQuery<Object>() {
                @Override
                public Object execute(Connection connection) throws SQLException {
                    execute(MyTunesRssUtils.createStatement(connection, "getTrackTagMap"), new ResultBuilder<Object>() {
                        public Object create(ResultSet resultSet) throws SQLException {
                            List<String> tags = trackTagMap.get(resultSet.getString(1));
                            if (tags == null) {
                                tags = new ArrayList<String>();
                                trackTagMap.put(resultSet.getString(1), tags);
                            }
                            tags.add(resultSet.getString(2));
                            return null;
                        }
                    }).getResults(); // call #getResults() just to make result builder run
                    return null;
                }
            });
            FindTrackQuery query = FindTrackQuery.getForIds(trackIds);
            query.setResultSetType(ResultSetType.TYPE_FORWARD_ONLY);
            query.setFetchSize(10000);
            DataStoreQuery.QueryResult<Track> queryResult = session.executeQuery(query);
            Set<String> deletedTracks = new HashSet<String>(Arrays.asList(trackIds));
            for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
                if (track.getMediaType() != MediaType.Image) {
                    Document document = createTrackDocument(track, trackTagMap);
                    iwriter.updateDocument(new Term("id", track.getId()), document);
                }
                deletedTracks.remove(track.getId());
            }
            for (String deletedTrack : deletedTracks) {
                iwriter.deleteDocuments(new Term("id", deletedTrack));
            }
            LOGGER.debug("Finished indexing " + trackIds.length + " tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
        } finally {
            if (iwriter != null) {
                iwriter.close();
            }
            if (directory != null) {
                directory.close();
            }
            if (session != null) {
                session.rollback();
            }
        }
    }

    public Collection<String> searchTrackIds(String[] searchTerms, int fuzziness) throws IOException, ParseException {
        Directory directory = null;
        IndexSearcher isearcher = null;
        Collection<String> trackIds;
        try {
            directory = getDirectory();
            isearcher = new IndexSearcher(IndexReader.open(directory));
            isearcher.setDefaultFieldSortScoring(true, true);
            Query luceneQuery = createQuery(searchTerms, fuzziness);
            TopDocs topDocs = isearcher.search(luceneQuery, MAX_RESULTS);
            trackIds = new HashSet<String>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(isearcher.doc(scoreDoc.doc).get("id"));
            }
            return trackIds;
        } finally {
            if (isearcher != null) {
                isearcher.close();
            }
            if (directory != null) {
                directory.close();
            }
        }
    }

    public Collection<String> searchTrackIds(String searchExpression) throws IOException, LuceneQueryParserException {
        Directory directory = null;
        IndexSearcher isearcher = null;
        try {
            directory = getDirectory();
            isearcher = new IndexSearcher(IndexReader.open(directory));
            isearcher.setDefaultFieldSortScoring(true, true);
            Query luceneQuery = null;
            try {
                QueryParser parser = new QueryParser(Version.LUCENE_35, "name", new WhitespaceAnalyzer(Version.LUCENE_35));
                parser.setAllowLeadingWildcard(true);
                luceneQuery = parser.parse(searchExpression);
            } catch (ParseException e) {
                throw new LuceneQueryParserException("Could not parse query string.", e);
            } catch (Exception e) {
                throw new LuceneQueryParserException("Could not parse query string.", e);
            }
            TopDocs topDocs = isearcher.search(luceneQuery, MAX_RESULTS);
            Collection<String> trackIds = new HashSet<String>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(isearcher.doc(scoreDoc.doc).get("id"));
            }
            return trackIds;
        } finally {
            if (isearcher != null) {
                isearcher.close();
            }
            if (directory != null) {
                directory.close();
            }
        }
    }

    private Query createQuery(String[] searchTerms, int fuzziness) {
        BooleanQuery orQuery = new BooleanQuery();
        for (String searchTerm : searchTerms) {
            for (String field : new String[]{"name", "album", "artist", "series", "comment", "tags", "album_artist", "composer"}) {
                String escapedSearchTerm = QueryParser.escape(searchTerm);
                Query query = new WildcardQuery(new Term(field, escapedSearchTerm));
                query.setBoost(100f);
                orQuery.add(query, BooleanClause.Occur.SHOULD);
                query = new WildcardQuery(new Term(field, "*" + escapedSearchTerm + "*"));
                query.setBoost(50f);
                orQuery.add(query, BooleanClause.Occur.SHOULD);
                if (fuzziness > 0) {
                    orQuery.add(new FuzzyQuery(new Term(field, escapedSearchTerm), ((float) (100 - fuzziness)) / 100f), BooleanClause.Occur.SHOULD);
                }
            }
        }
        LOGGER.debug("QUERY for \"" + StringUtils.join(searchTerms, " ") + "\" (" + fuzziness + "): " + orQuery);
        return orQuery;
    }

    public Collection<String> searchTrackIds(Collection<SmartInfo> smartInfos, int fuzziness) throws IOException, ParseException {
        Directory directory = null;
        IndexSearcher isearcher = null;
        Collection<String> trackIds;
        try {
            directory = getDirectory();
            isearcher = new IndexSearcher(IndexReader.open(directory));
            Query luceneQuery = createQuery(smartInfos, fuzziness);
            TopDocs topDocs = isearcher.search(luceneQuery, MAX_RESULTS);
            trackIds = new HashSet<String>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(isearcher.doc(scoreDoc.doc).get("id"));
            }
            return trackIds;
        } finally {
            if (isearcher != null) {
                isearcher.close();
            }
            if (directory != null) {
                directory.close();
            }
        }
    }

    private Query createQuery(Collection<SmartInfo> smartInfos, int fuzziness) {
        BooleanQuery andQuery = new BooleanQuery();
        for (SmartInfo smartInfo : smartInfos) {
            switch (smartInfo.getFieldType()) {
                case album:
                    addToAndQuery(andQuery, "album", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case artist:
                    addToAndQuery(andQuery, "artist", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    addToAndQuery(andQuery, "album_artist", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case comment:
                    addToAndQuery(andQuery, "comment", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case composer:
                    addToAndQuery(andQuery, "composer", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case file:
                    addToAndQuery(andQuery, "filename", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case genre:
                    addToAndQuery(andQuery, "genre", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case tag:
                    addToAndQuery(andQuery, "tags", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case title:
                    addToAndQuery(andQuery, "name", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case tvshow:
                    addToAndQuery(andQuery, "series", StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
            }
        }
        return andQuery;
    }

    private void addToAndQuery(BooleanQuery andQuery, String field, String pattern, int fuzziness) {
        if (StringUtils.isNotEmpty(pattern)) {
            BooleanQuery innerAndQuery = new BooleanQuery();
            String escapedPattern = QueryParser.escape(pattern);
            for (String term : StringUtils.split(escapedPattern)) {
                if (fuzziness > 0) {
                    innerAndQuery.add(new FuzzyQuery(new Term(field, term), ((float) (100 - fuzziness)) / 100f), BooleanClause.Occur.MUST);
                } else {
                    innerAndQuery.add(new WildcardQuery(new Term(field, "*" + term + "*")), BooleanClause.Occur.MUST);
                }
            }
            andQuery.add(innerAndQuery, BooleanClause.Occur.MUST);
        }
    }
}
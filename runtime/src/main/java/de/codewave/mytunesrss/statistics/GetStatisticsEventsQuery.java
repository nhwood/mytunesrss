package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class GetStatisticsEventsQuery extends DataStoreQuery<DataStoreQuery.QueryResult<StatisticsEvent>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetStatisticsEventsQuery.class);

    private long myFrom;
    private long myTo;
    private StatEventType[] myTypes;

    public GetStatisticsEventsQuery(long from, long to) {
        myFrom = from;
        myTo = to;
    }

    public GetStatisticsEventsQuery(long from, long to, StatEventType... types) {
        myFrom = from;
        myTo = to;
        myTypes = types;
    }

    @Override
    public QueryResult<StatisticsEvent> execute(Connection connection) throws SQLException {
        boolean types = myTypes != null && myTypes.length > 0;
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getStatisticEvents", Collections.singletonMap("types", types));
        statement.setLong("ts_from", myFrom);
        statement.setLong("ts_to", myTo);
        statement.setItems("type", myTypes);
        final ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
        return execute(statement, new ResultBuilder<StatisticsEvent>() {
            public StatisticsEvent create(ResultSet resultSet) throws SQLException {
                try {
                    AbstractEvent event = (AbstractEvent) mapper.readValue(resultSet.getString("data"), StatEventType.getEventClass(resultSet.getInt("type")));
                    event.setEventTime(resultSet.getLong("ts"));
                } catch (IOException e) {
                    LOGGER.warn("Could not reconstruct event from database.", e);
                }
                return NullEvent.INSTANCE;
            }
        });
    }
}
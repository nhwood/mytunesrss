package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public abstract class InsertOrUpdateImageStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(InsertOrUpdateImageStatement.class);

    private String myTrackId;
    private int mySize;
    private byte[] myData;
    private SmartStatement myStatement;

    public InsertOrUpdateImageStatement(String trackId, int size, byte[] data) {
        myTrackId = trackId;
        mySize = size;
        myData = data;
        if (LOG.isDebugEnabled()) {
            if (data != null) {
                LOG.debug("Image data size is " + data.length + " bytes.");
            } else {
                LOG.debug("Image data is NULL.");
            }
        }
    }

    public void setData(byte[] data) {
        myData = data;
    }

    public void setSize(int size) {
        mySize = size;
    }

    public void setTrackId(String trackId) {
        myTrackId = trackId;
    }

    public synchronized void execute(Connection connection) throws SQLException {
        if (myData != null) {
            try {
                if (myStatement == null) {
                    myStatement = MyTunesRssUtils.createStatement(connection, getStatementName());
                }
                myStatement.clearParameters();
                myStatement.setString("track_id", myTrackId);
                myStatement.setInt("size", mySize);
                myStatement.setObject("data", myData);
                myStatement.execute();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(String.format("Could not update image for track with ID \"%s\" in database.", myTrackId), e);
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping image insert/update for id \"" + myTrackId + "\" because image data is NULL.");
            }
        }
    }

    protected abstract String getStatementName();

    public void clear() {
        myTrackId = null;
        mySize = 0;
        myData = null;
    }
}
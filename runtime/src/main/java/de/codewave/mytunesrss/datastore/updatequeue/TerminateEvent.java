package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class TerminateEvent extends CheckpointEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateEvent.class);

    public boolean execute(DataStoreSession session) {
        try {
            session.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    MyTunesRssUtils.createStatement(connection, "finishDatabaseUpdate").execute();
                }
            });
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        }
        super.execute(session);
        session.commit();
        return false;
    }

    public boolean isTerminate() {
        return true;
    }
}
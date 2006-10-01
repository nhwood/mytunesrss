/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveMyTunesPlaylistStatement extends SavePlaylistStatement {
    public SaveMyTunesPlaylistStatement() {
        setType(PlaylistType.MyTunes);
    }

    public SaveMyTunesPlaylistStatement(DataStoreSession storeSession) throws SQLException {
        super(storeSession);
        setType(PlaylistType.MyTunes);
    }

    public void execute(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(myId)) {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT TOP 1 NEXT VALUE FOR playlist_id_sequence AS id FROM system_information");
            if (resultSet.next()) {
                setId("MyTunesRSS" + resultSet.getInt("ID"));
            }
            executeInsert(connection);
        } else {
            executeUpdate(connection);
        }
    }
}
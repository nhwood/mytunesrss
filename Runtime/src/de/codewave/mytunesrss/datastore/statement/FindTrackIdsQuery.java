/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackIdsQuery
 */
public class FindTrackIdsQuery extends DataStoreQuery<Collection<String>> {
    private String mySource;

    public FindTrackIdsQuery(String source) {
        mySource = source;
    }

    public Collection<String> execute(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id AS id FROM track WHERE source = ?");
        preparedStatement.setString(1, mySource);
        ResultSet resultSet = preparedStatement.executeQuery();
        Set<String> trackIds = new HashSet<String>();
        while (resultSet.next()) {
            trackIds.add(resultSet.getString("ID"));
        }
        return trackIds;
    }
}
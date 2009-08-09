/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStore;
import de.codewave.utils.sql.SmartStatementFactory;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.MyTunesRssDataStore
 */
public class MyTunesRssDataStore extends DataStore {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssDataStore.class);
    public static final int UPDATE_HELP_TABLES_FREQUENCY = 30000;

    private SmartStatementFactory mySmartStatementFactory;
    private boolean myInitialized;

    public boolean isInitialized() {
        return myInitialized;
    }

    @Override
    public void init() throws IOException, SQLException {
        final String databaseConnection = MyTunesRss.CONFIG.getDatabaseConnection();
        final String databaseUser = MyTunesRss.CONFIG.getDatabaseUser();
        final String databasePassword = MyTunesRss.CONFIG.getDatabasePassword();
        initSmartStatementFactory(MyTunesRss.CONFIG.getDatabaseType());
        LOG.info("Creating database connection pool for connect string \"" + databaseConnection + "\".");
        setConnectionPool(new GenericObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return DriverManager.getConnection(databaseConnection, databaseUser, databasePassword);
            }

            @Override
            public void destroyObject(Object object) throws Exception {
                if (object instanceof Connection) {
                    ((Connection)object).close();
                }
            }
        }, 50, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 30000, 5, 2, false, false, 15000, 10, 300000, false, 60000));
        testDatabaseConnection(databaseConnection, databaseUser, databasePassword);
        myInitialized = true;
    }

    private void testDatabaseConnection(String databaseConnection, String databaseUser, String databasePassword) throws SQLException {
//        long endTime = System.currentTimeMillis() + 10000;
//        do {
//            try {
//                Connection conn = DriverManager.getConnection(databaseConnection, databaseUser, databasePassword);
//                conn.close();
//                return; // done testing
//            } catch (SQLException e) {
//                if (LOG.isWarnEnabled()) {
//                    LOG.warn("Could not get a database connection.");
//                }
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                // intentionally left blank
//            }
//        } while (System.currentTimeMillis() < endTime);
        Connection conn = DriverManager.getConnection(databaseConnection, databaseUser, databasePassword);
        conn.close();
    }

    @Override
    protected void beforeDestroy(Connection connection) throws SQLException {
        if (MyTunesRss.CONFIG.isDefaultDatabase()) {
            connection.createStatement().execute("SHUTDOWN COMPACT");
        }
    }

    private void initSmartStatementFactory(String databaseType) {
        if (databaseType.startsWith("h2")) {
            databaseType = "h2"; // h2 and h2custom => h2 database dialect
        }
        LOG.info("Using DML/DDL for database type \"" + databaseType + "\".");
        JXPathContext[] contexts =
                new JXPathContext[] {JXPathUtils.getContext(getClass().getResource("ddl.xml")), JXPathUtils.getContext(getClass().getResource(
                        "dml.xml")), JXPathUtils.getContext(getClass().getResource("migration.xml"))};
        URL url = getClass().getResource("ddl_" + databaseType + ".xml");
        if (url != null) {
            contexts = (JXPathContext[])ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        url = getClass().getResource("dml_" + databaseType + ".xml");
        if (url != null) {
            contexts = (JXPathContext[])ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        url = getClass().getResource("migration_" + databaseType + ".xml");
        if (url != null) {
            contexts = (JXPathContext[])ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        mySmartStatementFactory = SmartStatementFactory.getInstance(contexts);
    }

    public SmartStatementFactory getSmartStatementFactory() {
        return mySmartStatementFactory;
    }
}

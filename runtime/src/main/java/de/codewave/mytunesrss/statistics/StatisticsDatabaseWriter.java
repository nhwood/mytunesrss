package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.statistics.StatisticsDatabaseWriter
 */
public class StatisticsDatabaseWriter implements StatisticsEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsDatabaseWriter.class);

    public void handleEvent(final StatisticsEvent event) {
        if (MyTunesRss.CONFIG.getStatisticKeepTime() > 0) {
            // write events only in case the keep time is greater than 0
            new Thread(new Runnable() {
                public void run() {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataStoreSession tx = MyTunesRss.STORE.getTransaction();
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(event);
                        InsertStatisticsEventStatement statisticsEventStatement = new InsertStatisticsEventStatement(baos.toByteArray());
                        tx.executeStatement(statisticsEventStatement);
                        tx.commit();
                        LOGGER.debug("Wrote statistics event \"" + event + "\" to database.");
                    } catch (IOException e) {
                        LOGGER.error("Could not write statistics event to database.", e);
                    } catch (SQLException e) {
                        LOGGER.error("Could not write statistics event to database.", e);
                    } finally {
                        tx.rollback();                    }
                }
            }, "StatisticsEventWriter").start();
        }
    }
}
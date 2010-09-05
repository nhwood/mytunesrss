package de.codewave.mytunesrss.job;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job implementation for database updates. This job simply starts the database builder task which only runs if it is not already running. The task is
 * started only if a database update is necessary according to the task.
 */
public class DatabaseUpdateJob implements Job {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseUpdateJob.class);

    /**
     * Execute the job.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            DatabaseBuilderCallable callable = new DatabaseBuilderCallable(MyTunesRss.CONFIG.isIgnoreTimestamps());
            if (callable.needsUpdate()) {
                callable.call();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not automatically update database.", e);
            }
        }
    }
}
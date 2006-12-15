package de.codewave.mytunesrss;

import de.codewave.utils.swing.*;
import de.codewave.utils.swing.pleasewait.*;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import org.apache.commons.logging.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HostConfiguration;

import javax.swing.*;
import java.text.MessageFormat;

/**
 * de.codewave.mytunesrss.MyTunesRssUtils
 */
public class MyTunesRssUtils {
    private static final Log LOG = LogFactory.getLog(MyTunesRssUtils.class);

    public static void showErrorMessage(String message) {
        if (MyTunesRss.HEADLESS) {
            if (LOG.isErrorEnabled()) {
                LOG.error(message);
            }
            System.err.println(message);
        } else {
            showErrorMessage(MyTunesRss.ROOT_FRAME, message);
        }
    }

    public static void showErrorMessage(JFrame parent, String message) {
        SwingUtils.showMessage(parent,
                               JOptionPane.ERROR_MESSAGE,
                               MyTunesRss.BUNDLE.getString("error.title"),
                               message,
                               MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH);
    }

    public static void showInfoMessage(JFrame parent, String message) {
        SwingUtils.showMessage(parent,
                               JOptionPane.INFORMATION_MESSAGE,
                               MyTunesRss.BUNDLE.getString("info.title"),
                               message,
                               MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH);
    }

    public static void executeTask(String title, String text, String cancelButtonText, boolean progressBar, PleaseWaitTask task) {
        if (MyTunesRss.HEADLESS) {
            try {
                task.execute();
            } catch (Exception e) {
                task.handleException(e);
            }
        } else {
            if (title == null) {
                title = MyTunesRss.BUNDLE.getString("pleaseWait.defaultTitle");
            }
            PleaseWaitUtils.executeAndWait(MyTunesRss.ROOT_FRAME, MyTunesRss.PLEASE_WAIT_ICON, title, text, cancelButtonText, progressBar, task);
        }
    }

    public static String getBundleString(String key, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return MyTunesRss.BUNDLE.getString(key);
        }
        return MessageFormat.format(MyTunesRss.BUNDLE.getString(key), parameters);
    }

  public static HttpClient createHttpClient() {
    HttpClient httpClient = new HttpClient();
    if (MyTunesRss.CONFIG.isProxyServer()) {
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setProxy(MyTunesRss.CONFIG.getProxyHost(), MyTunesRss.CONFIG.getProxyPort());
        httpClient.setHostConfiguration(hostConfiguration);
    }
    return httpClient;
  }

  public static void shutdown() {
    System.exit(0);
  }

  public static void shutdownGracefully() {
    if (MyTunesRss.WEBSERVER.isRunning()) {
        MyTunesRss.stopWebserver();
    }
    if (!MyTunesRss.WEBSERVER.isRunning()) {
        MyTunesRss.CONFIG.saveWindowPosition(MyTunesRss.ROOT_FRAME.getLocation());
        MyTunesRss.CONFIG.save();
        MyTunesRss.SERVER_RUNNING_TIMER.cancel();
      final DatabaseBuilderTask databaseBuilderTask = MyTunesRss.createDatabaseBuilderTask();
      if (databaseBuilderTask.isRunning()) {
        MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.finishingUpdate"), null, false, new MyTunesRssTask() {
              public void execute() {
                  while (databaseBuilderTask.isRunning()) {
                    try {
                      Thread.sleep(1000);
                    } catch (InterruptedException e) {
                      // intentionally left blank
                    }
                  }
              }
          });
      }
      MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.shutdownDatabase"), null, false, new MyTunesRssTask() {
            public void execute() {
                MyTunesRss.STORE.destroy();
            }
        });
        MyTunesRss.ROOT_FRAME.dispose();
    }
    shutdown();
  }
}
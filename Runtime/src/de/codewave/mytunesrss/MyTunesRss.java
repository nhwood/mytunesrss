/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.server.*;
import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.*;
import de.codewave.utils.moduleinfo.*;
import de.codewave.utils.swing.*;
import org.apache.catalina.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;
import snoozesoft.systray4j.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.sql.*;
import java.util.*;
import java.util.Timer;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);
    public static String VERSION;
    public static URL UPDATE_URL;
    public static MyTunesRssDataStore STORE = new MyTunesRssDataStore();
    public static MyTunesRssConfig CONFIG = new MyTunesRssConfig();
    public static ResourceBundle BUNDLE = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
    public static WebServer WEBSERVER = new WebServer();
    public static Timer DATABASE_WATCHDOG = new Timer("MyTunesRSSDatabaseWatchdog");
    public static SysTray SYSTRAYMENU;
    public static MessageDigest MESSAGE_DIGEST;
    public static JFrame ROOT_FRAME;
    public static ImageIcon PLEASE_WAIT_ICON;
    public static MyTunesRssRegistration REGISTRATION = new MyTunesRssRegistration();
    public static int OPTION_PANE_MAX_MESSAGE_LENGTH = 100;
    public static boolean HEADLESS;

    static {
        try {
            UPDATE_URL = new URL("http://www.codewave.de/download/versions/mytunesrss.xml");
            //UPDATE_URL = new URL("file:///Users/mdescher/Desktop/mytunesrss.xml");
        } catch (MalformedURLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create update url.", e);
            }
        }
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create message digest.", e);
            }
        }
    }

    public static void main(final String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException,
            InstantiationException, ClassNotFoundException, IOException, SQLException {
        final Map<String, String[]> arguments = ProgramUtils.getCommandLineArguments(args);
        HEADLESS = arguments.containsKey("headless");
        if (arguments.containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Operating system: " + ProgramUtils.guessOperatingSystem().name());
            LOG.info("Java: " + getJavaEnvironment());
        }
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        VERSION = modulesInfo != null ? modulesInfo.getVersion() : System.getProperty("MyTunesRSS.version", "0.0.0");
        if (LOG.isInfoEnabled()) {
            LOG.info("Application version: " + VERSION);
        }
        REGISTRATION.init();
        if (Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("deleteDatabaseOnNextStartOnError", false)) {
            new DeleteDatabaseTask(false).execute();
        }
        loadConfiguration(arguments);
        if (HEADLESS) {
            executeHeadlessMode();
        } else {
            Thread.setDefaultUncaughtExceptionHandler(new MyTunesRssUncaughtHandler(ROOT_FRAME, false));
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        executeGuiMode();
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(null, e);
                        }
                    }
                }
            });
        }
    }

    private static String getJavaEnvironment() {
        StringBuffer java = new StringBuffer();
        java.append(System.getProperty("java.version")).append(" (\"").append(System.getProperty("java.home")).append("\")");
        return java.toString();
    }

    private static void executeGuiMode() throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
            ClassNotFoundException, IOException, InterruptedException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ROOT_FRAME = new JFrame(BUNDLE.getString("settings.title") + " v" + VERSION);
        Thread.setDefaultUncaughtExceptionHandler(new MyTunesRssUncaughtHandler(ROOT_FRAME, false));
        showNewVersionInfo();
        PLEASE_WAIT_ICON = new ImageIcon(MyTunesRss.class.getResource("PleaseWait.gif"));
        final Settings settings = new Settings();
        MyTunesRssMainWindowListener mainWindowListener = new MyTunesRssMainWindowListener(settings);
        executeApple(mainWindowListener);
        executeWindows(settings);
        ROOT_FRAME.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
        ROOT_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ROOT_FRAME.addWindowListener(mainWindowListener);
        ROOT_FRAME.getContentPane().add(settings.getRootPanel());
        ROOT_FRAME.setResizable(false);
        settings.setGuiMode(GuiMode.ServerIdle);
        SwingUtils.removeEmptyTooltips(ROOT_FRAME.getRootPane());
        int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", Integer.MAX_VALUE);
        int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", Integer.MAX_VALUE);
        if (x != Integer.MAX_VALUE && y != Integer.MAX_VALUE) {
            ROOT_FRAME.setLocation(x, y);
            SwingUtils.packAndShow(ROOT_FRAME);
        } else {
            SwingUtils.packAndShowRelativeTo(ROOT_FRAME, null);
        }
        if (CONFIG.isCheckUpdateOnStart()) {
            UpdateUtils.checkForUpdate(true);
        }
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.initializingDatabase"), null, false, new InitializeDatabaseTask());
        settings.init();
        if (CONFIG.isAutoStartServer()) {
            settings.doStartServer();
            if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
                // todo: hide window on osx instead of iconify
                ROOT_FRAME.setExtendedState(JFrame.ICONIFIED);
            } else {
                ROOT_FRAME.setExtendedState(JFrame.ICONIFIED);
            }
        }
    }

    private static void showNewVersionInfo() {
        String lastNewVersionInfo = Preferences.userRoot().node("/de/codewave/mytunesrss").get("lastNewVersionInfo", "0");
        if (!VERSION.equals(lastNewVersionInfo)) {
            try {
                String message = BUNDLE.getString("info.newVersion");
                if (StringUtils.isNotEmpty(message)) {
                    MyTunesRssUtils.showInfoMessage(ROOT_FRAME, message);
                }
            } catch (MissingResourceException e) {
                // intentionally left blank
            }
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("lastNewVersionInfo", VERSION);
        }
    }

    private static void executeHeadlessMode() throws IOException, SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Headless mode");
        }
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.initializingDatabase"), null, false, new InitializeDatabaseTask());
        startWebserver(new DatabaseBuilderTask());
        if (!WEBSERVER.isRunning()) {
            CONFIG.save();
            DATABASE_WATCHDOG.cancel();
            STORE.destroy();
        }
    }

    public static void startWebserver(DatabaseBuilderTask databaseBuilderTask) {
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("settings.buildDatabase"), null, false, databaseBuilderTask);
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.serverstarting"), null, false, new MyTunesRssTask() {
            public void execute() throws Exception {
                WEBSERVER.start();
            }
        });
        if (WEBSERVER.isRunning()) {
            if (CONFIG.isAutoUpdateDatabase()) {
                DatabaseWatchdogTask databaseWatchdogTask = new DatabaseWatchdogTask(DATABASE_WATCHDOG,
                                                                                     CONFIG.getAutoUpdateDatabaseInterval(),
                                                                                     databaseBuilderTask);
                DATABASE_WATCHDOG.schedule(databaseWatchdogTask, 60000 * CONFIG.getAutoUpdateDatabaseInterval());
            }
        }
    }

    private static void loadConfiguration(Map<String, String[]> arguments) throws MalformedURLException {
        if (arguments.containsKey("config")) {
            CONFIG.loadFromXml(new File(arguments.get("config")[0]).toURL());
        } else {
            CONFIG.loadFromPrefs();
        }
    }


    private static void executeWindows(Settings settingsForm) {
        if (ProgramUtils.guessOperatingSystem() == OperatingSystem.Windows && SysTrayMenu.isAvailable()) {
            SYSTRAYMENU = new SysTray(settingsForm);
        }
    }

    private static void executeApple(MyTunesRssMainWindowListener mainWindowListener) {
        if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
            try {
                Class appleExtensionsClass = Class.forName("de.codewave.mytunesrss.AppleExtensions");
                Method activateMethod = appleExtensionsClass.getMethod("activate", WindowListener.class);
                activateMethod.invoke(null, mainWindowListener);
            } catch (Exception e) {
                // intentionally left blank
            }
        }
    }

    private static class MyTunesRssMainWindowListener extends WindowAdapter {
        private Settings mySettingsForm;

        public MyTunesRssMainWindowListener(Settings settingsForm) {
            mySettingsForm = settingsForm;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            mySettingsForm.doQuitApplication();
        }

        @Override
        public void windowIconified(WindowEvent e) {
            if (SYSTRAYMENU != null) {
                ROOT_FRAME.setVisible(false);
                SYSTRAYMENU.show();
            }
        }
    }
}
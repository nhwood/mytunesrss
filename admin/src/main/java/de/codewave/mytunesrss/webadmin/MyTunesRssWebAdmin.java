/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.Window;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.ResourceBundleManager;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.component.ServerSideFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public class MyTunesRssWebAdmin extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssWebAdmin.class);

    public static final ResourceBundleManager RESOURCE_BUNDLE_MANAGER = new ResourceBundleManager(MyTunesRssWebAdmin.class.getClassLoader());

    public static final int ADMIN_REFRESHER_INTERVAL_MILLIS = 5000;
    
    private static final String BUNDLE_NAME = "de.codewave.mytunesrss.webadmin.MyTunesRssAdmin";

    public static String getBundleString(ResourceBundle bundle, String key, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return bundle.getString(key);
        }
        return MessageFormat.format(bundle.getString(key), parameters);
    }

    private ComponentFactory myComponentFactory;

    private ValidatorFactory myValidatorFactory;

    @Override
    public void init() {
        myComponentFactory = new ComponentFactory(BUNDLE_NAME, getLocale());
        myValidatorFactory = new ValidatorFactory(BUNDLE_NAME, getLocale());
        setTheme("mytunesrss");
        setMainWindow(new MainWindow(getBundleString("mainWindowTitle", MyTunesRss.VERSION), getNewWindowPanel()));
    }

    Panel getNewWindowPanel() {
        Panel panel;
        if (MyTunesRss.CONFIG.isShowInitialWizard()) {
            panel = new WizardPanel();
        } else if (MyTunesRss.CONFIG.isAdminPassword()) {
            panel = new LoginPanel();
        } else {
            ((WebApplicationContext)getContext()).getHttpSession().setAttribute("MyTunesRSSWebAdmin", Boolean.TRUE);
            panel = new StatusPanel();
        }
        return panel;
    }


    @Override
    public Window getWindow(String name) {
        Window window = super.getWindow(name);
        if (window == null) {
            window = new MainWindow(getBundleString("mainWindowTitle", MyTunesRss.VERSION), getNewWindowPanel());
            window.setName(name);
            addWindow(window);
        }
        return window;
    }

    public ResourceBundle getBundle() {
        return MyTunesRssWebAdmin.RESOURCE_BUNDLE_MANAGER.getBundle(BUNDLE_NAME, getLocale());
    }

    public String getBundleString(String key, Object... parameters) {
        return getBundleString(getBundle(), key, parameters);
    }

    public ComponentFactory getComponentFactory() {
        return myComponentFactory;
    }

    public ValidatorFactory getValidatorFactory() {
        return myValidatorFactory;
    }

    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Unhandled exception.", event.getThrowable());
        }
        MyTunesRss.UNHANDLED_EXCEPTION.set(true);
    }

    @Override
    public String getLogoutURL() {
        return "/";
    }

    public void createPlaylistTreeTableHierarchy(TreeTable treeTable, List<Playlist> playlists) {
        for (Playlist playlist : (Iterable<Playlist>) treeTable.getItemIds()) {
            treeTable.setParent(playlist, MyTunesRssUtils.findParentPlaylist(playlist, playlists));
            treeTable.setChildrenAllowed(playlist, MyTunesRssUtils.hasChildPlaylists(playlist, playlists));
        }
    }

    public ServerSideFileChooser.Labels getServerSideFileChooserLabels() {
        return new ServerSideFileChooser.Labels(
                getBundleString("serverSideFileChooser.roots"),
                getBundleString("serverSideFileChooser.ok"),
                getBundleString("serverSideFileChooser.cancel"),
                getBundleString("serverSideFileChooser.mkdir")
        );
    }
}

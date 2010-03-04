/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class MyTunesRssWebAdmin extends Application {

    public void init() {
        setTheme("mytunesrss");
        Window main = new Window("MyTunesRSS Adminstration"); // TODO i18n
        main.getContent().setWidth(100, Sizeable.UNITS_PERCENTAGE);
        setMainWindow(main);
        main.addComponent(new LoginPanel(MyTunesRssWebAdminUtils.COMPONENT_FACTORY));
    }

    public void setMainComponent(Component component) {
        Window mainWindow = getMainWindow();
        mainWindow.removeAllComponents();
        mainWindow.addComponent(component);
    }

    public void showError(String messageKey, Object... parameters) {
        getMainWindow().showNotification(null, MyTunesRssWebAdminUtils.getBundleString(messageKey, parameters), Window.Notification.TYPE_ERROR_MESSAGE);
    }
}

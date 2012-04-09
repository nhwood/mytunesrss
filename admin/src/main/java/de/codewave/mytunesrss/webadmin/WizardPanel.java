/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

public class WizardPanel extends Panel implements Button.ClickListener {

    private Button myAddDatasourceButton;
    private SmartTextField myDatasourcePath;
    private SmartTextField myUsername;
    private SmartTextField myPassword;
    private SmartTextField myRetypePassword;
    private Button myFinishButton;
    private Button mySkipButton;

    public void attach() {
        super.attach();
        setCaption(getApplication().getBundleString("wizardPanel.caption"));
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        myDatasourcePath = getApplication().getComponentFactory().createTextField("wizardPanel.datasource");
        myAddDatasourceButton = getApplication().getComponentFactory().createButton("wizardPanel.addDatasource", this);
        myUsername = getApplication().getComponentFactory().createTextField("wizardPanel.username");
        myPassword = getApplication().getComponentFactory().createPasswordTextField("wizardPanel.password");
        myRetypePassword = getApplication().getComponentFactory().createPasswordTextField("wizardPanel.retypePassword");
        myFinishButton = getApplication().getComponentFactory().createButton("wizardPanel.finish", this);
        mySkipButton = getApplication().getComponentFactory().createButton("wizardPanel.skip", this);
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descGeneral")));
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descDatasource")));
        addComponent(myDatasourcePath);
        addComponent(myAddDatasourceButton);
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descUser")));
        addComponent(myUsername);
        addComponent(myPassword);
        addComponent(myRetypePassword);
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descButtons")));
        Panel mainButtons = new Panel();
        addComponent(mainButtons);
        mainButtons.addStyleName("light");
        mainButtons.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        mainButtons.addComponent(myFinishButton);
        mainButtons.addComponent(mySkipButton);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddDatasourceButton) {
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getApplication().getBundleString("datasourcesConfigPanel.caption.selectLocalDatasource"), null, ServerSideFileChooser.PATTERN_ALL, DatasourcesConfigPanel.XML_FILE_PATTERN, false, getApplication().getServerSideFileChooserLabels()) {
                @Override
                protected void onFileSelected(File file) {
                    myDatasourcePath.setValue(file.getAbsolutePath());
                    getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else if (clickEvent.getSource() == myFinishButton) {
            if (isAnyEmpty(myDatasourcePath, myUsername, myPassword, myRetypePassword)) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("wizardPanel.error.allFieldsMandatory");
            } else {
                DatasourceConfig datasourceConfig = DatasourceConfig.create(UUID.randomUUID().toString(), myDatasourcePath.getStringValue(null));
                if (datasourceConfig == null) {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.invalidDatasourcePath");
                } else if (!myPassword.getStringValue("1").equals(myRetypePassword.getStringValue("2"))) {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("editUserConfigPanel.error.retypePassword");
                } else {
                    MyTunesRss.CONFIG.setDatasources(Collections.singletonList(datasourceConfig));
                    User user = new User(myUsername.getStringValue(null));
                    user.setPasswordHash(myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST));
                    user.setEmptyPassword(false);
                    MyTunesRss.CONFIG.addUser(user);
                    MyTunesRss.CONFIG.setInitialWizard(false); // do not run wizard again
                    MyTunesRss.CONFIG.save();
                    MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(MyTunesRss.CONFIG.getDatasources(), true);
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new WizardWorkingPanel());
                }
            }
        } else if (clickEvent.getSource() == mySkipButton) {
            MyTunesRss.CONFIG.setInitialWizard(false); // do not run wizard again
            MyTunesRss.CONFIG.save();
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(getApplication().getNewWindowPanel());
        }
    }

    private boolean isAnyEmpty(SmartTextField... fields) {
        for (SmartTextField field : fields) {
            if (StringUtils.isEmpty(field.getStringValue(null))) {
                return true;
            }
        }
        return false;
    }
}

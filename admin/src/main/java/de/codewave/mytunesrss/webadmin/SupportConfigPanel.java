/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssRegistration;
import de.codewave.mytunesrss.MyTunesRssRegistrationException;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.webadmin.task.SendSupportRequestTask;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ProgressWindow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SupportConfigPanel extends MyTunesRssConfigPanel implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

    private Form mySupportForm;
    private Form myRegistrationForm;
    private Form mySysInfoForm;
    private Select myLogLevel;
    private Button myShowLog;
    private SmartTextField myName;
    private SmartTextField myEmail;
    private TextArea myDescription;
    private CheckBox myIncludeItunesXml;
    private Button mySendSupport;
    private SmartTextField myRegName;
    private DateField myExpirationDate;
    private Upload myUploadLicense;
    private File myUploadDir;

    public void attach() {
        super.attach();
        init(getBundleString(MyTunesRssUtils.isAppStoreVersion() ? "supportConfigPanel.caption.appStore" : "supportConfigPanel.caption"), getComponentFactory().createGridLayout(1, MyTunesRssUtils.isAppStoreVersion() ? 3 : 4, true, true));
        mySupportForm = getComponentFactory().createForm(null, true);
        myName = getComponentFactory().createTextField("supportConfigPanel.name");
        myEmail = getComponentFactory().createTextField("supportConfigPanel.email");
        myDescription = getComponentFactory().createTextArea("supportConfigPanel.description");
        myDescription.setRows(10);
        myIncludeItunesXml = getComponentFactory().createCheckBox("supportConfigPanel.includeItunesXml");
        mySendSupport = getComponentFactory().createButton("supportConfigPanel.sendSupport", this);
        mySupportForm.addField("name", myName);
        mySupportForm.addField("email", myEmail);
        mySupportForm.addField("description", myDescription);
        mySupportForm.addField("includeItunesXml", myIncludeItunesXml);
        mySupportForm.addField("sendSupport", mySendSupport);
        addComponent(getComponentFactory().surroundWithPanel(mySupportForm, FORM_PANEL_MARGIN_INFO, getBundleString("supportConfigPanel.caption.support")));
        if (!MyTunesRssUtils.isAppStoreVersion()) {
            myRegistrationForm = getComponentFactory().createForm(null, true);
            myRegName = getComponentFactory().createTextField("supportConfigPanel.regName");
            myRegName.setEnabled(false);
            myExpirationDate = new DateField(getBundleString("supportConfigPanel.expirationDate"));
            myExpirationDate.setDateFormat(MyTunesRssUtils.getBundleString(Locale.getDefault(), "common.dateFormat"));
            myExpirationDate.setResolution(DateField.RESOLUTION_DAY);
            myExpirationDate.setEnabled(false);
            myRegistrationForm.addField("regName", myRegName);
            myRegistrationForm.addField("expirationDate", myExpirationDate);
            Panel registrationPanel = getComponentFactory().surroundWithPanel(myRegistrationForm, new Layout.MarginInfo(false, true, true, true), getBundleString("supportConfigPanel.caption.registration"));
            if (!MyTunesRss.REGISTRATION.isReleaseVersion()) {
                Label label = new Label(getBundleString("supportConfigPanel.prereleaseCannotBeRegistered"));
                registrationPanel.addComponent(label);
            } else {
                myUploadLicense = new Upload(null, this);
                myUploadLicense.setButtonCaption(getBundleString("supportConfigPanel.uploadLicense"));
                myUploadLicense.setImmediate(true);
                myUploadLicense.addListener((Upload.SucceededListener) this);
                myUploadLicense.addListener((Upload.FailedListener) this);
                registrationPanel.addComponent(myUploadLicense);
            }
            addComponent(registrationPanel);
        }
        mySysInfoForm = getComponentFactory().createForm(null, true);
        myLogLevel = getComponentFactory().createSelect("supportConfigPanel.logLevel", Arrays.asList(Level.OFF, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG));
        myShowLog = getComponentFactory().createButton("supportConfigPanel.showLog", this);
        mySysInfoForm.addField("logLevel", myLogLevel);
        mySysInfoForm.addField("showLog", myShowLog);
        addComponent(getComponentFactory().surroundWithPanel(mySysInfoForm, FORM_PANEL_MARGIN_INFO, getBundleString("supportConfigPanel.caption.sysInfo")));

        addDefaultComponents(0, MyTunesRssUtils.isAppStoreVersion() ? 2 : 3, 0, MyTunesRssUtils.isAppStoreVersion() ? 2 : 3, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myLogLevel.setValue(MyTunesRss.CONFIG.getCodewaveLogLevel());
        myName.setValue(MyTunesRss.CONFIG.getSupportName());
        myEmail.setValue(MyTunesRss.CONFIG.getSupportEmail());
        if (!MyTunesRssUtils.isAppStoreVersion()) {
            myRegName.setValue(MyTunesRss.REGISTRATION.getName());
            if (MyTunesRss.REGISTRATION.isExpirationDate()) {
                myExpirationDate.setValue(new Date(MyTunesRss.REGISTRATION.getExpiration()));
            }
        }
    }

    protected void writeToConfig() {
        if (!MyTunesRss.CONFIG.getCodewaveLogLevel().equals(myLogLevel.getValue())) {
            MyTunesRss.CONFIG.setCodewaveLogLevel((Level) myLogLevel.getValue());
            MyTunesRssUtils.setCodewaveLogLevel((Level) myLogLevel.getValue());
        }
        MyTunesRss.CONFIG.setSupportName((String) myName.getValue());
        MyTunesRss.CONFIG.setSupportEmail((String) myEmail.getValue());
        MyTunesRss.CONFIG.save();
        if (MyTunesRss.FORM != null) {
            MyTunesRss.FORM.refreshSupportConfig();
        }
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == mySendSupport) {
            if (StringUtils.isNotBlank((String) myName.getValue()) && StringUtils.isNotBlank((String) myEmail.getValue()) && StringUtils.isNotBlank((String) myDescription.getValue())) {
                SendSupportRequestTask task = new SendSupportRequestTask(((MainWindow) VaadinUtils.getApplicationWindow(this)), (String) myName.getValue(), (String) myEmail.getValue(), myDescription.getValue() + "\n\n\n", (Boolean) myIncludeItunesXml.getValue());
                new ProgressWindow(50, Sizeable.UNITS_EM, null, null, getBundleString("supportConfigPanel.task.message"), false, 2000, task).show(getWindow());
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("supportConfigPanel.error.allFieldsMandatoryForSupport");
            }
        } else if (clickEvent.getSource() == myShowLog) {
            getWindow().open(new ExternalResource("/-system/log"), UUID.randomUUID().toString());
        } else {
            super.buttonClick(clickEvent);
        }
    }

    public OutputStream receiveUpload(String filename, String MIMEType) {
        try {
            myUploadDir = new File(MyTunesRss.CACHE_DATA_PATH + "/license-upload");
            if (!myUploadDir.isDirectory()) {
                myUploadDir.mkdir();
            }
            return new FileOutputStream(new File(myUploadDir, filename));
        } catch (IOException e) {
            throw new RuntimeException("Could not receive upload.", e);
        }
    }

    public void uploadFailed(Upload.FailedEvent event) {
        FileUtils.deleteQuietly(myUploadDir);
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("supportConfigPanel.error.licenseUploadFailed");
    }

    public void uploadSucceeded(Upload.SucceededEvent event) {
        try {
            MyTunesRssRegistration registration = MyTunesRssRegistration.register(new File(myUploadDir, event.getFilename()));
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("supportConfigPanel.info.licenseOk", registration.getName());
        } catch (MyTunesRssRegistrationException e) {
            switch (e.getErrror()) {
                case InvalidFile:
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("supportConfigPanel.error.invalidLicenseFile");
                    break;
                case LicenseExpired:
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("supportConfigPanel.error.licenseExpired");
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected error code \"" + e.getErrror() + "\".");
            }
        } finally {
            FileUtils.deleteQuietly(myUploadDir);
        }
    }

}

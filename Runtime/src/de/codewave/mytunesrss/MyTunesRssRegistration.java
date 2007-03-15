package de.codewave.mytunesrss;

import de.codewave.utils.*;
import de.codewave.utils.registration.*;
import de.codewave.utils.xml.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.MyTunesRssRegistration
 */
public class MyTunesRssRegistration {
    private static final Log LOG = LogFactory.getLog(MyTunesRssRegistration.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final int UNREGISTERED_MAX_USERS = 3;
    public static final int UNREGISTERED_MAX_WATCHFOLDERS = 1;

    private String myName;
    private long myExpiration;
    private boolean myRegistered;
    private boolean myDefaultData;

    public static boolean isValidRegistration(File file) {
        try {
            return RegistrationUtils.getRegistrationData(file.toURL(), getPublicKey()) != null;
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not check registration file, assuming it is invalid.", e);
            }
        }
        return false;
    }

    private static URL getPublicKey() throws IOException {
        return MyTunesRssRegistration.class.getResource("/MyTunesRSS.public");
    }

    public void init(File file, boolean allowDefaultLicense) throws IOException {
        String path = PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        String registration = RegistrationUtils.getRegistrationData(file != null ? file.toURL() : new File(path + "/MyTunesRSS.key").toURL(), getPublicKey());
        if (registration != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Using registration data from preferences.");
            }
            handleRegistration(registration);
        } else if (allowDefaultLicense) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Using default registration data.");
            }
            handleRegistration(RegistrationUtils.getRegistrationData(getClass().getResource("/MyTunesRSS.key"), getPublicKey()));
            myDefaultData = true;
        }
    }

    private void handleRegistration(String registration) {
        if (StringUtils.isNotEmpty(registration)) {
            JXPathContext registrationContext = JXPathUtils.getContext(registration);
            myRegistered = JXPathUtils.getBooleanValue(registrationContext, "/registration/registered", false);
            myName = JXPathUtils.getStringValue(registrationContext, "/registration/name", "unregistered");
            String expirationDate = JXPathUtils.getStringValue(registrationContext, "/registration/expiration", null);
            if (expirationDate != null) {
                try {
                    myExpiration = DATE_FORMAT.parse(expirationDate).getTime();
                } catch (ParseException e) {
                    // intentionally left blank
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registration data:");
                LOG.debug("name=" + getName());
                LOG.debug("registered=" + isRegistered());
                LOG.debug("expiration=" + getExpiration(MyTunesRss.BUNDLE.getString("common.dateFormat")));
            }
        }
    }

    public long getExpiration() {
        return myExpiration;
    }

    public String getExpiration(String dateFormat) {
        if (getExpiration() > 0) {
            return new SimpleDateFormat(dateFormat).format(new Date(getExpiration()));
        }
        return "";
    }

    public boolean isExpired() {
        return myExpiration > 0 && myExpiration <= System.currentTimeMillis();
    }

    public boolean isExpirationDate() {
        return myExpiration > 0;
    }

    public String getName() {
        return myName;
    }

    public boolean isRegistered() {
        return myRegistered && !isExpired();
    }

    public boolean isDefaultData() {
        return myDefaultData;
    }
}
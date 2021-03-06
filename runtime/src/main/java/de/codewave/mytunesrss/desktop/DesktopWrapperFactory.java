/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for desktop wrapper.
 */
public class DesktopWrapperFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopWrapperFactory.class);

    /**
     * Create a desktop wrapper.
     *
     * @return A desktop wrapper.
     */
    public static DesktopWrapper createDesktopWrapper() {
        try {
            return new Java6DesktopWrapper();
        } catch (Throwable e) {
            LOGGER.debug("Could not create Java6DesktopWrapper: " + e.getMessage());
            return new NullDesktopWrapper();
        }
    }
}

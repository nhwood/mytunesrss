/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public abstract class DatasourceConfig implements Comparable<DatasourceConfig> {

    public static DatasourceConfig create(String definition) {
        File file = new File(definition);
        if (file.isFile() && StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(definition), "xml")) {
            return new ItunesDatasourceConfig(definition);
        } else if (file.isDirectory() && new File(file, IphotoDatasourceConfig.IPHOTO_XML_FILE_NAME).isFile()) {
            return new IphotoDatasourceConfig(definition);
        } else if (file.isDirectory() && new File(file, ApertureDatasourceConfig.APERTURE_XML_FILE_NAME).isFile()) {
            return new ApertureDatasourceConfig(definition);
        } else if (file.isDirectory()) {
            return new WatchfolderDatasourceConfig(definition);
        } else {
            return null;
        }
    }

    private String myDefinition;

    public DatasourceConfig(DatasourceConfig source) {
        myDefinition = source.getDefinition();
    }

    public DatasourceConfig(String definition) {
        setDefinition(definition);
    }

    public String getDefinition() {
        return myDefinition;
    }

    public void setDefinition(String definition) {
        if (StringUtils.isBlank(definition)) {
            throw new NullPointerException("Datasource definition must not be blank.");
        }
        myDefinition = StringUtils.trim(definition);
    }

    public abstract DatasourceType getType();

    public int compareTo(DatasourceConfig other) {
        return getDefinition().compareTo(other.getDefinition());
    }

    public static DatasourceConfig copy(DatasourceConfig config) {
        switch (config.getType()) {
            case Itunes:
                return new ItunesDatasourceConfig((ItunesDatasourceConfig)config);
            case Iphoto:
                return new IphotoDatasourceConfig((IphotoDatasourceConfig)config);
            case Aperture:
                return new ApertureDatasourceConfig((ApertureDatasourceConfig)config);
            case Watchfolder:
                return new WatchfolderDatasourceConfig((WatchfolderDatasourceConfig)config);
            default:
                throw new IllegalArgumentException("Illegal datasource type.");
        }
    }
}
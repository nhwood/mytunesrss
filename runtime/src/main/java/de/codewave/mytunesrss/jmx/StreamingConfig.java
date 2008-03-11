package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;

import javax.management.NotCompliantMBeanException;
import java.math.BigDecimal;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       Cologne Systems GmbH<br> <b>Creation Date:</b>
 * 01.03.2007
 *
 * @author Michael Descher
 * @version 1.0
 */
public class StreamingConfig extends MyTunesRssMBean implements StreamingConfigMBean {
    public StreamingConfig() throws NotCompliantMBeanException {
        super(StreamingConfigMBean.class);
    }

    public String getFaad2Binary() {
        return MyTunesRss.CONFIG.getFaad2Binary();
    }

    public String getAlacBinary() {
        return MyTunesRss.CONFIG.getAlacBinary();
    }

    public int getCacheMaxFiles() {
        return MyTunesRss.CONFIG.getStreamingCacheMaxFiles();
    }

    public int getCacheTimeout() {
        return MyTunesRss.CONFIG.getStreamingCacheTimeout();
    }

    public String getLameBinary() {
        return MyTunesRss.CONFIG.getLameBinary();
    }

    public void setFaad2Binary(String faad2Binary) {
        MyTunesRss.CONFIG.setFaad2Binary(faad2Binary);
        onChange();
    }

    public void setAlacBinary(String alacBinary) {
        MyTunesRss.CONFIG.setAlacBinary(alacBinary);
        onChange();
    }

    public void setCacheMaxFiles(int maxFiles) {
        MyTunesRss.CONFIG.setStreamingCacheMaxFiles(maxFiles);
        onChange();
    }

    public void setCacheTimeout(int timeout) {
        MyTunesRss.CONFIG.setStreamingCacheTimeout(timeout);
        onChange();
    }

    public void setLameBinary(String lameBinary) {
        MyTunesRss.CONFIG.setLameBinary(lameBinary);
        onChange();
    }

    public BigDecimal getBandwidthLimitFactor() {
        return MyTunesRss.CONFIG.getBandwidthLimitFactor();
    }

    public boolean isBandwidthLimit() {
        return MyTunesRss.CONFIG.isBandwidthLimit();
    }

    public void setBandwidthLimit(boolean limit) {
        MyTunesRss.CONFIG.setBandwidthLimit(limit);
        onChange();
    }

    public void setBandwidthLimitFactor(BigDecimal factor) {
        MyTunesRss.CONFIG.setBandwidthLimitFactor(factor);
        onChange();
    }
}
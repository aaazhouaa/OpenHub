package com.thirtydegreesray.openhub.mvp.model;

import java.util.Date;

/**
 * Created by ThirtyDegreesRay on 2017/9/16 15:55:57
 */

public class DownloadSource {

    private String url;
    private boolean isSourceCode;
    private String name;
    private long size;
    private int downloadCount;
    private Date createdAt;

    public DownloadSource(String url, boolean isSourceCode, String name) {
        this.url = url;
        this.isSourceCode = isSourceCode;
        this.name = name;
    }

    public DownloadSource(String url, boolean isSourceCode, String name, long size) {
        this.url = url;
        this.isSourceCode = isSourceCode;
        this.name = name;
        this.size = size;
    }

    public DownloadSource(String url, boolean isSourceCode, String name, long size,
                          int downloadCount, Date createdAt) {
        this.url = url;
        this.isSourceCode = isSourceCode;
        this.name = name;
        this.size = size;
        this.downloadCount = downloadCount;
        this.createdAt = createdAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSourceCode() {
        return isSourceCode;
    }

    public void setSourceCode(boolean sourceCode) {
        isSourceCode = sourceCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

package com.thirtydegreesray.openhub.http.model;

import com.google.gson.annotations.SerializedName;

/**
 * 创建/更新仓库文件的请求体（GitHub Contents API）。
 * content 为 base64 编码后的文件内容；更新已有文件时需携带 sha。
 */
public class FileRequestModel {

    private String message;
    private String content;
    private String sha;
    private String branch;

    public FileRequestModel(String message, String content, String sha, String branch) {
        this.message = message;
        this.content = content;
        this.sha = sha;
        this.branch = branch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}

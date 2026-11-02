package com.thirtydegreesray.openhub.http.model;

/**
 * 更新 Release 的请求体（GitHub Releases API）。
 * 仅提交需要修改的字段，其余字段不传。
 */
public class ReleaseRequestModel {

    private String body;

    public ReleaseRequestModel(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

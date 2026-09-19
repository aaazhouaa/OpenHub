package com.thirtydegreesray.openhub.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * A single code search result item.
 */
public class CodeSearchItem implements Parcelable {

    private String name;
    private String path;
    private String sha;
    @SerializedName("html_url") private String htmlUrl;
    private Repository repository;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public CodeSearchItem() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeString(this.sha);
        dest.writeString(this.htmlUrl);
        dest.writeParcelable(this.repository, flags);
    }

    protected CodeSearchItem(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.sha = in.readString();
        this.htmlUrl = in.readString();
        this.repository = in.readParcelable(Repository.class.getClassLoader());
    }

    public static final Creator<CodeSearchItem> CREATOR = new Creator<CodeSearchItem>() {
        @Override
        public CodeSearchItem createFromParcel(Parcel source) {
            return new CodeSearchItem(source);
        }

        @Override
        public CodeSearchItem[] newArray(int size) {
            return new CodeSearchItem[size];
        }
    };
}

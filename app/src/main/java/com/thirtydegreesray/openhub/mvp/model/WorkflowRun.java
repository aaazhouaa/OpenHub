package com.thirtydegreesray.openhub.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * A GitHub Actions workflow run.
 */
public class WorkflowRun implements Parcelable {

    private long id;
    private String name;
    @SerializedName("display_title") private String displayTitle;
    @SerializedName("head_branch") private String headBranch;
    @SerializedName("head_sha") private String headSha;
    @SerializedName("event") private String event;
    private String status;
    private String conclusion;
    @SerializedName("html_url") private String htmlUrl;
    @SerializedName("created_at") private Date createdAt;
    @SerializedName("updated_at") private Date updatedAt;
    @SerializedName("run_number") private long runNumber;
    @SerializedName("run_attempt") private long runAttempt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public void setHeadBranch(String headBranch) {
        this.headBranch = headBranch;
    }

    public String getHeadSha() {
        return headSha;
    }

    public void setHeadSha(String headSha) {
        this.headSha = headSha;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getRunNumber() {
        return runNumber;
    }

    public void setRunNumber(long runNumber) {
        this.runNumber = runNumber;
    }

    public long getRunAttempt() {
        return runAttempt;
    }

    public void setRunAttempt(long runAttempt) {
        this.runAttempt = runAttempt;
    }

    public WorkflowRun() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.displayTitle);
        dest.writeString(this.headBranch);
        dest.writeString(this.headSha);
        dest.writeString(this.event);
        dest.writeString(this.status);
        dest.writeString(this.conclusion);
        dest.writeString(this.htmlUrl);
        dest.writeLong(this.createdAt != null ? this.createdAt.getTime() : -1);
        dest.writeLong(this.updatedAt != null ? this.updatedAt.getTime() : -1);
        dest.writeLong(this.runNumber);
        dest.writeLong(this.runAttempt);
    }

    protected WorkflowRun(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.displayTitle = in.readString();
        this.headBranch = in.readString();
        this.headSha = in.readString();
        this.event = in.readString();
        this.status = in.readString();
        this.conclusion = in.readString();
        this.htmlUrl = in.readString();
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.runNumber = in.readLong();
        this.runAttempt = in.readLong();
    }

    public static final Creator<WorkflowRun> CREATOR = new Creator<WorkflowRun>() {
        @Override
        public WorkflowRun createFromParcel(Parcel source) {
            return new WorkflowRun(source);
        }

        @Override
        public WorkflowRun[] newArray(int size) {
            return new WorkflowRun[size];
        }
    };
}

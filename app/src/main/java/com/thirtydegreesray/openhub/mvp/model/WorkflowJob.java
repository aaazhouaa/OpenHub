package com.thirtydegreesray.openhub.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * A job within a GitHub Actions workflow run.
 */
public class WorkflowJob implements Parcelable {

    private long id;
    @SerializedName("run_id") private long runId;
    private String name;
    private String status;
    private String conclusion;
    @SerializedName("started_at") private Date startedAt;
    @SerializedName("completed_at") private Date completedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRunId() {
        return runId;
    }

    public void setRunId(long runId) {
        this.runId = runId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public WorkflowJob() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.runId);
        dest.writeString(this.name);
        dest.writeString(this.status);
        dest.writeString(this.conclusion);
        dest.writeLong(this.startedAt != null ? this.startedAt.getTime() : -1);
        dest.writeLong(this.completedAt != null ? this.completedAt.getTime() : -1);
    }

    protected WorkflowJob(Parcel in) {
        this.id = in.readLong();
        this.runId = in.readLong();
        this.name = in.readString();
        this.status = in.readString();
        this.conclusion = in.readString();
        long tmpStartedAt = in.readLong();
        this.startedAt = tmpStartedAt == -1 ? null : new Date(tmpStartedAt);
        long tmpCompletedAt = in.readLong();
        this.completedAt = tmpCompletedAt == -1 ? null : new Date(tmpCompletedAt);
    }

    public static final Creator<WorkflowJob> CREATOR = new Creator<WorkflowJob>() {
        @Override
        public WorkflowJob createFromParcel(Parcel source) {
            return new WorkflowJob(source);
        }

        @Override
        public WorkflowJob[] newArray(int size) {
            return new WorkflowJob[size];
        }
    };
}

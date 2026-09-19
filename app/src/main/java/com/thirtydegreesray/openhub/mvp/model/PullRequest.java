package com.thirtydegreesray.openhub.mvp.model;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * A GitHub pull request. Extends {@link Issue} so the existing issue list/detail
 * adapters and timeline rendering can be reused without a parallel UI stack.
 *
 * Created by OpenHub contributors.
 */
public class PullRequest extends Issue {

    public enum MergeableState {
        mergeable, conflicting, unknown
    }

    @SerializedName("diff_url") private String diffUrl;
    @SerializedName("patch_url") private String patchUrl;
    @SerializedName("issue_url") private String issueUrl;

    private boolean merged;
    @SerializedName("merged_at") private Date mergedAt;
    @SerializedName("merge_commit_sha") private String mergeCommitSha;
    @SerializedName("merged_by") private User mergedBy;

    private int commits;
    private int additions;
    private int deletions;
    @SerializedName("changed_files") private int changedFiles;

    @SerializedName("mergeable_state") private String mergeableState;

    @SerializedName("base") private PullRequestBase base;

    /**
     * The {@code base.repo} object of a pull request response. Only the fields the
     * UI needs are kept; the nested repository gives us owner/name when the issue
     * {@code repository_url} field is absent from the pulls API.
     */
    public static class PullRequestBase {
        private Repository repo;

        public Repository getRepo() {
            return repo;
        }

        public void setRepo(Repository repo) {
            this.repo = repo;
        }
    }

    private String baseRepoFullName;

    private String getBaseRepoFullName() {
        if (base != null && base.getRepo() != null && base.getRepo().getFullName() != null) {
            return base.getRepo().getFullName();
        }
        return baseRepoFullName;
    }

    public String getDiffUrl() {
        return diffUrl;
    }

    public void setDiffUrl(String diffUrl) {
        this.diffUrl = diffUrl;
    }

    public String getPatchUrl() {
        return patchUrl;
    }

    public void setPatchUrl(String patchUrl) {
        this.patchUrl = patchUrl;
    }

    public String getIssueUrl() {
        return issueUrl;
    }

    public void setIssueUrl(String issueUrl) {
        this.issueUrl = issueUrl;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public Date getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt;
    }

    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    public User getMergedBy() {
        return mergedBy;
    }

    public void setMergedBy(User mergedBy) {
        this.mergedBy = mergedBy;
    }

    public int getCommits() {
        return commits;
    }

    public void setCommits(int commits) {
        this.commits = commits;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public int getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(int changedFiles) {
        this.changedFiles = changedFiles;
    }

    public String getMergeableState() {
        return mergeableState;
    }

    public void setMergeableState(String mergeableState) {
        this.mergeableState = mergeableState;
    }

    public PullRequest() {
    }

    @Override
    public String getRepoName() {
        String fullName = getBaseRepoFullName();
        if (fullName != null && fullName.contains("/")) {
            return fullName.substring(fullName.indexOf("/") + 1);
        }
        return super.getRepoName();
    }

    @Override
    public String getRepoAuthorName() {
        String fullName = getBaseRepoFullName();
        if (fullName != null && fullName.contains("/")) {
            return fullName.substring(0, fullName.indexOf("/"));
        }
        return super.getRepoAuthorName();
    }

    @Override
    public String getRepoFullName() {
        String fullName = getBaseRepoFullName();
        if (fullName != null) {
            return fullName;
        }
        return super.getRepoFullName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.diffUrl);
        dest.writeString(this.patchUrl);
        dest.writeString(this.issueUrl);
        dest.writeByte(this.merged ? (byte) 1 : (byte) 0);
        dest.writeLong(this.mergedAt != null ? this.mergedAt.getTime() : -1);
        dest.writeString(this.mergeCommitSha);
        dest.writeParcelable(this.mergedBy, flags);
        dest.writeInt(this.commits);
        dest.writeInt(this.additions);
        dest.writeInt(this.deletions);
        dest.writeInt(this.changedFiles);
        dest.writeString(this.mergeableState);
        dest.writeString(getBaseRepoFullName());
    }

    protected PullRequest(Parcel in) {
        super(in);
        this.diffUrl = in.readString();
        this.patchUrl = in.readString();
        this.issueUrl = in.readString();
        this.merged = in.readByte() != 0;
        long tmpMergedAt = in.readLong();
        this.mergedAt = tmpMergedAt == -1 ? null : new Date(tmpMergedAt);
        this.mergeCommitSha = in.readString();
        this.mergedBy = in.readParcelable(User.class.getClassLoader());
        this.commits = in.readInt();
        this.additions = in.readInt();
        this.deletions = in.readInt();
        this.changedFiles = in.readInt();
        this.mergeableState = in.readString();
        this.baseRepoFullName = in.readString();
    }

    public static final Creator<PullRequest> CREATOR = new Creator<PullRequest>() {
        @Override
        public PullRequest createFromParcel(Parcel source) {
            return new PullRequest(source);
        }

        @Override
        public PullRequest[] newArray(int size) {
            return new PullRequest[size];
        }
    };
}

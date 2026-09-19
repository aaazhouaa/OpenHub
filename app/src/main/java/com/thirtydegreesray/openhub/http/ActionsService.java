package com.thirtydegreesray.openhub.http;

import androidx.annotation.NonNull;

import com.thirtydegreesray.openhub.mvp.model.WorkflowJob;
import com.thirtydegreesray.openhub.mvp.model.WorkflowRun;

import java.util.ArrayList;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * GitHub Actions REST API endpoints.
 */
public interface ActionsService {

    @NonNull @GET("repos/{owner}/{repo}/actions/runs")
    Observable<Response<WorkflowRunListResponse>> getWorkflowRuns(
            @Header("forceNetWork") boolean forceNetWork,
            @Path("owner") String owner,
            @Path("repo") String repo,
            @Query("page") int page
    );

    @NonNull @GET("repos/{owner}/{repo}/actions/runs/{runId}/jobs")
    Observable<Response<WorkflowJobListResponse>> getWorkflowRunJobs(
            @Header("forceNetWork") boolean forceNetWork,
            @Path("owner") String owner,
            @Path("repo") String repo,
            @Path("runId") long runId
    );

    /** Wrapper for the paginated {@code GET /repos/{owner}/{repo}/actions/runs} response. */
    class WorkflowRunListResponse {
        @com.google.gson.annotations.SerializedName("total_count") private int totalCount;
        @com.google.gson.annotations.SerializedName("workflow_runs") private ArrayList<WorkflowRun> workflowRuns;

        public int getTotalCount() {
            return totalCount;
        }

        public ArrayList<WorkflowRun> getWorkflowRuns() {
            return workflowRuns;
        }
    }

    /** Wrapper for the {@code GET /repos/{owner}/{repo}/actions/runs/{run_id}/jobs} response. */
    class WorkflowJobListResponse {
        @com.google.gson.annotations.SerializedName("total_count") private int totalCount;
        private ArrayList<WorkflowJob> jobs;

        public int getTotalCount() {
            return totalCount;
        }

        public ArrayList<WorkflowJob> getJobs() {
            return jobs;
        }
    }
}

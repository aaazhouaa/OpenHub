package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.ActionsService;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.mvp.contract.IWorkflowRunDetailContract;
import com.thirtydegreesray.openhub.mvp.model.WorkflowJob;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePresenter;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Loads the jobs of a single GitHub Actions workflow run.
 */
public class WorkflowRunDetailPresenter extends BasePresenter<IWorkflowRunDetailContract.View>
        implements IWorkflowRunDetailContract.Presenter {

    @AutoAccess String owner;
    @AutoAccess String repoName;
    @AutoAccess long runId;

    private ArrayList<WorkflowJob> jobs;

    @Inject
    public WorkflowRunDetailPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
        loadJobs();
    }

    @Override
    public void loadJobs() {
        if (jobs != null) {
            mView.showJobs(jobs);
            return;
        }
        mView.showLoading();
        HttpObserver<ActionsService.WorkflowJobListResponse> httpObserver =
                new HttpObserver<ActionsService.WorkflowJobListResponse>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.hideLoading();
                        mView.showLoadError(getErrorTip(error));
                    }

                    @Override
                    public void onSuccess(HttpResponse<ActionsService.WorkflowJobListResponse> response) {
                        mView.hideLoading();
                        jobs = response.body().getJobs();
                        mView.showJobs(jobs);
                    }
                };
        generalRxHttpExecute(new IObservableCreator<ActionsService.WorkflowJobListResponse>() {
            @Override
            public Observable<Response<ActionsService.WorkflowJobListResponse>> createObservable(
                    boolean forceNetWork) {
                return getActionsService().getWorkflowRunJobs(forceNetWork,
                        owner, repoName, runId);
            }
        }, httpObserver, true);
    }
}

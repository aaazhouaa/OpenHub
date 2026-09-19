package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.ActionsService;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.http.error.HttpPageNoFoundError;
import com.thirtydegreesray.openhub.mvp.contract.IWorkflowRunsContract;
import com.thirtydegreesray.openhub.mvp.model.WorkflowRun;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePagerPresenter;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Loads the GitHub Actions workflow runs of a repository.
 */
public class WorkflowRunsPresenter extends BasePagerPresenter<IWorkflowRunsContract.View>
        implements IWorkflowRunsContract.Presenter {

    @AutoAccess String owner;
    @AutoAccess String repoName;

    private ArrayList<WorkflowRun> runs;

    @Inject
    public WorkflowRunsPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    protected void loadData() {
        loadWorkflowRuns(1, false);
    }

    @Override
    public void loadWorkflowRuns(final int page, final boolean isReload) {
        final boolean readCacheFirst = page == 1 && !isReload;
        mView.showLoading();
        HttpObserver<ActionsService.WorkflowRunListResponse> httpObserver =
                new HttpObserver<ActionsService.WorkflowRunListResponse>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.hideLoading();
                        handleError(error);
                    }

                    @Override
                    public void onSuccess(HttpResponse<ActionsService.WorkflowRunListResponse> response) {
                        mView.hideLoading();
                        ArrayList<WorkflowRun> result = response.body().getWorkflowRuns();
                        if (isReload || runs == null || readCacheFirst) {
                            runs = result;
                        } else {
                            runs.addAll(result);
                        }
                        if (result.size() == 0 && runs.size() != 0) {
                            mView.setCanLoadMore(false);
                        } else {
                            mView.showWorkflowRuns(runs);
                        }
                    }
                };
        generalRxHttpExecute(new IObservableCreator<ActionsService.WorkflowRunListResponse>() {
            @Override
            public Observable<Response<ActionsService.WorkflowRunListResponse>> createObservable(
                    boolean forceNetWork) {
                return getActionsService().getWorkflowRuns(forceNetWork, owner, repoName, page);
            }
        }, httpObserver, readCacheFirst);
    }

    private void handleError(Throwable error) {
        if (!StringUtils.isBlankList(runs)) {
            mView.showErrorToast(getErrorTip(error));
        } else if (error instanceof HttpPageNoFoundError) {
            mView.showWorkflowRuns(new ArrayList<WorkflowRun>());
        } else {
            mView.showLoadError(getErrorTip(error));
        }
    }

    public String getOwner() {
        return owner;
    }

    public String getRepoName() {
        return repoName;
    }
}

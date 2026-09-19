package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.mvp.contract.IPullRequestDetailContract;
import com.thirtydegreesray.openhub.mvp.model.PullRequest;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePresenter;
import com.thirtydegreesray.openhub.util.StringUtils;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Loads a single pull request detail (reuses the issue timeline for conversation).
 */
public class PullRequestDetailPresenter extends BasePresenter<IPullRequestDetailContract.View>
        implements IPullRequestDetailContract.Presenter {

    @AutoAccess PullRequest pullRequest;
    @AutoAccess String owner;
    @AutoAccess String repoName;
    @AutoAccess int number;

    @Inject
    public PullRequestDetailPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
        if (pullRequest != null) {
            owner = pullRequest.getRepoAuthorName();
            repoName = pullRequest.getRepoName();
            number = pullRequest.getNumber();
            mView.showPullRequest(pullRequest);
            loadPullRequestInfo(false);
        } else {
            loadPullRequestInfo(true);
        }
    }

    private void loadPullRequestInfo(final boolean showLoading) {
        if (showLoading) mView.showLoading();
        HttpObserver<PullRequest> httpObserver = new HttpObserver<PullRequest>() {
            @Override
            public void onError(Throwable error) {
                if (showLoading) mView.hideLoading();
                mView.showErrorToast(getErrorTip(error));
            }

            @Override
            public void onSuccess(HttpResponse<PullRequest> response) {
                if (showLoading) mView.hideLoading();
                pullRequest = response.body();
                mView.showPullRequest(pullRequest);
            }
        };
        generalRxHttpExecute(new IObservableCreator<PullRequest>() {
            @Override
            public Observable<Response<PullRequest>> createObservable(boolean forceNetWork) {
                return getPullRequestService().getPullRequestInfo(forceNetWork,
                        owner, repoName, number);
            }
        }, httpObserver, true);
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepoName() {
        return repoName;
    }
}

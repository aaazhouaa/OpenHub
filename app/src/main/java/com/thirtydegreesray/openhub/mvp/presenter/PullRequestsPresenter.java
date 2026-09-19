package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.http.error.HttpPageNoFoundError;
import com.thirtydegreesray.openhub.mvp.contract.IPullRequestsContract;
import com.thirtydegreesray.openhub.mvp.model.PullRequest;
import com.thirtydegreesray.openhub.mvp.model.filter.IssuesFilter;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePagerPresenter;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Loads the open/closed pull requests of a repository.
 */
public class PullRequestsPresenter extends BasePagerPresenter<IPullRequestsContract.View>
        implements IPullRequestsContract.Presenter {

    @AutoAccess String userId;
    @AutoAccess String repoName;
    @AutoAccess IssuesFilter filter;

    private ArrayList<PullRequest> pullRequests;

    @Inject
    public PullRequestsPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    protected void loadData() {
        loadPullRequests(1, false);
    }

    @Override
    public void loadPullRequests(int page, boolean isReload) {
        loadPullRequests(getFilter(), page, isReload);
    }

    @Override
    public void loadPullRequests(IssuesFilter filter, int page, boolean isReload) {
        this.filter = filter;
        final boolean readCacheFirst = page == 1 && !isReload;
        mView.showLoading();

        HttpObserver<ArrayList<PullRequest>> httpObserver =
                new HttpObserver<ArrayList<PullRequest>>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.hideLoading();
                        handleError(error);
                    }

                    @Override
                    public void onSuccess(HttpResponse<ArrayList<PullRequest>> response) {
                        mView.hideLoading();
                        if (isReload || pullRequests == null || readCacheFirst) {
                            pullRequests = response.body();
                        } else {
                            pullRequests.addAll(response.body());
                        }
                        if (response.body().size() == 0 && pullRequests.size() != 0) {
                            mView.setCanLoadMore(false);
                        } else {
                            mView.showPullRequests(pullRequests);
                        }
                    }
                };

        final String state = filter.getIssueState().name().toLowerCase();
        final String sort = filter.getSortType().name().toLowerCase();
        final String direction = filter.getSortDirection().name().toLowerCase();

        generalRxHttpExecute(new IObservableCreator<ArrayList<PullRequest>>() {
            @Override
            public Observable<Response<ArrayList<PullRequest>>> createObservable(boolean forceNetWork) {
                return getPullRequestService().getPullRequests(forceNetWork, userId, repoName,
                        state, sort, direction, page);
            }
        }, httpObserver, readCacheFirst);
    }

    private IssuesFilter getFilter() {
        if (filter == null) {
            filter = new IssuesFilter(IssuesFilter.Type.Repo,
                    com.thirtydegreesray.openhub.mvp.model.Issue.IssueState.open);
        }
        return filter;
    }

    private void handleError(Throwable error) {
        if (!StringUtils.isBlankList(pullRequests)) {
            mView.showErrorToast(getErrorTip(error));
        } else if (error instanceof HttpPageNoFoundError) {
            mView.showPullRequests(new ArrayList<PullRequest>());
        } else {
            mView.showLoadError(getErrorTip(error));
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getRepoName() {
        return repoName;
    }
}

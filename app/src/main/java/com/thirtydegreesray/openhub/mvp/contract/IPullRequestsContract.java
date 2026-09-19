package com.thirtydegreesray.openhub.mvp.contract;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseListContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBasePagerContract;
import com.thirtydegreesray.openhub.mvp.model.PullRequest;
import com.thirtydegreesray.openhub.mvp.model.filter.IssuesFilter;

import java.util.ArrayList;

/**
 * Contract for the pull request list screen.
 */
public interface IPullRequestsContract {

    interface View extends IBaseContract.View, IBasePagerContract.View, IBaseListContract.View {
        void showPullRequests(ArrayList<PullRequest> pullRequests);
    }

    interface Presenter extends IBasePagerContract.Presenter<IPullRequestsContract.View> {
        void loadPullRequests(int page, boolean isReload);

        void loadPullRequests(IssuesFilter filter, int page, boolean isReload);
    }
}

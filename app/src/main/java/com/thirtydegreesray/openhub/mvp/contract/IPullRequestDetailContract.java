package com.thirtydegreesray.openhub.mvp.contract;

import androidx.annotation.NonNull;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.model.PullRequest;

/**
 * Contract for the pull request detail screen.
 */
public interface IPullRequestDetailContract {

    interface View extends IBaseContract.View {
        void showPullRequest(@NonNull PullRequest pullRequest);
    }

    interface Presenter extends IBaseContract.Presenter<IPullRequestDetailContract.View> {
    }
}

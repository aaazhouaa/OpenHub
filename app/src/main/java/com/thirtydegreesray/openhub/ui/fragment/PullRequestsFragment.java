package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.IPullRequestsContract;
import com.thirtydegreesray.openhub.mvp.model.Issue;
import com.thirtydegreesray.openhub.mvp.model.PullRequest;
import com.thirtydegreesray.openhub.mvp.model.filter.IssuesFilter;
import com.thirtydegreesray.openhub.mvp.presenter.PullRequestsPresenter;
import com.thirtydegreesray.openhub.ui.activity.IssueDetailActivity;
import com.thirtydegreesray.openhub.ui.activity.PullRequestDetailActivity;
import com.thirtydegreesray.openhub.ui.adapter.IssuesAdapter;
import com.thirtydegreesray.openhub.ui.fragment.base.ListFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;

/**
 * Lists the pull requests of a repository (open / closed tabs).
 */
public class PullRequestsFragment extends ListFragment<PullRequestsPresenter, IssuesAdapter>
        implements IPullRequestsContract.View {

    public static PullRequestsFragment createForRepo(@NonNull Issue.IssueState state,
                                                     @NonNull String userId,
                                                     @NonNull String repoName) {
        PullRequestsFragment fragment = new PullRequestsFragment();
        fragment.setArguments(BundleHelper.builder()
                .put("filter", new IssuesFilter(IssuesFilter.Type.Repo, state))
                .put("userId", userId)
                .put("repoName", repoName).build());
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    protected void setupFragmentComponent(AppComponent appComponent) {
        DaggerFragmentComponent.builder()
                .appComponent(appComponent)
                .fragmentModule(new FragmentModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void initFragment(Bundle savedInstanceState) {
        super.initFragment(savedInstanceState);
        setLoadMoreEnable(true);
        adapter.setUserIssues(false);
    }

    @Override
    protected void onReLoadData() {
        mPresenter.loadPullRequests(1, true);
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.no_pull_requests);
    }

    @Override
    public void showPullRequests(ArrayList<PullRequest> pullRequests) {
        adapter.setData(new ArrayList<>(pullRequests));
        postNotifyDataSetChanged();
    }

    @Override
    protected void onLoadMore(int page) {
        super.onLoadMore(page);
        mPresenter.loadPullRequests(page, false);
    }

    @Override
    public void onItemClick(int position, @NonNull View view) {
        super.onItemClick(position, view);
        PullRequest pullRequest = (PullRequest) adapter.getData().get(position);
        View avatarView = view.findViewById(R.id.user_avatar);
        View titleView = view.findViewById(R.id.issue_title);
        PullRequestDetailActivity.show(getActivity(), avatarView, titleView, pullRequest);
    }

    @Override
    public void onFragmentShowed() {
        super.onFragmentShowed();
        if (mPresenter != null) mPresenter.prepareLoadData();
    }
}

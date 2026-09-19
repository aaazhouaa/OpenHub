package com.thirtydegreesray.openhub.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.core.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerActivityComponent;
import com.thirtydegreesray.openhub.inject.module.ActivityModule;
import com.thirtydegreesray.openhub.mvp.contract.IPullRequestDetailContract;
import com.thirtydegreesray.openhub.mvp.model.Issue;
import com.thirtydegreesray.openhub.mvp.model.PullRequest;
import com.thirtydegreesray.openhub.mvp.presenter.PullRequestDetailPresenter;
import com.thirtydegreesray.openhub.ui.activity.base.PagerActivity;
import com.thirtydegreesray.openhub.ui.adapter.base.FragmentPagerModel;
import com.thirtydegreesray.openhub.ui.fragment.IssueTimelineFragment;
import com.thirtydegreesray.openhub.ui.fragment.PullRequestFilesFragment;
import com.thirtydegreesray.openhub.util.AppOpener;
import com.thirtydegreesray.openhub.util.AppUtils;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Detail view for a pull request: header summary, conversation (reusing the
 * issue timeline) and changed files.
 */
public class PullRequestDetailActivity extends PagerActivity<PullRequestDetailPresenter>
        implements IPullRequestDetailContract.View {

    public static void show(@NonNull Activity activity, @NonNull View avatarView,
                            @NonNull View titleView, @NonNull PullRequest pullRequest) {
        Intent intent = new Intent(activity, PullRequestDetailActivity.class);
        Pair<View, String> avatarPair = Pair.create(avatarView, "userAvatar");
        Pair<View, String> titlePair = Pair.create(titleView, "issueTitle");
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, avatarPair, titlePair);
        intent.putExtras(BundleHelper.builder().put("pullRequest", pullRequest).build());
        activity.startActivity(intent, optionsCompat.toBundle());
    }

    public static void show(@NonNull Activity activity, @NonNull String owner,
                            @NonNull String repoName, int number) {
        Intent intent = new Intent(activity, PullRequestDetailActivity.class)
                .putExtras(BundleHelper.builder()
                        .put("owner", owner)
                        .put("repoName", repoName)
                        .put("number", number).build());
        activity.startActivity(intent);
    }

    private IssueTimelineFragment timelineFragment;
    private PullRequestFilesFragment filesFragment;

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .activityModule(new ActivityModule(this))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    protected int getContentView() {
        return R.layout.activity_view_pager;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setToolbarScrollAble(true);
        setToolbarBackEnable();
        setToolbarTitle(getString(R.string.pull_request));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mPresenter.getPullRequest() != null) {
            getMenuInflater().inflate(R.menu.menu_issue_detail, menu);
            menu.removeItem(R.id.action_issue_toggle);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_open_in_browser:
                AppOpener.openInCustomTabsOrBrowser(getActivity(),
                        mPresenter.getPullRequest().getHtmlUrl());
                return true;
            case R.id.action_share:
                AppOpener.shareText(getActivity(), mPresenter.getPullRequest().getHtmlUrl());
                return true;
            case R.id.action_copy_url:
                AppUtils.copyToClipboard(getActivity(), mPresenter.getPullRequest().getHtmlUrl());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showPullRequest(@NonNull PullRequest pullRequest) {
        setToolbarTitle(getString(R.string.pull_request).concat(" #")
                .concat(String.valueOf(pullRequest.getNumber())));
        setToolbarSubTitle(pullRequest.getTitle() + "    " + buildStateSummary(pullRequest));
        invalidateOptionsMenu();

        if (pagerAdapter.getCount() == 0) {
            timelineFragment = IssueTimelineFragment.create(pullRequest);
            filesFragment = PullRequestFilesFragment.create(
                    pullRequest.getRepoAuthorName(), pullRequest.getRepoName(),
                    pullRequest.getNumber());

            ArrayList<FragmentPagerModel> pagerModels = new ArrayList<>();
            pagerModels.add(new FragmentPagerModel(getString(R.string.conversation), timelineFragment));
            pagerModels.add(new FragmentPagerModel(getString(R.string.files), filesFragment));
            for (FragmentPagerModel model : pagerModels) {
                model.getFragment().setPagerFragment(true);
            }

            pagerAdapter.setPagerList(pagerModels);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setupWithViewPager(viewPager);
            viewPager.setAdapter(pagerAdapter);
            showFirstPager();

            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
                p.setMargins(12, 0, 12, 0);
                tab.setLayoutParams(p);
            }
        }
    }

    private String buildStateSummary(@NonNull PullRequest pullRequest) {
        String state;
        if (pullRequest.isMerged()) {
            state = getString(R.string.merged);
        } else if (Issue.IssueState.open.equals(pullRequest.getState())) {
            state = getString(R.string.open);
        } else {
            state = getString(R.string.closed);
        }
        int additions = pullRequest.getAdditions();
        int deletions = pullRequest.getDeletions();
        if (additions > 0 || deletions > 0) {
            return String.format(Locale.getDefault(), "%s  +%d -%d",
                    state, additions, deletions);
        }
        return state;
    }

    @Override
    public int getPagerSize() {
        return 2;
    }

    @Override
    protected int getFragmentPosition(Fragment fragment) {
        if (fragment instanceof IssueTimelineFragment) {
            return 0;
        } else if (fragment instanceof PullRequestFilesFragment) {
            return 1;
        }
        return -1;
    }

    @Override
    protected void onToolbarDoubleClick() {
        super.onToolbarDoubleClick();
        if (timelineFragment != null) timelineFragment.scrollToTop();
    }
}

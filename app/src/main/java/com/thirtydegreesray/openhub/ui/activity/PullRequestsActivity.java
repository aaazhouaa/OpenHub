package com.thirtydegreesray.openhub.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerActivityComponent;
import com.thirtydegreesray.openhub.inject.module.ActivityModule;
import com.thirtydegreesray.openhub.mvp.model.Issue;
import com.thirtydegreesray.openhub.mvp.presenter.IssuesActPresenter;
import com.thirtydegreesray.openhub.ui.activity.base.PagerActivity;
import com.thirtydegreesray.openhub.ui.adapter.base.FragmentPagerModel;
import com.thirtydegreesray.openhub.ui.adapter.base.FragmentViewPagerAdapter;
import com.thirtydegreesray.openhub.ui.fragment.PullRequestsFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists a repository's pull requests (open / closed).
 */
public class PullRequestsActivity extends PagerActivity<IssuesActPresenter> {

    public static void show(@NonNull Activity activity, @NonNull String owner,
                            @NonNull String repo) {
        Intent intent = createIntent(activity, owner, repo);
        activity.startActivity(intent);
    }

    public static Intent createIntent(@NonNull Activity activity, @NonNull String owner,
                                      @NonNull String repo) {
        return new Intent(activity, PullRequestsActivity.class)
                .putExtras(BundleHelper.builder()
                        .put("userId", owner)
                        .put("repoName", repo).build());
    }

    @AutoAccess String userId;
    @AutoAccess String repoName;

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .activityModule(new ActivityModule(getActivity()))
                .build()
                .inject(this);
    }

    @Override
    protected void initActivity() {
        super.initActivity();
        pagerAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
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
        setToolbarTitle(getString(R.string.pull_requests), userId.concat("/").concat(repoName));

        List<FragmentPagerModel> pagerModels = new ArrayList<>();
        pagerModels.add(new FragmentPagerModel(getString(R.string.open),
                PullRequestsFragment.createForRepo(Issue.IssueState.open, userId, repoName)));
        pagerModels.add(new FragmentPagerModel(getString(R.string.closed),
                PullRequestsFragment.createForRepo(Issue.IssueState.closed, userId, repoName)));
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
            p.setMargins(getResources().getDimensionPixelSize(R.dimen.spacing_normal), 0,
                    getResources().getDimensionPixelSize(R.dimen.spacing_normal), 0);
            tab.setLayoutParams(p);
        }
    }

    @Override
    public int getPagerSize() {
        return 2;
    }

    @Override
    protected int getFragmentPosition(Fragment fragment) {
        return -1;
    }
}

package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.IWorkflowRunsContract;
import com.thirtydegreesray.openhub.mvp.model.WorkflowRun;
import com.thirtydegreesray.openhub.mvp.presenter.WorkflowRunsPresenter;
import com.thirtydegreesray.openhub.ui.activity.WorkflowRunDetailActivity;
import com.thirtydegreesray.openhub.ui.adapter.WorkflowRunsAdapter;
import com.thirtydegreesray.openhub.ui.fragment.base.ListFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;

/**
 * Lists a repository's GitHub Actions workflow runs.
 */
public class WorkflowRunsFragment extends ListFragment<WorkflowRunsPresenter, WorkflowRunsAdapter>
        implements IWorkflowRunsContract.View {

    public static WorkflowRunsFragment create(@NonNull String owner, @NonNull String repoName) {
        WorkflowRunsFragment fragment = new WorkflowRunsFragment();
        fragment.setArguments(BundleHelper.builder()
                .put("owner", owner)
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
    }

    @Override
    protected void onReLoadData() {
        mPresenter.loadWorkflowRuns(1, true);
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.no_workflow_runs);
    }

    @Override
    public void showWorkflowRuns(ArrayList<WorkflowRun> runs) {
        adapter.setData(runs);
        postNotifyDataSetChanged();
    }

    @Override
    protected void onLoadMore(int page) {
        super.onLoadMore(page);
        mPresenter.loadWorkflowRuns(page, false);
    }

    @Override
    public void onItemClick(int position, @NonNull View view) {
        super.onItemClick(position, view);
        WorkflowRun run = adapter.getData().get(position);
        WorkflowRunDetailActivity.show(getActivity(), mPresenter.getOwner(),
                mPresenter.getRepoName(), run);
    }

    @Override
    public void onFragmentShowed() {
        super.onFragmentShowed();
        if (mPresenter != null) mPresenter.prepareLoadData();
    }
}

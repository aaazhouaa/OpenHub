package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.IWorkflowRunDetailContract;
import com.thirtydegreesray.openhub.mvp.model.WorkflowJob;
import com.thirtydegreesray.openhub.mvp.presenter.WorkflowRunDetailPresenter;
import com.thirtydegreesray.openhub.ui.adapter.WorkflowJobsAdapter;
import com.thirtydegreesray.openhub.ui.fragment.base.ListFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;

/**
 * Shows the jobs of a workflow run.
 */
public class WorkflowRunDetailFragment extends ListFragment<WorkflowRunDetailPresenter, WorkflowJobsAdapter>
        implements IWorkflowRunDetailContract.View {

    public static WorkflowRunDetailFragment create(@NonNull String owner,
                                                   @NonNull String repoName, long runId) {
        WorkflowRunDetailFragment fragment = new WorkflowRunDetailFragment();
        fragment.setArguments(BundleHelper.builder()
                .put("owner", owner)
                .put("repoName", repoName)
                .put("runId", runId).build());
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
        setLoadMoreEnable(false);
        setRefreshEnable(false);
    }

    @Override
    protected void onReLoadData() {
        mPresenter.loadJobs();
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.no_workflow_jobs);
    }

    @Override
    public void showJobs(ArrayList<WorkflowJob> jobs) {
        adapter.setData(jobs);
        postNotifyDataSetChanged();
    }
}

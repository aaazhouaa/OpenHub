package com.thirtydegreesray.openhub.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.model.WorkflowRun;
import com.thirtydegreesray.openhub.ui.activity.base.SingleFragmentActivity;
import com.thirtydegreesray.openhub.ui.fragment.WorkflowRunDetailFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

/**
 * Shows the jobs of a single GitHub Actions workflow run.
 */
public class WorkflowRunDetailActivity extends SingleFragmentActivity<IBaseContract.Presenter, WorkflowRunDetailFragment> {

    public static void show(@NonNull Activity activity, @NonNull String owner,
                            @NonNull String repo, @NonNull WorkflowRun run) {
        Intent intent = new Intent(activity, WorkflowRunDetailActivity.class)
                .putExtras(BundleHelper.builder()
                        .put("owner", owner)
                        .put("repo", repo)
                        .put("runId", run.getId()).build());
        activity.startActivity(intent);
    }

    @AutoAccess String owner;
    @AutoAccess String repo;
    @AutoAccess long runId;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setToolbarTitle(getString(R.string.jobs), owner.concat("/").concat(repo));
        setToolbarScrollAble(true);
    }

    @Override
    protected WorkflowRunDetailFragment createFragment() {
        return WorkflowRunDetailFragment.create(owner, repo, runId);
    }
}

package com.thirtydegreesray.openhub.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.ui.activity.base.SingleFragmentActivity;
import com.thirtydegreesray.openhub.ui.fragment.WorkflowRunsFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

/**
 * Lists a repository's GitHub Actions workflow runs.
 */
public class WorkflowRunsActivity extends SingleFragmentActivity<IBaseContract.Presenter, WorkflowRunsFragment> {

    public static void show(@NonNull Activity activity, @NonNull String owner,
                            @NonNull String repo) {
        Intent intent = createIntent(activity, owner, repo);
        activity.startActivity(intent);
    }

    public static Intent createIntent(@NonNull Activity activity, @NonNull String owner,
                                      @NonNull String repo) {
        return new Intent(activity, WorkflowRunsActivity.class)
                .putExtras(BundleHelper.builder()
                        .put("owner", owner)
                        .put("repo", repo).build());
    }

    @AutoAccess String owner;
    @AutoAccess String repo;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setToolbarTitle(getString(R.string.actions), owner.concat("/").concat(repo));
        setToolbarScrollAble(true);
    }

    @Override
    protected WorkflowRunsFragment createFragment() {
        return WorkflowRunsFragment.create(owner, repo);
    }
}

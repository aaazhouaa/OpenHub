package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.IPullRequestFilesContract;
import com.thirtydegreesray.openhub.mvp.model.CommitFile;
import com.thirtydegreesray.openhub.mvp.model.CommitFilesPathModel;
import com.thirtydegreesray.openhub.mvp.presenter.PullRequestFilesPresenter;
import com.thirtydegreesray.openhub.ui.activity.ViewerActivity;
import com.thirtydegreesray.openhub.ui.adapter.CommitFilesAdapter;
import com.thirtydegreesray.openhub.ui.adapter.base.DoubleTypesModel;
import com.thirtydegreesray.openhub.ui.fragment.base.ListFragment;
import com.thirtydegreesray.openhub.util.GitHubHelper;

import java.util.ArrayList;

/**
 * Shows the changed files of a pull request, reusing the commit-files list.
 */
public class PullRequestFilesFragment extends ListFragment<PullRequestFilesPresenter, CommitFilesAdapter>
        implements IPullRequestFilesContract.View {

    public static PullRequestFilesFragment create(@NonNull String owner,
                                                  @NonNull String repoName, int number) {
        PullRequestFilesFragment fragment = new PullRequestFilesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("owner", owner);
        bundle.putString("repoName", repoName);
        bundle.putInt("number", number);
        fragment.setArguments(bundle);
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
        mPresenter.loadFiles();
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.no_file);
    }

    @Override
    public void onItemClick(int position, @NonNull View view) {
        super.onItemClick(position, view);
        DoubleTypesModel<CommitFilesPathModel, CommitFile> model = adapter.getData().get(position);
        if (model.getTypePosition() == 1) {
            CommitFile commitFile = model.getM2();
            if (GitHubHelper.isImage(commitFile.getFileName())) {
                ViewerActivity.showImage(getActivity(), commitFile.getRawUrl());
            } else {
                ViewerActivity.showForDiff(getActivity(), commitFile);
            }
        }
    }

    @Override
    public void showFiles(ArrayList<DoubleTypesModel<CommitFilesPathModel, CommitFile>> files) {
        adapter.setData(files);
        postNotifyDataSetChanged();
    }
}

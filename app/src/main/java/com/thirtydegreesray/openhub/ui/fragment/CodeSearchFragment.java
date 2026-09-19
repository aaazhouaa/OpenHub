package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.ICodeSearchContract;
import com.thirtydegreesray.openhub.mvp.model.CodeSearchItem;
import com.thirtydegreesray.openhub.mvp.model.SearchModel;
import com.thirtydegreesray.openhub.mvp.presenter.CodeSearchPresenter;
import com.thirtydegreesray.openhub.ui.activity.ViewerActivity;
import com.thirtydegreesray.openhub.ui.adapter.CodeSearchAdapter;
import com.thirtydegreesray.openhub.ui.fragment.base.ListFragment;
import com.thirtydegreesray.openhub.util.AppOpener;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;

/**
 * Shows code search results.
 */
public class CodeSearchFragment extends ListFragment<CodeSearchPresenter, CodeSearchAdapter>
        implements ICodeSearchContract.View {

    public static CodeSearchFragment createForSearch(@NonNull SearchModel searchModel) {
        CodeSearchFragment fragment = new CodeSearchFragment();
        fragment.setArguments(BundleHelper.builder()
                .put("searchModel", searchModel).build());
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
        mPresenter.searchCode(1);
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.no_code_search_result);
    }

    @Override
    public void showCodeSearchResult(ArrayList<CodeSearchItem> items) {
        adapter.setData(items);
        postNotifyDataSetChanged();
    }

    @Override
    protected void onLoadMore(int page) {
        super.onLoadMore(page);
        mPresenter.searchCode(page);
    }

    @Override
    public void onItemClick(int position, @NonNull View view) {
        super.onItemClick(position, view);
        CodeSearchItem item = adapter.getData().get(position);
        if (item.getRepository() != null && item.getRepository().getOwner() != null) {
            ViewerActivity.show(getActivity(), item.getHtmlUrl(),
                    item.getRepository().getFullName());
        } else {
            AppOpener.openInCustomTabsOrBrowser(getActivity(), item.getHtmlUrl());
        }
    }

    @Override
    public void onFragmentShowed() {
        super.onFragmentShowed();
        if (mPresenter != null) mPresenter.prepareLoadData();
    }
}

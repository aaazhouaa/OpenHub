package com.thirtydegreesray.openhub.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.thirtydegreesray.openhub.AppData;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.ui.activity.base.SingleFragmentActivity;
import com.thirtydegreesray.openhub.ui.fragment.ActivityFragment;
import com.thirtydegreesray.openhub.ui.fragment.CollectionsFragment;
import com.thirtydegreesray.openhub.ui.fragment.TopicsFragment;

public class HubContainerActivity extends SingleFragmentActivity<IBaseContract.Presenter, Fragment> {

    public enum Type {
        News, PublicNews, Collections, Topics
    }

    public static void show(@NonNull Context context, @NonNull Type type) {
        Intent intent = new Intent(context, HubContainerActivity.class);
        intent.putExtra("type", type.name());
        context.startActivity(intent);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Type type = Type.valueOf(getIntent().getStringExtra("type"));
        switch (type) {
            case News:
                setToolbarTitle(getString(R.string.news));
                break;
            case PublicNews:
                setToolbarTitle(getString(R.string.public_news));
                break;
            case Collections:
                setToolbarTitle(getString(R.string.repo_collections));
                break;
            case Topics:
                setToolbarTitle(getString(R.string.topics));
                break;
        }
    }

    @Override
    protected Fragment createFragment() {
        Type type = Type.valueOf(getIntent().getStringExtra("type"));
        String loginUser = AppData.INSTANCE.getLoggedUser() != null ?
                AppData.INSTANCE.getLoggedUser().getLogin() : "";
        switch (type) {
            case News:
                return ActivityFragment.create(ActivityFragment.ActivityType.News, loginUser);
            case PublicNews:
                return ActivityFragment.create(ActivityFragment.ActivityType.PublicNews, loginUser);
            case Collections:
                return CollectionsFragment.create();
            case Topics:
                return TopicsFragment.create();
        }
        return CollectionsFragment.create();
    }
}

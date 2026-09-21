package com.thirtydegreesray.openhub.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.util.ThemeHelper;

/**
 * Created by ThirtyDegreesRay on 2017/8/29.
 */

public class AboutActivity extends MaterialAboutActivity {

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    private boolean isAlive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isAlive = true;
        ThemeHelper.applyForAboutActivity(this);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        return new MaterialAboutList();
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.about);
    }

    @Override
    protected void onDestroy() {
        isAlive = false;
        super.onDestroy();
    }

}

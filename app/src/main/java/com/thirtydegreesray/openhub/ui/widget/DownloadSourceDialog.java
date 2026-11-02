package com.thirtydegreesray.openhub.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.mvp.model.DownloadSource;
import com.thirtydegreesray.openhub.mvp.model.Release;
import com.thirtydegreesray.openhub.mvp.model.ReleaseAsset;
import com.thirtydegreesray.openhub.ui.adapter.DownloadSourcesAdapter;
import com.thirtydegreesray.openhub.util.PrefUtils;
import com.thirtydegreesray.openhub.util.StringUtils;
import com.thirtydegreesray.openhub.util.ViewUtils;

import java.util.ArrayList;

/**
 * Created by ThirtyDegreesRay on 2017/9/16 23:21:22
 */

public class DownloadSourceDialog {

    public static void show(Context context, String repoName, String tagName, Release release){
        DownloadSourcesAdapter adapter = new DownloadSourcesAdapter(context, repoName, tagName);
        adapter.setData(getDownloadSources(context, release));

        final RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        View titleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_download_title, null);
        TextView apkFilter = titleView.findViewById(R.id.apk_filter);
        ColorStateList apkFilterDefaultColor = apkFilter.getTextColors();
        applyApkFilterColor(apkFilter, apkFilterDefaultColor, context);
        apkFilter.setOnClickListener(v -> {
            PrefUtils.set(PrefUtils.DOWNLOAD_APK_FILTER, !PrefUtils.isDownloadApkFilterEnabled());
            applyApkFilterColor(apkFilter, apkFilterDefaultColor, context);
            adapter.notifyDataSetChanged();
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle_OpenHub_Download)
                .setCancelable(true)
                .setCustomTitle(titleView)
                .setView(recyclerView)
                .setPositiveButton(R.string.close, (dialog, which) -> dialog.cancel());

        Drawable bg = builder.getBackground();
        if (bg instanceof MaterialShapeDrawable) {
            ((MaterialShapeDrawable) bg).setFillColor(
                    ColorStateList.valueOf(ViewUtils.getWindowBackground(context)));
            ((MaterialShapeDrawable) bg).setElevation(0f);
        }

        builder.show();
    }

    private static void applyApkFilterColor(TextView view, ColorStateList defaultColor, Context context) {
        if (PrefUtils.isDownloadApkFilterEnabled()) {
            view.setTextColor(ViewUtils.getAccentColor(context));
        } else {
            view.setTextColor(defaultColor);
        }
    }

    private static ArrayList<DownloadSource> getDownloadSources(Context context, Release release){
        ArrayList<DownloadSource> sources = new ArrayList<>();
        if(release.getAssets() != null){
            for(ReleaseAsset asset : release.getAssets()){
                sources.add(new DownloadSource(asset.getDownloadUrl(), false, asset.getName(),
                        asset.getSize(), asset.getDownloadCout(), asset.getCreatedAt()));
            }
        }
        if(!StringUtils.isBlank(release.getZipballUrl())){
            sources.add(new DownloadSource(release.getZipballUrl(), true,
                    context.getString(R.string.source_code_zip)));
        }
        if(!StringUtils.isBlank(release.getTarballUrl())){
            sources.add(new DownloadSource(release.getTarballUrl(), true,
                    context.getString(R.string.source_code_tar)));
        }
        return sources;
    }

}

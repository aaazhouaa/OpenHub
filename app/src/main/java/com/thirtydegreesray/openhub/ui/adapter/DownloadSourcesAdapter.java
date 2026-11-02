package com.thirtydegreesray.openhub.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.http.Downloader;
import com.thirtydegreesray.openhub.mvp.model.DownloadSource;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseAdapter;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseViewHolder;
import com.thirtydegreesray.openhub.util.AppOpener;
import com.thirtydegreesray.openhub.util.PrefUtils;
import com.thirtydegreesray.openhub.util.StringUtils;
import com.thirtydegreesray.openhub.util.ViewUtils;

import butterknife.BindView;

/**
 * Created by ThirtyDegreesRay on 2017/9/16 15:53:46
 */

public class DownloadSourcesAdapter extends BaseAdapter<DownloadSourcesAdapter.ViewHolder, DownloadSource> {

    private final String repoName;
    private final String tagName;

    public DownloadSourcesAdapter(Context context, String repoName, String tagName) {
        super(context);
        this.repoName = repoName;
        this.tagName = tagName;
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.layout_item_download_source;
    }

    @Override
    protected ViewHolder getViewHolder(View itemView, int viewType) {
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        DownloadSource model = data.get(position);
        holder.name.setText(buildName(model.getName()));

        boolean hasSize = model.getSize() != 0;
        if (hasSize) {
            holder.size.setVisibility(View.VISIBLE);
            holder.size.setText(StringUtils.getSizeString(model.getSize()));
        } else {
            holder.size.setVisibility(View.GONE);
        }

        boolean hasCreatedAt = model.getCreatedAt() != null;
        if (hasCreatedAt) {
            holder.createdAt.setVisibility(View.VISIBLE);
            holder.createdAt.setText(StringUtils.getNewsTimeStr(context, model.getCreatedAt()));
        } else {
            holder.createdAt.setVisibility(View.GONE);
        }

        boolean hasDownloadCount = model.getDownloadCount() > 0;
        if (hasDownloadCount) {
            if (holder.downloadLayout != null) {
                holder.downloadLayout.setVisibility(View.VISIBLE);
            }
            holder.downloadCount.setText(String.valueOf(model.getDownloadCount()));
        } else {
            if (holder.downloadLayout != null) {
                holder.downloadLayout.setVisibility(View.GONE);
            }
        }

        if (holder.dot1 != null) {
            holder.dot1.setVisibility(hasDownloadCount && (hasCreatedAt || hasSize) ? View.VISIBLE : View.GONE);
        }
        if (holder.dot2 != null) {
            holder.dot2.setVisibility(hasCreatedAt && hasSize ? View.VISIBLE : View.GONE);
        }

        if (holder.divider != null) {
            holder.divider.setVisibility(position == getItemCount() - 1 ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 开启 apk 筛选时，.apk 文件的整个文件名用主题色；否则只高亮后缀。
     */
    private CharSequence buildName(@Nullable String name) {
        if (StringUtils.isBlank(name)) return name;
        int accent = ViewUtils.getAccentColor(context);
        SpannableString spannable = new SpannableString(name);
        if (PrefUtils.isDownloadApkFilterEnabled()
                && name.toLowerCase().endsWith(".apk")) {
            spannable.setSpan(new ForegroundColorSpan(accent),
                    0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable;
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return name;
        spannable.setSpan(new ForegroundColorSpan(accent),
                dot, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    class ViewHolder extends BaseViewHolder {

        @BindView(R2.id.name) TextView name;
        @BindView(R2.id.size) TextView size;
        @BindView(R2.id.download_count) TextView downloadCount;
        @BindView(R2.id.created_at) TextView createdAt;
        @Nullable View downloadLayout;
        @Nullable View dot1;
        @Nullable View dot2;
        @Nullable View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadLayout = itemView.findViewById(R.id.download_layout);
            dot1 = itemView.findViewById(R.id.dot_1);
            dot2 = itemView.findViewById(R.id.dot_2);
            divider = itemView.findViewById(R.id.divider);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && data != null && position < data.size()) {
                    DownloadSource source = data.get(position);
                    String fileName = repoName.concat("-").concat(tagName)
                            .concat("-").concat(source.getName());
                    AppOpener.startDownload(context, source.getUrl(), fileName);
                }
            });
        }

    }

}

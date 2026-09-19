package com.thirtydegreesray.openhub.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.mvp.model.CodeSearchItem;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseAdapter;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseViewHolder;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Lists code search results.
 */
public class CodeSearchAdapter extends BaseAdapter<CodeSearchAdapter.ViewHolder, CodeSearchItem> {

    @Inject
    public CodeSearchAdapter(Context context, BaseFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.layout_item_code_search;
    }

    @Override
    protected ViewHolder getViewHolder(View itemView, int viewType) {
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        CodeSearchItem item = data.get(position);
        holder.path.setText(item.getPath());
        if (item.getRepository() != null) {
            holder.repoFullName.setText(item.getRepository().getFullName());
        } else {
            holder.repoFullName.setText("");
        }
    }

    class ViewHolder extends BaseViewHolder {
        @BindView(R2.id.path) TextView path;
        @BindView(R2.id.repo_full_name) TextView repoFullName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

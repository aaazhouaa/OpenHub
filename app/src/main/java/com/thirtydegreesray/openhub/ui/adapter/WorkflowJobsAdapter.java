package com.thirtydegreesray.openhub.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.View;
import android.widget.TextView;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.mvp.model.WorkflowJob;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseAdapter;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseViewHolder;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;
import com.thirtydegreesray.openhub.util.StringUtils;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Lists the jobs of a workflow run.
 */
public class WorkflowJobsAdapter extends BaseAdapter<WorkflowJobsAdapter.ViewHolder, WorkflowJob> {

    @Inject
    public WorkflowJobsAdapter(Context context, BaseFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.layout_item_workflow_job;
    }

    @Override
    protected ViewHolder getViewHolder(View itemView, int viewType) {
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        WorkflowJob job = data.get(position);

        holder.name.setText(job.getName());
        if (job.getCompletedAt() != null) {
            holder.time.setText(StringUtils.getNewsTimeStr(context, job.getCompletedAt()));
        } else if (job.getStartedAt() != null) {
            holder.time.setText(StringUtils.getNewsTimeStr(context, job.getStartedAt()));
        } else {
            holder.time.setText("");
        }

        String statusText;
        int statusColor;
        if ("completed".equals(job.getStatus()) && job.getConclusion() != null) {
            statusText = job.getConclusion();
            statusColor = conclusionColor(job.getConclusion());
        } else {
            statusText = job.getStatus();
            statusColor = ContextCompat.getColor(context, R.color.material_grey_500);
        }
        holder.status.setText(statusText);
        holder.statusIcon.setImageTintList(ColorStateList.valueOf(statusColor));
    }

    private int conclusionColor(String conclusion) {
        switch (conclusion) {
            case "success":
                return ContextCompat.getColor(context, R.color.commit_file_added_color);
            case "failure":
            case "timed_out":
                return ContextCompat.getColor(context, R.color.material_red_800);
            case "cancelled":
            case "skipped":
                return ContextCompat.getColor(context, R.color.material_grey_500);
            default:
                return ContextCompat.getColor(context, R.color.material_amber_700);
        }
    }

    class ViewHolder extends BaseViewHolder {
        @BindView(R2.id.status_icon) AppCompatImageView statusIcon;
        @BindView(R2.id.name) TextView name;
        @BindView(R2.id.status) TextView status;
        @BindView(R2.id.time) TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

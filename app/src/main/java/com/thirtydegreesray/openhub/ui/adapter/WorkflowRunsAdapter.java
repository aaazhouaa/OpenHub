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
import com.thirtydegreesray.openhub.mvp.model.WorkflowRun;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseAdapter;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseViewHolder;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;
import com.thirtydegreesray.openhub.util.StringUtils;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Lists GitHub Actions workflow runs.
 */
public class WorkflowRunsAdapter extends BaseAdapter<WorkflowRunsAdapter.ViewHolder, WorkflowRun> {

    @Inject
    public WorkflowRunsAdapter(Context context, BaseFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.layout_item_workflow_run;
    }

    @Override
    protected ViewHolder getViewHolder(View itemView, int viewType) {
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        WorkflowRun run = data.get(position);

        String title = run.getDisplayTitle();
        if (StringUtils.isBlank(title)) {
            title = run.getName();
        }
        holder.title.setText(title);
        holder.headBranch.setText(run.getHeadBranch());
        holder.event.setText(run.getEvent());
        holder.time.setText(StringUtils.getNewsTimeStr(context, run.getCreatedAt()));
        holder.runNumber.setText("#".concat(String.valueOf(run.getRunNumber())));

        int statusColor;
        String statusText;
        if ("completed".equals(run.getStatus()) && run.getConclusion() != null) {
            statusText = run.getConclusion();
            statusColor = getConclusionColor(run.getConclusion());
        } else {
            statusText = run.getStatus();
            statusColor = ContextCompat.getColor(context, R.color.material_grey_500);
        }
        holder.status.setText(statusText);
        holder.statusIcon.setImageTintList(ColorStateList.valueOf(statusColor));
    }

    private int getConclusionColor(String conclusion) {
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
        @BindView(R2.id.title) TextView title;
        @BindView(R2.id.status) TextView status;
        @BindView(R2.id.head_branch) TextView headBranch;
        @BindView(R2.id.event) TextView event;
        @BindView(R2.id.run_number) TextView runNumber;
        @BindView(R2.id.time) TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

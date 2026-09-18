package com.thirtydegreesray.openhub.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.util.SearchRecordHelper;

import java.util.List;

/**
 * Drop-down adapter for the recent search queries. Tapping an item fills the query
 * box, long-pressing it offers to delete the record.
 */
public class SearchRecordAdapter extends ArrayAdapter<String> {

    public interface OnRecordClickListener {
        void onRecordClick(String record);
    }

    private OnRecordClickListener clickListener;

    public SearchRecordAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, R.layout.layout_search_history_item, objects);
    }

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        clickListener = listener;
    }

    /**
     * Re-reads the persisted records, e.g. after a query has been submitted.
     */
    public void reload() {
        clear();
        addAll(SearchRecordHelper.getRecords());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final String record = getItem(position);
        view.setOnClickListener(v -> {
            if (clickListener != null && record != null) {
                clickListener.onRecordClick(record);
            }
        });
        view.setOnLongClickListener(v -> {
            if (record != null) {
                confirmRemoveRecord(record);
            }
            return true;
        });
        return view;
    }

    private void confirmRemoveRecord(@NonNull String record) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.warning_dialog_tile)
                .setMessage(R.string.delete_search_record_confirm)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    SearchRecordHelper.removeRecord(record);
                    reload();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

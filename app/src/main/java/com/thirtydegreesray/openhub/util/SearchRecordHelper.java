package com.thirtydegreesray.openhub.util;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Persists the recent search queries that back the search-history drop-down.
 * <p>
 * Records live in a single preference string joined by "$$"; {@link SearchPresenter}
 * and the in-page toolbar search share this class so both read and write the same list.
 */
public final class SearchRecordHelper {

    private static final int MAX_RECORD_SIZE = 30;
    private static final String SEPARATOR = "$$";
    private static final String SEPARATOR_REGEX = "\\$\\$";

    private SearchRecordHelper() {
    }

    /**
     * @return the stored records, most recent first. Never null.
     */
    @NonNull
    public static ArrayList<String> getRecords() {
        String records = PrefUtils.getSearchRecords();
        ArrayList<String> recordList = new ArrayList<>();
        if (!StringUtils.isBlank(records)) {
            Collections.addAll(recordList, records.split(SEPARATOR_REGEX));
        }
        return recordList;
    }

    /**
     * Moves the record to the front, dropping the oldest one when the list is full.
     */
    public static void addRecord(@NonNull String record) {
        if (record.contains("$")) return;
        ArrayList<String> recordList = getRecords();
        recordList.remove(record);
        if (recordList.size() >= MAX_RECORD_SIZE) {
            recordList.remove(recordList.size() - 1);
        }
        recordList.add(0, record);
        save(recordList);
    }

    public static void removeRecord(@NonNull String record) {
        ArrayList<String> recordList = getRecords();
        recordList.remove(record);
        save(recordList);
    }

    private static void save(@NonNull ArrayList<String> recordList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < recordList.size(); i++) {
            if (i > 0) builder.append(SEPARATOR);
            builder.append(recordList.get(i));
        }
        PrefUtils.set(PrefUtils.SEARCH_RECORDS, builder.toString());
    }
}

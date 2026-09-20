
package com.thirtydegreesray.openhub.ui.activity.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AutoCompleteTextView;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.mvp.model.BookmarkExt;
import com.thirtydegreesray.openhub.mvp.model.Repository;
import com.thirtydegreesray.openhub.mvp.model.TraceExt;
import com.thirtydegreesray.openhub.mvp.model.User;
import com.thirtydegreesray.openhub.ui.activity.SearchActivity;
import com.thirtydegreesray.openhub.ui.adapter.SearchRecordAdapter;
import com.thirtydegreesray.openhub.ui.fragment.BookmarksFragment;
import com.thirtydegreesray.openhub.ui.fragment.RepositoriesFragment;
import com.thirtydegreesray.openhub.ui.fragment.TraceFragment;
import com.thirtydegreesray.openhub.ui.fragment.UserListFragment;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;
import com.thirtydegreesray.openhub.util.SearchRecordHelper;

import java.util.ArrayList;
import java.util.Locale;
import java.util.WeakHashMap;

/**
 * In-page search for repo/user list pages. Typing filters the currently
 * visible list in place; submitting jumps to global GitHub search. Clearing
 * the query restores the unfiltered list.
 */
public final class PageSearchHelper {

    private static final WeakHashMap<BaseFragment, ArrayList<?>> ORIGINAL_DATA = new WeakHashMap<>();

    private PageSearchHelper() {
    }

    public static void attach(@NonNull BaseActivity activity, @NonNull SearchView searchView) {
        searchView.setQueryHint(activity.getString(R.string.search));
        searchView.setIconifiedByDefault(true);
        SearchRecordAdapter historyAdapter = attachHistoryDropDown(activity, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if (query != null && query.trim().length() > 0) {
                    // Recorded here as well as in the search page; addRecord de-duplicates,
                    // and reloading keeps the query in the drop-down of this page.
                    SearchRecordHelper.addRecord(query.trim());
                    if (historyAdapter != null) historyAdapter.reload();
                    SearchActivity.show(activity, query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyLocalSearch(activity, newText);
                return true;
            }
        });
    }

    /**
     * Attaches the recent-search drop-down to the toolbar search box, so every page
     * whose action bar carries a search can pop its history, not just the search page.
     *
     * @return the adapter, or null when the search view exposes no text field
     */
    public static SearchRecordAdapter attachHistoryDropDown(@NonNull Activity activity,
                                                            @NonNull SearchView searchView) {
        View srcTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (!(srcTextView instanceof AutoCompleteTextView)) return null;
        AutoCompleteTextView srcText = (AutoCompleteTextView) srcTextView;

        SearchRecordAdapter adapter = new SearchRecordAdapter(activity, new ArrayList<>());
        adapter.setOnRecordClickListener(record -> {
            srcText.setText(record);
            srcText.setSelection(record.length());
            srcText.dismissDropDown();
        });
        srcText.setThreshold(0);
        srcText.setAdapter(adapter);
        srcText.setDropDownBackgroundResource(R.drawable.bg_search_history_rounded);
        // Tapping the magnifier is what expands the box, and it does not reliably hand
        // focus to the text field (the toolbar may keep it), so listen for that click
        // directly instead of waiting for a focus change. Focus stays as a fallback
        // for tapping the box itself once it is already open.
        searchView.setOnSearchClickListener(v -> showHistory(srcText, adapter));
        srcText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showHistory(srcText, adapter);
        });

        final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastImeHeight = -1;
            private int lastDisplayBottom = -1;

            @Override
            public void onGlobalLayout() {
                if (!srcText.isAttachedToWindow()) {
                    srcText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    return;
                }
                if (srcText.isPopupShowing()) {
                    Rect frame = new Rect();
                    srcText.getWindowVisibleDisplayFrame(frame);
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(srcText);
                    int ime = insets != null ? insets.getInsets(WindowInsetsCompat.Type.ime()).bottom : 0;
                    if (ime != lastImeHeight || frame.bottom != lastDisplayBottom) {
                        lastImeHeight = ime;
                        lastDisplayBottom = frame.bottom;
                        adjustDropDownSize(srcText, adapter);
                        srcText.showDropDown();
                    }
                }
            }
        };
        srcText.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        srcText.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                srcText.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
            }
        });

        if (srcText.hasFocus()) showHistory(srcText, adapter);
        return adapter;
    }

    /**
     * Dynamically adjusts the dropdown size (both width and height) to fit between the search
     * anchor and the soft keyboard, with a consistent full-width margin across first and
     * subsequent taps.
     */
    public static void adjustDropDownSize(@NonNull AutoCompleteTextView srcText,
                                          @NonNull SearchRecordAdapter adapter) {
        int count = adapter.getCount();
        if (count == 0) return;

        Context context = srcText.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        int itemHeight = (int) (48 * density);
        int paddingVertical = (int) (16 * density);
        int totalContentHeight = count * itemHeight + paddingVertical;

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        // Unify width: align symmetrically with 8dp margin on each side of the screen
        int marginHorizontal = (int) (8 * density);
        int targetWidth = screenWidth - 2 * marginHorizontal;
        srcText.setDropDownWidth(targetWidth);

        // Unify horizontal offset so dropdown left edge starts at marginHorizontal on screen
        int[] loc = new int[2];
        srcText.getLocationOnScreen(loc);
        int targetOffset = marginHorizontal - loc[0];
        srcText.setDropDownHorizontalOffset(targetOffset);

        // Unify height calculation
        Rect displayFrame = new Rect();
        srcText.getWindowVisibleDisplayFrame(displayFrame);

        int imeHeight = 0;
        WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(srcText);
        if (rootInsets != null) {
            imeHeight = rootInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
        }

        int windowHeight = srcText.getRootView().getHeight();
        if (windowHeight <= 0) {
            windowHeight = screenHeight;
        }

        int keyboardTop = displayFrame.bottom;
        if (imeHeight > 0 && displayFrame.bottom >= windowHeight - (imeHeight / 2)) {
            keyboardTop = displayFrame.bottom - imeHeight;
        }

        View anchor = srcText;
        int anchorId = srcText.getDropDownAnchor();
        if (anchorId != View.NO_ID && context instanceof Activity) {
            View customAnchor = ((Activity) context).findViewById(anchorId);
            if (customAnchor != null) anchor = customAnchor;
        }

        int[] anchorLoc = new int[2];
        anchor.getLocationOnScreen(anchorLoc);
        int anchorBottom = anchorLoc[1] + anchor.getHeight();

        // Baseline maximum height: 38% of window height
        int unifiedMaxHeight = (int) (windowHeight * 0.38f);

        // If keyboard is already visible, cap by space above keyboard
        if (imeHeight > 0 && keyboardTop > anchorBottom) {
            int availableToKeyboard = keyboardTop - anchorBottom - (int) (8 * density);
            if (availableToKeyboard > 0) {
                unifiedMaxHeight = Math.min(unifiedMaxHeight, availableToKeyboard);
            }
        }

        unifiedMaxHeight = Math.max(itemHeight + paddingVertical, unifiedMaxHeight);

        if (totalContentHeight <= unifiedMaxHeight) {
            srcText.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            srcText.setDropDownHeight(unifiedMaxHeight);
        }
    }

    /**
     * Reloads the stored records and pops the drop-down. Uses post() so the layout
     * pass (especially the first expand of the search view) is completed, ensuring
     * identical dimensions on first and subsequent taps.
     */
    public static void showHistory(@Nullable AutoCompleteTextView srcText,
                                   @Nullable SearchRecordAdapter adapter) {
        if (srcText == null || adapter == null) return;
        adapter.reload();
        if (adapter.isEmpty()) return;

        srcText.post(() -> {
            if (!srcText.isAttachedToWindow()) return;
            adjustDropDownSize(srcText, adapter);
            srcText.showDropDown();
        });
    }

    private static void applyLocalSearch(@NonNull BaseActivity activity, String query) {
        BaseFragment fragment = getVisibleBaseFragment(activity);
        if (fragment == null) return;
        boolean emptyQuery = query == null || query.trim().length() == 0;
        if (fragment instanceof RepositoriesFragment) {
            RepositoriesFragment f = (RepositoriesFragment) fragment;
            ArrayList<Repository> data = f.getRepositoriesAdapter().getData();
            if (emptyQuery) {
                f.showRepositories(restoreOriginal(fragment, data));
            } else {
                ArrayList<Repository> source = sourceOf(fragment, data);
                ArrayList<Repository> result = new ArrayList<>();
                if (source != null) {
                    for (Repository repo : source) {
                        if (matches(repo.getName(), repo.getDescription(), query)) result.add(repo);
                    }
                }
                f.showRepositories(result);
            }
        } else if (fragment instanceof UserListFragment) {
            UserListFragment f = (UserListFragment) fragment;
            ArrayList<User> data = f.getUsersAdapter().getData();
            if (emptyQuery) {
                f.showUsers(restoreOriginal(fragment, data));
            } else {
                ArrayList<User> source = sourceOf(fragment, data);
                ArrayList<User> result = new ArrayList<>();
                if (source != null) {
                    for (User user : source) {
                        if (matches(user.getLogin(), user.getName(), query)) result.add(user);
                    }
                }
                f.showUsers(result);
            }
        } else if (fragment instanceof BookmarksFragment) {
            BookmarksFragment f = (BookmarksFragment) fragment;
            ArrayList<BookmarkExt> data = f.getBookmarksAdapter().getData();
            if (emptyQuery) {
                f.showBookmarks(restoreOriginal(fragment, data));
            } else {
                ArrayList<BookmarkExt> source = sourceOf(fragment, data);
                ArrayList<BookmarkExt> result = new ArrayList<>();
                if (source != null) {
                    for (BookmarkExt bookmark : source) {
                        if ("user".equals(bookmark.getType())) {
                            if (bookmark.getUser() != null && matches(
                                    bookmark.getUser().getLogin(), bookmark.getUser().getName(), query)) {
                                result.add(bookmark);
                            }
                        } else if (bookmark.getRepository() != null && matches(
                                bookmark.getRepository().getName(),
                                bookmark.getRepository().getDescription(), query)) {
                            result.add(bookmark);
                        }
                    }
                }
                f.showBookmarks(result);
            }
        } else if (fragment instanceof TraceFragment) {
            TraceFragment f = (TraceFragment) fragment;
            ArrayList<TraceExt> data = f.getTraceAdapter().getData();
            if (emptyQuery) {
                f.showTraceList(restoreOriginal(fragment, data));
            } else {
                ArrayList<TraceExt> source = sourceOf(fragment, data);
                ArrayList<TraceExt> result = new ArrayList<>();
                if (source != null) {
                    for (TraceExt trace : source) {
                        if ("user".equals(trace.getType())) {
                            if (trace.getUser() != null && matches(
                                    trace.getUser().getLogin(), trace.getUser().getName(), query)) {
                                result.add(trace);
                            }
                        } else if (trace.getRepository() != null && matches(
                                trace.getRepository().getName(),
                                trace.getRepository().getDescription(), query)) {
                            result.add(trace);
                        }
                    }
                }
                f.showTraceList(result);
            }
        }
    }

    /**
     * Returns the unfiltered list for the fragment, caching the adapter's
     * data the first time so every keystroke filters from the full list.
     */
    @SuppressWarnings("unchecked")
    private static <T> ArrayList<T> sourceOf(@NonNull BaseFragment fragment,
                                             ArrayList<T> adapterData) {
        ArrayList<T> cached = (ArrayList<T>) ORIGINAL_DATA.get(fragment);
        if (cached != null) return cached;
        if (adapterData != null) ORIGINAL_DATA.put(fragment, adapterData);
        return adapterData;
    }

    @SuppressWarnings("unchecked")
    private static <T> ArrayList<T> restoreOriginal(@NonNull BaseFragment fragment,
                                                    ArrayList<T> current) {
        ArrayList<T> original = (ArrayList<T>) ORIGINAL_DATA.remove(fragment);
        return original != null ? original : current;
    }

    private static boolean matches(String name, String description, String query) {
        if (query == null || query.trim().length() == 0) return true;
        String q = query.trim().toLowerCase(Locale.getDefault());
        if (name != null && name.toLowerCase(Locale.getDefault()).contains(q)) return true;
        return description != null
                && description.toLowerCase(Locale.getDefault()).contains(q);
    }

    private static BaseFragment getVisibleBaseFragment(@NonNull BaseActivity activity) {
        androidx.fragment.app.Fragment fragment = activity.getVisibleFragment();
        if (fragment instanceof BaseFragment) {
            return (BaseFragment) fragment;
        }
        return null;
    }

}

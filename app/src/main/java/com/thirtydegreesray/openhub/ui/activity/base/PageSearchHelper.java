
package com.thirtydegreesray.openhub.ui.activity.base;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.mvp.model.BookmarkExt;
import com.thirtydegreesray.openhub.mvp.model.Repository;
import com.thirtydegreesray.openhub.mvp.model.TraceExt;
import com.thirtydegreesray.openhub.mvp.model.User;
import com.thirtydegreesray.openhub.ui.activity.SearchActivity;
import com.thirtydegreesray.openhub.ui.fragment.BookmarksFragment;
import com.thirtydegreesray.openhub.ui.fragment.RepositoriesFragment;
import com.thirtydegreesray.openhub.ui.fragment.TraceFragment;
import com.thirtydegreesray.openhub.ui.fragment.UserListFragment;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;

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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if (query != null && query.trim().length() > 0) {
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

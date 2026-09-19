package com.thirtydegreesray.openhub.mvp.model.filter;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.ui.fragment.RepositoriesFragment;
import com.thirtydegreesray.openhub.util.PrefUtils;
import com.thirtydegreesray.openhub.util.ViewUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ThirtyDegreesRay on 2017/11/9 16:30:13
 */

public class RepositoriesFilter implements Parcelable {

    private final static String PREF_SORT_OWNED = "repositoriesFilterSortOwned";
    private final static String PREF_SORT_STARRED = "repositoriesFilterSortStarred";

    private enum Type{
        All, Owner, Public, Private, Member
    }

    private enum Sort{
        Created, Updated, Pushed, Full_name
    }

    private final static Map<Integer, Type> TYPE_RELATION = new HashMap<>();
    static {
        TYPE_RELATION.put(R.id.nav_all, Type.All);
        TYPE_RELATION.put(R.id.nav_owner, Type.Owner);
        TYPE_RELATION.put(R.id.nav_public, Type.Public);
        TYPE_RELATION.put(R.id.nav_private, Type.Private);
        TYPE_RELATION.put(R.id.nav_member, Type.Member);
    }

    private Type type = Type.All;
    private Sort sort = Sort.Full_name;
    private SortDirection sortDirection = SortDirection.Asc;

    public static RepositoriesFilter generateFromDrawer(@NonNull NavigationView navView,
                                                        @NonNull RepositoriesFragment.RepositoriesType type){
        RepositoriesFilter filter = new RepositoriesFilter();
        MenuItem typeItem = ViewUtils.getSelectedMenu(navView.getMenu().findItem(R.id.nav_type_chooser));
        if (typeItem != null){
            filter.type = TYPE_RELATION.get(typeItem.getItemId());
        }

        MenuItem sortItem = ViewUtils.getSelectedMenu(navView.getMenu().findItem(R.id.nav_sort));
        saveSort(type, sortItem);
        if(sortItem != null){
            applySortId(filter, sortItem.getItemId());
        }

        return filter;
    }

    public static void initDrawer(NavigationView navView,
                                  @NonNull RepositoriesFragment.RepositoriesType type){
        if(navView == null) return;
        if(RepositoriesFragment.RepositoriesType.OWNED.equals(type)){
            restoreSavedSort(navView, type, R.id.nav_full_name_asc);
        } else if(RepositoriesFragment.RepositoriesType.PUBLIC.equals(type)){
            navView.getMenu().findItem(R.id.nav_private).setVisible(false);
            navView.getMenu().findItem(R.id.nav_public).setVisible(false);
            restoreSavedSort(navView, type, R.id.nav_full_name_asc);
        } else if(RepositoriesFragment.RepositoriesType.STARRED.equals(type)){
            navView.getMenu().findItem(R.id.nav_type_chooser).setVisible(false);
            navView.getMenu().findItem(R.id.nav_full_name_asc).setVisible(false);
            navView.getMenu().findItem(R.id.nav_full_name_desc).setVisible(false);
            navView.getMenu().findItem(R.id.nav_most_pushed).setVisible(false);
            navView.getMenu().findItem(R.id.nav_fewest_pushed).setVisible(false);

            navView.getMenu().findItem(R.id.nav_recently_created).setTitle(R.string.recently_starred);
            navView.getMenu().findItem(R.id.nav_previously_created).setTitle(R.string.previously_starred);

            restoreSavedSort(navView, type, R.id.nav_recently_created);
        }
    }

    private static void saveSort(@NonNull RepositoriesFragment.RepositoriesType type, MenuItem sortItem) {
        if (!isSortPersisted(type) || sortItem == null) return;
        PrefUtils.set(getSortPrefKey(type), sortItem.getItemId());
    }

    private static void applySortId(@NonNull RepositoriesFilter filter, int sortItemId) {
        switch (sortItemId) {
            case R.id.nav_full_name_asc:
                filter.sort = Sort.Full_name;
                filter.sortDirection = SortDirection.Asc;
                break;
            case R.id.nav_full_name_desc:
                filter.sort = Sort.Full_name;
                filter.sortDirection = SortDirection.Desc;
                break;
            case R.id.nav_recently_created:
                filter.sort = Sort.Created;
                filter.sortDirection = SortDirection.Desc;
                break;
            case R.id.nav_previously_created:
                filter.sort = Sort.Created;
                filter.sortDirection = SortDirection.Asc;
                break;
            case R.id.nav_recently_updated:
                filter.sort = Sort.Updated;
                filter.sortDirection = SortDirection.Desc;
                break;
            case R.id.nav_least_recently_updated:
                filter.sort = Sort.Updated;
                filter.sortDirection = SortDirection.Asc;
                break;
            case R.id.nav_most_pushed:
                filter.sort = Sort.Pushed;
                filter.sortDirection = SortDirection.Desc;
                break;
            case R.id.nav_fewest_pushed:
                filter.sort = Sort.Pushed;
                filter.sortDirection = SortDirection.Asc;
                break;
            default:
                break;
        }
    }

    public static RepositoriesFilter getDefault(@NonNull RepositoriesFragment.RepositoriesType type) {
        int defaultSortId = RepositoriesFragment.RepositoriesType.STARRED.equals(type)
                ? R.id.nav_recently_created : R.id.nav_full_name_asc;
        int sortId = defaultSortId;
        if (isSortPersisted(type)) {
            int savedId = getSavedSortId(type);
            if (savedId != 0) sortId = savedId;
        }
        RepositoriesFilter filter = new RepositoriesFilter();
        applySortId(filter, sortId);
        return filter;
    }

    private static boolean isSortPersisted(RepositoriesFragment.RepositoriesType type) {
        return RepositoriesFragment.RepositoriesType.OWNED.equals(type)
                || RepositoriesFragment.RepositoriesType.STARRED.equals(type);
    }

    private static String getSortPrefKey(RepositoriesFragment.RepositoriesType type) {
        return RepositoriesFragment.RepositoriesType.STARRED.equals(type)
                ? PREF_SORT_STARRED : PREF_SORT_OWNED;
    }

    private static int getSavedSortId(RepositoriesFragment.RepositoriesType type) {
        return PrefUtils.getDefaultSp().getInt(getSortPrefKey(type), 0);
    }

    private static void restoreSavedSort(@NonNull NavigationView navView,
                                         @NonNull RepositoriesFragment.RepositoriesType type,
                                         int defaultSortId) {
        MenuItem defaultChecked = navView.getMenu().findItem(R.id.nav_full_name_asc);
        if (defaultChecked != null) defaultChecked.setChecked(false);

        int sortId = defaultSortId;
        if (isSortPersisted(type)) {
            int savedId = getSavedSortId(type);
            if (savedId != 0) sortId = savedId;
        }
        MenuItem target = navView.getMenu().findItem(sortId);
        if (target == null || !target.isVisible()) {
            target = navView.getMenu().findItem(defaultSortId);
        }
        if (target != null) target.setChecked(true);
    }

    public String getType() {
        return type.name().toLowerCase();
    }

    public String getSort() {
        return sort.name().toLowerCase();
    }

    public String getSortDirection() {
        return sortDirection.name().toLowerCase();
    }

    private RepositoriesFilter setType(Type type) {
        this.type = type;
        return this;
    }

    private RepositoriesFilter setSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    private RepositoriesFilter setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeInt(this.sort == null ? -1 : this.sort.ordinal());
        dest.writeInt(this.sortDirection == null ? -1 : this.sortDirection.ordinal());
    }

    public RepositoriesFilter() {
    }

    protected RepositoriesFilter(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : Type.values()[tmpType];
        int tmpSort = in.readInt();
        this.sort = tmpSort == -1 ? null : Sort.values()[tmpSort];
        int tmpSortDirection = in.readInt();
        this.sortDirection = tmpSortDirection == -1 ? null : SortDirection.values()[tmpSortDirection];
    }

    public static final Parcelable.Creator<RepositoriesFilter> CREATOR = new Parcelable.Creator<RepositoriesFilter>() {
        @Override
        public RepositoriesFilter createFromParcel(Parcel source) {
            return new RepositoriesFilter(source);
        }

        @Override
        public RepositoriesFilter[] newArray(int size) {
            return new RepositoriesFilter[size];
        }
    };

}

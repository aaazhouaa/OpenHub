package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.thirtydegreesray.openhub.AppData;
import com.thirtydegreesray.openhub.R;

public class StarredReposPagerFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static StarredReposPagerFragment create() {
        return new StarredReposPagerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_starred_pager, container, false);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        StarredPagerAdapter adapter = new StarredPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        com.thirtydegreesray.openhub.util.ViewUtils.alignTabContentBottom(tabLayout);

        return view;
    }

    private class StarredPagerAdapter extends FragmentPagerAdapter {

        private final int[] titleRes = new int[]{
                R.string.recently_starred,
                R.string.recently_updated,
                R.string.previously_starred,
                R.string.least_recently_updated
        };

        private final int[] sortIds = new int[]{
                R.id.nav_recently_created,
                R.id.nav_recently_updated,
                R.id.nav_previously_created,
                R.id.nav_least_recently_updated
        };

        StarredPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return titleRes.length;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            String user = AppData.INSTANCE.getLoggedUser() != null ?
                    AppData.INSTANCE.getLoggedUser().getLogin() : "";
            return RepositoriesFragment.create(
                    RepositoriesFragment.RepositoriesType.STARRED,
                    user,
                    sortIds[position]
            );
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return getString(titleRes[position]);
        }
    }
}

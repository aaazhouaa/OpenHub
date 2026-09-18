package com.thirtydegreesray.openhub.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.ui.activity.base.BaseActivity;
import com.thirtydegreesray.openhub.ui.adapter.ImagePagerAdapter;
import com.thirtydegreesray.openhub.util.AppOpener;
import com.thirtydegreesray.openhub.util.AppUtils;
import com.thirtydegreesray.openhub.util.BundleHelper;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Full-screen image viewer with left/right paging, opened from a page that holds
 * several images (e.g. a repository README). Each page reuses the zoomable
 * {@link com.thirtydegreesray.openhub.ui.widget.webview.CodeWebView} image view.
 */
public class ImageGalleryActivity extends BaseActivity<IBaseContract.Presenter> {

    private static final String EXTRA_IMAGE_URLS = "imageUrls";
    private static final String EXTRA_CURRENT_INDEX = "currentIndex";

    public static void show(@NonNull Context context, @NonNull ArrayList<String> imageUrls,
                            int currentIndex) {
        if (imageUrls.isEmpty()) return;
        Intent intent = new Intent(context, ImageGalleryActivity.class);
        intent.putExtras(BundleHelper.builder()
                .putStringList(EXTRA_IMAGE_URLS, imageUrls)
                .put(EXTRA_CURRENT_INDEX, currentIndex)
                .build());
        context.startActivity(intent);
    }

    @AutoAccess ArrayList<String> imageUrls;
    @AutoAccess int currentIndex;

    @BindView(R2.id.image_pager) ViewPager2 imagePager;

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_image_gallery;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setTransparentStatusBar();
        setToolbarBackEnable();
        setToolbarScrollAble(false);

        if (imageUrls == null || imageUrls.isEmpty()) {
            finish();
            return;
        }
        if (currentIndex < 0 || currentIndex >= imageUrls.size()) currentIndex = 0;

        imagePager.setAdapter(new ImagePagerAdapter(imageUrls));
        imagePager.setCurrentItem(currentIndex, false);
        imagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentIndex = position;
                updateTitle();
                invalidateOptionsMenu();
            }
        });
        updateTitle();
    }

    private void updateTitle() {
        String url = getCurrentImageUrl();
        String name = StringUtils.isBlank(url) ? "" : url.substring(url.lastIndexOf("/") + 1,
                url.contains("?") ? url.indexOf("?") : url.length());
        // Position indicator is language-neutral, so it stays out of the translations.
        setToolbarTitle(name, (currentIndex + 1) + " / " + imageUrls.size());
    }

    @Nullable
    private String getCurrentImageUrl() {
        if (imageUrls == null || currentIndex < 0 || currentIndex >= imageUrls.size()) return null;
        return imageUrls.get(currentIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem fullscreen = menu.findItem(R.id.action_fullscreen);
        MenuItem refresh = menu.findItem(R.id.action_refresh);
        MenuItem wrap = menu.findItem(R.id.action_wrap_lines);
        MenuItem viewFile = menu.findItem(R.id.action_view_file);
        MenuItem download = menu.findItem(R.id.action_download);
        if (fullscreen != null) fullscreen.setVisible(true);
        if (refresh != null) refresh.setVisible(false);
        if (wrap != null) wrap.setVisible(false);
        if (viewFile != null) viewFile.setVisible(false);
        if (download != null) download.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_fullscreen) {
            intoFullScreen();
            return true;
        }
        String url = getCurrentImageUrl();
        if (StringUtils.isBlank(url)) return super.onOptionsItemSelected(item);
        if (itemId == R.id.action_open_in_browser) {
            AppOpener.openInCustomTabsOrBrowser(getActivity(), url);
            return true;
        } else if (itemId == R.id.action_share) {
            AppOpener.shareText(getActivity(), url);
            return true;
        } else if (itemId == R.id.action_copy_url) {
            AppUtils.copyToClipboard(getActivity(), url);
            return true;
        } else if (itemId == R.id.action_download) {
            String fileName = url.substring(url.lastIndexOf("/") + 1,
                    url.contains("?") ? url.indexOf("?") : url.length());
            AppOpener.startDownload(getActivity(), url, fileName);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

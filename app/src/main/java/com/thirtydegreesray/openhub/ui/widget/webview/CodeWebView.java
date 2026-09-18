

package com.thirtydegreesray.openhub.ui.widget.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.util.AppOpener;
import com.thirtydegreesray.openhub.util.AppUtils;
import com.thirtydegreesray.openhub.util.PrefUtils;
import com.thirtydegreesray.openhub.util.StringUtils;
import com.thirtydegreesray.openhub.util.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created on 2017/8/20 12:10:56
 * Copied from Copyright (C) 2017 Kosh.
 * Modified by Copyright (C) 2017 ThirtyDegreesRay.
 */

public class CodeWebView extends WebView {

    private ContentChangedListener contentChangedListener;
    private OnImageClickListener imageClickListener;
    private int backgroundColor ;

    public interface ContentChangedListener {
        void onContentChanged(int progress);

        void onScrollChanged(boolean reachedTop, int scroll);
    }

    /**
     * Notified when the user taps an image of the loaded page, with every viewable
     * image of that page so the caller can offer left/right paging.
     */
    public interface OnImageClickListener {
        void onImageClick(@NonNull ArrayList<String> imageUrls, int index);
    }

    /**
     * Reports image taps from the page; only URLs whose anchor navigates elsewhere
     * (badges and the like) are skipped, so tapping those keeps following the link.
     */
    private static final String IMAGE_CLICK_SCRIPT = "(function(){"
            + "if (window.__openhubImageHook) return; window.__openhubImageHook = true;"
            + "function viewable(im){var a = im.closest ? im.closest('a') : null;"
            + "if (!a || !a.href) return true;"
            + "if (a.href === im.src) return true;"
            + "return /\\.(png|jpe?g|gif|svg|webp|bmp)(\\?|#|$)/i.test(a.href);}"
            + "function current(){var r=[],im=document.images,i;"
            + "for(i=0;i<im.length;i++){if(viewable(im[i])) r.push(im[i].src);}return r;}"
            + "document.addEventListener('click', function(e){"
            + "var el = e.target;"
            + "if (!el || el.tagName !== 'IMG') return;"
            + "if (!viewable(el)) return;"
            + "var urls = current(); var idx = urls.indexOf(el.src); if (idx < 0) idx = 0;"
            + "if (!urls.length) return;"
            + "OpenHubImage.onImageClick(JSON.stringify(urls), idx);"
            + "e.preventDefault(); e.stopPropagation();"
            + "}, true);"
            + "})();";

    public CodeWebView(Context context) {
        super(context);
        init(null);
    }

    public CodeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CodeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public CodeWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray tp = getContext().obtainStyledAttributes(attrs, R.styleable.CodeWebView);
            try {
                backgroundColor = tp.getColor(R.styleable.CodeWebView_webview_background,
                        ViewUtils.getWindowBackground(getContext()));
                setBackgroundColor(backgroundColor);
            } finally {
                tp.recycle();
            }
        }

        setWebChromeClient(new ChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setWebViewClient(new WebClientN());
        } else {
            setWebViewClient(new WebClient());
        }
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setGeolocationDatabasePath(getContext().getCacheDir().getPath());
        settings.setGeolocationEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDefaultTextEncodingName("utf-8");
        boolean isLoadImageEnable = PrefUtils.isLoadImageEnable();
        settings.setLoadsImagesAutomatically(isLoadImageEnable);
        settings.setBlockNetworkImage(!isLoadImageEnable);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult result = getHitTestResult();
                if (hitLinkResult(result) && !StringUtils.isBlank(result.getExtra())) {
                    AppUtils.copyToClipboard(getContext(), result.getExtra());
                    return true;
                }
                return false;
            }
        });
    }

    public void setContentChangedListener(ContentChangedListener contentChangedListener) {
        this.contentChangedListener = contentChangedListener;
    }

    /**
     * Enables image-tap reporting for pages that hold several images (e.g. a README).
     * Call before loading the content.
     */
    public void setImageClickListener(@Nullable OnImageClickListener listener) {
        imageClickListener = listener;
        if (listener != null) {
            addJavascriptInterface(new ImageBridge(), "OpenHubImage");
        }
    }

    private void injectImageClickScript(@NonNull WebView view) {
        if (imageClickListener == null) return;
        view.evaluateJavascript(IMAGE_CLICK_SCRIPT, null);
    }

    /**
     * Receives the image tap from the page's JavaScript and hops back to the UI thread.
     */
    private class ImageBridge {
        @JavascriptInterface
        public void onImageClick(String imageUrlsJson, int index) {
            if (imageClickListener == null) return;
            final ArrayList<String> urls = parseImageUrls(imageUrlsJson);
            if (urls.isEmpty()) return;
            final int safeIndex = Math.max(0, Math.min(index, urls.size() - 1));
            post(() -> {
                if (imageClickListener != null) {
                    imageClickListener.onImageClick(urls, safeIndex);
                }
            });
        }
    }

    @NonNull
    private static ArrayList<String> parseImageUrls(@Nullable String imageUrlsJson) {
        ArrayList<String> urls = new ArrayList<>();
        if (StringUtils.isBlank(imageUrlsJson)) return urls;
        try {
            JSONArray array = new JSONArray(imageUrlsJson);
            for (int i = 0; i < array.length(); i++) {
                String url = array.optString(i, null);
                if (!StringUtils.isBlank(url)) urls.add(url);
            }
        } catch (JSONException e) {
            return urls;
        }
        return urls;
    }

    public void setCodeSource(@NonNull String source, boolean wrap) {
        setCodeSource(source, wrap, null);
    }

    public void loadImage(@NonNull String url) {
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        String html = HtmlHelper.generateImageHtml(url, getCodeBackgroundColor());
        // loadData() URL-decodes its input, so the '#' of the CSS background colour
        // truncates everything after it (including the <img> tag) and nothing shows.
        // loadDataWithBaseURL loads the markup verbatim.
        loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    public void setHtmlSource(@NonNull String htmlSource) {
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        String html = HtmlHelper.generateHtmlSourceHtml(htmlSource,
                getCodeBackgroundColor(), getAccentColor());
        loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    public void setMdSource(@NonNull String source, @Nullable String baseUrl) {
        setMdSource(source, baseUrl, false);
    }

    public void setMdSource(@NonNull String source, @Nullable String baseUrl, boolean wrapCode) {
        if (StringUtils.isBlank(source)) return;
        String page = HtmlHelper.generateMdHtml(source, baseUrl, AppUtils.isNightMode(),
                getCodeBackgroundColor(), getAccentColor(), wrapCode);
        loadPage(page);
    }

    public void setCodeSource(@NonNull String source, boolean wrap, @Nullable String extension) {
        if (StringUtils.isBlank(source)) return;
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        String page = HtmlHelper.generateCodeHtml(source, extension, AppUtils.isNightMode(),
                getCodeBackgroundColor(), wrap, true);
        loadPage(page);
    }

    public void setDiffFileSource(@NonNull String source, boolean wrap) {
        if (StringUtils.isBlank(source)) return;
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        String page = HtmlHelper.generateDiffHtml(source, AppUtils.isNightMode(),
                getCodeBackgroundColor(), wrap);
        loadPage(page);
    }

    private void loadPageWithBaseUrl(final String baseUrl, final String page){
        post(new Runnable() {
            @Override
            public void run() {
                loadDataWithBaseURL(baseUrl, page, "text/html", "utf-8", null);
            }
        });
    }

    private void loadPage(String page) {
        loadPageWithBaseUrl("file:///android_asset/code_prettify/", page);
    }

    private boolean hitLinkResult(WebView.HitTestResult result) {
        return result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                result.getType() == HitTestResult.IMAGE_TYPE ||
                result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE;
    }

    private class ChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            super.onProgressChanged(view, progress);
            if (contentChangedListener != null) {
                contentChangedListener.onContentChanged(progress);
            }
        }
    }

    private class WebClientN extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            startActivity(request.getUrl());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            injectImageClickScript(view);
        }
    }

    private class WebClient extends WebViewClient {
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            startActivity(Uri.parse(url));
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            injectImageClickScript(view);
        }
    }

    private String getCodeBackgroundColor(){
        return "#" + Integer.toHexString(backgroundColor).substring(2).toUpperCase();
    }
    private String getAccentColor(){
        return "#" + Integer.toHexString(ViewUtils.getAccentColor(getContext())).substring(2).toUpperCase();
    }

    private void startActivity(Uri uri){
        if(uri == null) return;
        AppOpener.launchUrl(getContext(), uri);
    }
}


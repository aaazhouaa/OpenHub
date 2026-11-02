package com.thirtydegreesray.openhub.ui.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.ui.widget.webview.CodeWebView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * One zoomable image per page for the in-place image preview dialog.
 */
public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageHolder> {

    private final List<String> imageUrls;

    public ImagePagerAdapter(@NonNull List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_image_page, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        holder.bind(imageUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.web_view) CodeWebView webView;

        private float initialScale;

        ImageHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            webView.setOnTouchListener(this::onWebViewTouch);
        }

        void bind(@NonNull String url) {
            initialScale = 0f;
            webView.loadImage(url);
        }

        /**
         * ViewPager2 会在横向拖动时抢走手势。默认允许它拦截（保证翻页正常），
         * 只有图片已放大或出现双指时，才禁止父级拦截，让 WebView 处理拖动/缩放。
         */
        private boolean onWebViewTouch(@NonNull View view, @NonNull MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (initialScale <= 0f) {
                        initialScale = webView.getScale();
                    }
                    view.getParent().requestDisallowInterceptTouchEvent(isZoomed());
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() > 1 || isZoomed()) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                default:
                    break;
            }
            return false;
        }

        private boolean isZoomed() {
            return webView.getScale() > initialScale + 0.01f;
        }
    }
}

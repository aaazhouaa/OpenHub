package com.thirtydegreesray.openhub.ui.adapter;

import android.view.LayoutInflater;
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
 * One zoomable image per page for {@link com.thirtydegreesray.openhub.ui.activity.ImageGalleryActivity}.
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
        holder.webView.loadImage(imageUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.web_view) CodeWebView webView;

        ImageHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

package com.thirtydegreesray.openhub.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.ui.adapter.ImagePagerAdapter;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * In-place full-screen image preview. Unlike the old ImageGalleryActivity, this
 * opens as a full-screen dialog over the current page (no activity jump), so
 * pressing back returns directly to where the image was tapped.
 */
public class ImagePreviewDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_URLS = "imageUrls";
    private static final String ARG_CURRENT_INDEX = "currentIndex";

    public static ImagePreviewDialogFragment newInstance(@NonNull ArrayList<String> imageUrls,
                                                          int currentIndex) {
        ImagePreviewDialogFragment fragment = new ImagePreviewDialogFragment();
        fragment.setArguments(BundleHelper.builder()
                .putStringList(ARG_IMAGE_URLS, imageUrls)
                .put(ARG_CURRENT_INDEX, currentIndex)
                .build());
        return fragment;
    }

    /**
     * Shows a single image in-place (no activity jump).
     */
    public static void showSingle(@NonNull androidx.fragment.app.Fragment host,
                                  @NonNull String imageUrl) {
        ArrayList<String> urls = new ArrayList<>();
        urls.add(imageUrl);
        newInstance(urls, 0).show(host.getChildFragmentManager(), "image_preview");
    }

    @BindView(R2.id.image_pager) ViewPager2 imagePager;
    @BindView(R2.id.image_counter) TextView imageCounter;
    @BindView(R2.id.close_button) ImageView closeButton;

    private ArrayList<String> imageUrls;
    private int currentIndex;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Bundle args = getArguments();
        imageUrls = args != null ? args.getStringArrayList(ARG_IMAGE_URLS) : null;
        currentIndex = args != null ? args.getInt(ARG_CURRENT_INDEX, 0) : 0;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_preview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (imageUrls == null || imageUrls.isEmpty()) {
            dismiss();
            return;
        }
        if (currentIndex < 0 || currentIndex >= imageUrls.size()) currentIndex = 0;

        imagePager.setAdapter(new ImagePagerAdapter(imageUrls));
        imagePager.setCurrentItem(currentIndex, false);
        imagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                updateCounter();
            }
        });
        imageCounter.setVisibility(imageUrls.size() > 1 ? View.VISIBLE : View.GONE);
        updateCounter();

        closeButton.setOnClickListener(v -> dismiss());
    }

    private void updateCounter() {
        if (imageCounter != null) {
            imageCounter.setText((currentIndex + 1) + " / " + imageUrls.size());
        }
    }
}

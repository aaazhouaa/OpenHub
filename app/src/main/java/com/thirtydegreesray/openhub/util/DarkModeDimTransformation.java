package com.thirtydegreesray.openhub.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * Dims images in dark mode to reduce glare while keeping full brightness in light mode.
 */
public class DarkModeDimTransformation extends BitmapTransformation {

    private static final String ID = "com.thirtydegreesray.openhub.util.DarkModeDimTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);
    private static final float DIM_FACTOR = 0.68f;

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if (!AppUtils.isNightMode()) {
            return toTransform;
        }

        Bitmap.Config config = toTransform.getConfig() != null ? toTransform.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap result = pool.get(toTransform.getWidth(), toTransform.getHeight(), config);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(DIM_FACTOR, DIM_FACTOR, DIM_FACTOR, 1.0f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(toTransform, 0, 0, paint);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DarkModeDimTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + (AppUtils.isNightMode() ? 1 : 0);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        messageDigest.update((byte) (AppUtils.isNightMode() ? 1 : 0));
    }
}

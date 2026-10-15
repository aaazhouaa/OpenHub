package com.thirtydegreesray.openhub.ui.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thirtydegreesray.openhub.util.ThemeHelper;

/**
 * Dims colorful emojis in dark mode to reduce glare against dark backgrounds.
 */
public class DimEmojiSpan extends ReplacementSpan {

    private static final int DIM_ALPHA = (int) (255 * 0.42f);

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        if (ThemeHelper.isLightMode()) {
            canvas.drawText(text, start, end, x, y, paint);
            return;
        }

        int prevAlpha = paint.getAlpha();
        try {
            paint.setAlpha(DIM_ALPHA);
            canvas.drawText(text, start, end, x, y, paint);
        } finally {
            paint.setAlpha(prevAlpha);
        }
    }
}

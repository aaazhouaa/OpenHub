package com.thirtydegreesray.openhub.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.reflect.Field;

/**
 * 支持从屏幕任意位置向右滑动打开侧边栏的 DrawerLayout
 */
public class SlideDrawerLayout extends DrawerLayout {

    private ViewDragHelper leftDragger;
    private int touchSlop;
    private float downX;
    private float downY;
    private boolean isRightSwiping = false;

    public SlideDrawerLayout(@NonNull Context context) {
        this(context, null);
    }

    public SlideDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        disableSystemGestureInsetsOverride();
        findLeftDragger();
        updateLeftDraggerEdgeSize();
    }

    private void disableSystemGestureInsetsOverride() {
        try {
            Field sField = DrawerLayout.class.getDeclaredField("sEdgeSizeUsingSystemGestureInsets");
            sField.setAccessible(true);
            sField.setBoolean(null, false);
        } catch (Throwable ignored) {
        }
    }

    private void findLeftDragger() {
        if (leftDragger != null) return;
        try {
            Field leftDraggerField = DrawerLayout.class.getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            leftDragger = (ViewDragHelper) leftDraggerField.get(this);
        } catch (Throwable e) {
            for (Field field : DrawerLayout.class.getDeclaredFields()) {
                if (ViewDragHelper.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        leftDragger = (ViewDragHelper) field.get(this);
                        break;
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

    private void updateLeftDraggerEdgeSize() {
        if (leftDragger == null) {
            findLeftDragger();
        }
        if (leftDragger == null) return;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int targetEdgeSize = dm.widthPixels;

        try {
            leftDragger.setEdgeSize(targetEdgeSize);
        } catch (Throwable ignored) {
        }

        try {
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            edgeSizeField.setInt(leftDragger, targetEdgeSize);
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateLeftDraggerEdgeSize();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        updateLeftDraggerEdgeSize();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                isRightSwiping = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                if (!isDrawerOpen(GravityCompat.START)
                        && getDrawerLockMode(GravityCompat.START) == LOCK_MODE_UNLOCKED) {
                    if (dx > touchSlop && dx > Math.abs(dy) * 1.5f) {
                        isRightSwiping = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isRightSwiping && !isDrawerOpen(GravityCompat.START)) {
                    float totalDx = ev.getX() - downX;
                    float totalDy = ev.getY() - downY;
                    int minTrigger = touchSlop * 3;
                    if (totalDx > minTrigger && totalDx > Math.abs(totalDy) * 1.5f) {
                        if (leftDragger != null && leftDragger.getViewDragState() == ViewDragHelper.STATE_IDLE) {
                            openDrawer(GravityCompat.START, true);
                        }
                    }
                }
                isRightSwiping = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                isRightSwiping = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept && isRightSwiping) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }
}

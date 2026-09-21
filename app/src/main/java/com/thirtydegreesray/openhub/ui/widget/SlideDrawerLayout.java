package com.thirtydegreesray.openhub.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * 支持从内容区域任意位置向右滑动打开左侧抽屉的 DrawerLayout。
 *
 * <p>与之前通过反射放大 ViewDragHelper edge size 的做法不同，这里不改动 DrawerLayout 内部状态，
 * 只在其手势判定之外补一层意图识别：
 * <ul>
 *   <li>优先交给 {@code super}：左侧边缘内的跟手拖拽、打开状态下的拖拽关闭、点击遮罩关闭，
 *       全部沿用 DrawerLayout 原生逻辑；</li>
 *   <li>super 未接管时，再判断是否是"向右、且水平位移明显大于竖直位移"的手势。
 *       是则拦截并取消子 View 的点击，抬手后打开抽屉；</li>
 *   <li>竖直方向的手势从不拦截，列表上下滚动完全不受影响。</li>
 * </ul>
 *
 * <p>之所以不在检测到右滑后让抽屉跟手移动：{@code ViewDragHelper} 只会捕获真正可横向拖动
 * 的视图（{@link DrawerLayout} 中只有抽屉自身满足），内容视图无法被捕获，因此全屏右滑
 * 采用抬手后动画打开的方式。
 */
public class SlideDrawerLayout extends DrawerLayout {

    /**
     * 水平位移需大于竖直位移的倍数，手势才判定为横向滑抽屉。
     * 取 1.5 略偏袒竖直方向，减少列表上下滑动被误判的概率。
     */
    private static final float HORIZONTAL_BIAS = 1.5f;

    /** 触发拦截所需的水平位移，为 touchSlop 的倍数，避免刚过阈值就抢走手势。 */
    private static final float TRIGGER_SLOP_FACTOR = 2f;

    private int touchSlop;

    private float downX;
    private float downY;
    private boolean dragging;

    public SlideDrawerLayout(@NonNull Context context) {
        this(context, null);
    }

    public SlideDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 原生逻辑优先：边缘跟手拖拽、拖拽关闭、点击遮罩都由 DrawerLayout 处理。
        // 没有这一步，左侧边缘起手的滑动会失去跟手效果、打开状态下也无法拖拽关闭。
        if (super.onInterceptTouchEvent(ev)) {
            return true;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                dragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (!dragging && canStartSlideFromContent()) {
                    dragging = isRightSlide(ev.getX() - downX, ev.getY() - downY);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                break;
        }

        return dragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = super.onTouchEvent(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                if (dragging) {
                    dragging = false;
                    // 中途抽屉可能被移除或锁定，这里再确认一次，避免 openDrawer 抛异常
                    if (canStartSlideFromContent()) {
                        openDrawer(GravityCompat.START, true);
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                break;
        }
        return handled;
    }

    /**
     * 是否具备"从内容区域右滑拉出左侧抽屉"的前提：没有抽屉正在展示，且左侧抽屉可被打开。
     */
    private boolean canStartSlideFromContent() {
        if (isDrawerVisible(GravityCompat.START) || isDrawerVisible(GravityCompat.END)) {
            // 已有抽屉在展示，交给原生逻辑处理拖拽关闭
            return false;
        }
        View drawer = findStartDrawer();
        return drawer != null && getDrawerLockMode(drawer) == LOCK_MODE_UNLOCKED;
    }

    /**
     * 查找左侧抽屉。{@code DrawerLayout#findDrawerWithGravity} 不是 public，这里按同样的方式
     * 解析绝对 gravity 后自行遍历。内容视图没有设置 gravity，不会被误认。
     */
    @Nullable
    private View findStartDrawer() {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child.getLayoutParams() instanceof LayoutParams)) {
                continue;
            }
            int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
            int absGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection)
                    & Gravity.HORIZONTAL_GRAVITY_MASK;
            if (absGravity == Gravity.LEFT) {
                return child;
            }
        }
        return null;
    }

    /**
     * 判断是否是向右拉出左侧抽屉的手势。
     *
     * @param dx 相对按下点的水平位移，向右为正
     * @param dy 相对按下点的竖直位移
     */
    private boolean isRightSlide(float dx, float dy) {
        return dx > touchSlop * TRIGGER_SLOP_FACTOR && dx > Math.abs(dy) * HORIZONTAL_BIAS;
    }
}

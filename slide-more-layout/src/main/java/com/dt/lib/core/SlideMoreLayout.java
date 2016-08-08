package com.dt.lib.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Winter on 2016/8/3 0003.
 */
public class SlideMoreLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private View mSurfaceView;
    private View mDetailView;
    private int mSlideOffset;
    private int mSlideMaxHeight;
    private boolean isShowingDetail;
    private boolean isHandleScroll;
    private Animator mAnimator;
    private NestedScrollingParentHelper nestedParentHelper;
    private NestedScrollingChildHelper nestedChildHelper;

    private int mSwitchThreshold = 60;//default threshold (dp)

    public SlideMoreLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SlideMoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SlideMoreLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mSlideOffset = 0;
        mSlideMaxHeight = 0;
        isHandleScroll = false;
        isShowingDetail = false;
        mSwitchThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mSwitchThreshold, context.getResources().getDisplayMetrics());
        nestedParentHelper = new NestedScrollingParentHelper(this);
        nestedChildHelper = new NestedScrollingChildHelper(this);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, com.dt.lib.core.R.styleable.SlideMoreLayout);
            mSwitchThreshold = typedArray.getDimensionPixelSize(com.dt.lib.core.R.styleable.SlideMoreLayout_switchThreshold, mSwitchThreshold);
            typedArray.recycle();
        }

        setNestedScrollingEnabled(true);
    }

    @Override
    public void onViewAdded(View child) {
        mSurfaceView = getChildAt(0);
        mDetailView = getChildAt(1);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildWithMargins(mSurfaceView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        measureChildWithMargins(mDetailView, widthMeasureSpec, 0, heightMeasureSpec, 0);

        MarginLayoutParams surfaceLP = (MarginLayoutParams) mSurfaceView.getLayoutParams();
        MarginLayoutParams detailLP = (MarginLayoutParams) mDetailView.getLayoutParams();

        int widthResult =
                Math.max(getSuggestedMinimumWidth(),
                        Math.max(
                                mSurfaceView.getMeasuredWidth()
                                        + getPaddingLeft() + getPaddingRight()
                                        + surfaceLP.leftMargin + surfaceLP.rightMargin,
                                mDetailView.getMeasuredWidth()
                                        + getPaddingLeft() + getPaddingRight()
                                        + detailLP.leftMargin + detailLP.rightMargin));
        int heightResult =
                Math.max(getSuggestedMinimumHeight(),
                        Math.max(
                                mSurfaceView.getMeasuredHeight()
                                        + getPaddingTop() + getPaddingBottom()
                                        + surfaceLP.topMargin + surfaceLP.bottomMargin,
                                mDetailView.getMeasuredHeight()
                                        + getPaddingTop() + getPaddingBottom()
                                        + detailLP.topMargin + detailLP.bottomMargin));

        setMeasuredDimension(widthResult, heightResult);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int tLeft = l + getPaddingLeft();
        int tRight = r - getPaddingRight();
        int tTop = t + getPaddingTop();
        int tBottom = b - getPaddingBottom();

        MarginLayoutParams surfaceLP = (MarginLayoutParams) mSurfaceView.getLayoutParams();
        MarginLayoutParams detailLP = (MarginLayoutParams) mDetailView.getLayoutParams();

        int width = Math.max(tRight - tLeft, 0);
        int height = Math.max(tBottom - tTop, 0);
        mSlideMaxHeight = height;
        int offset = Math.min(Math.max(0, mSlideOffset), mSlideMaxHeight);

        if (width > 0 && height > 0) {
            if (mSurfaceView.getVisibility() != GONE) {
                int surfaceLeft = tLeft + surfaceLP.leftMargin;
                int surfaceTop = tTop + surfaceLP.topMargin - offset;
                mSurfaceView.layout(
                        surfaceLeft,
                        surfaceTop,
                        surfaceLeft + mSurfaceView.getMeasuredWidth(),
                        surfaceTop + mSurfaceView.getMeasuredHeight());
            }

            if (mDetailView.getVisibility() != GONE) {
                int detailLeft = tLeft + detailLP.leftMargin;
                int detailTop = tTop + detailLP.topMargin + height - offset;
                mDetailView.layout(
                        detailLeft,
                        detailTop,
                        detailLeft + mDetailView.getMeasuredWidth(),
                        detailTop + mDetailView.getMeasuredHeight());
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                }
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
//                if (isHandleScroll) {
//                    float moveDis = downY - event.getY();
//                    if (!isShowingDetail && moveDis > 0) {
//                        return true;
//                    }
//                    if (isShowingDetail && moveDis < 0) {
//                        return true;
//                    }
//                }
                break;
        }
        return false;
    }

    float downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float moveDis = downY - event.getY();
                addSlideOffset((int) moveDis);
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetSlideOffset();
                return true;
        }
        return false;
    }

    //NestedScrollingParent
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        nestedParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        isHandleScroll = false;
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int myConsumed = 0;
        int myUnconsumed = dyUnconsumed;
        if (isHandleScroll) {
            if (dyUnconsumed != 0) {
                addSlideOffset(dyUnconsumed);
                myConsumed = dyUnconsumed;
                myUnconsumed = 0;
            } else if (Math.abs(dyConsumed) > 10) {
                addSlideOffset(dyConsumed);
            }
        } else if ((!isShowingDetail && dyUnconsumed > 0) || (isShowingDetail && dyUnconsumed < 0)) {
            addSlideOffset(dyUnconsumed);
            isHandleScroll = true;
            myConsumed = dyUnconsumed;
            myUnconsumed = 0;
        }
        dispatchNestedScroll(0, myConsumed, dxUnconsumed, myUnconsumed, null);
    }

    @Override
    public void onStopNestedScroll(View target) {
        if (isHandleScroll) {
            resetSlideOffset();
        }
        nestedParentHelper.onStopNestedScroll(target);
        stopNestedScroll();
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return nestedParentHelper.getNestedScrollAxes();
    }

    //NestedScrollingChild
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        nestedChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return nestedChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return nestedChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        nestedChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return nestedChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return nestedChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    private void addSlideOffset(int offset) {
        setSlideOffset(mSlideOffset + offset);
    }

    public void setSlideOffset(int offset) {
        mSlideOffset = Math.min(Math.max(0, offset), mSlideMaxHeight);
        requestLayout();
    }

    public int getSlideOffset() {
        return mSlideOffset;
    }

    private void resetSlideOffset() {
        if (!isShowingDetail) {
            //判断是否显示Detail
            if (mSlideOffset >= mSwitchThreshold) {
                showDetail();
            } else {
                showSurface();
            }
        } else {
            //判断是否显示Surface
            if (mSlideMaxHeight - mSlideOffset >= mSwitchThreshold) {
                showSurface();
            } else {
                showDetail();
            }
        }
    }

    private void showDetail() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ObjectAnimator.ofInt(this, "SlideOffset", mSlideMaxHeight);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isShowingDetail = true;
            }
        });
        mAnimator.setDuration(300);
        mAnimator.start();
    }

    private void showSurface() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ObjectAnimator.ofInt(this, "SlideOffset", 0);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isShowingDetail = false;
            }
        });
        mAnimator.setDuration(300);
        mAnimator.start();
    }
}

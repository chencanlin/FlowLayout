package com.ccl.perfectisshit.flowlayout.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.ccl.perfectisshit.flowlayout.listener.OnFlowLayoutClickListener;
import com.ccl.perfectisshit.flowlayout.util.DensityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccl on 2017/9/5.
 */

public class FlowLayout extends ViewGroup {
    private SparseArrayCompat<View> viewArray = new SparseArrayCompat<>();
    private SparseArrayCompat<Rect> childViewRectArray = new SparseArrayCompat<>();
    private List<Integer> lineWidthArray = new ArrayList<>();
    private List<Integer> lineHeightArray = new ArrayList<>();
    private int mMeasureWidth;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;
    private Scroller mScroller;
    private float mStartY;
    private OnFlowLayoutClickListener mClickListener;
    private float mY;
    private Rect mLocalVisibleRect;
    private BaseAdapter<?> mAdapter;

    private VelocityTracker velocityTracker;
    private int minimumVelocity;
    private int maximumVelocity;
    private int mMeasureHeight;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        mLocalVisibleRect = new Rect();
        getLocalVisibleRect(mLocalVisibleRect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        calculateChildViewSize();
        int height = 0;
        if (lineHeightArray != null && lineHeightArray.size() > 0) {
            height = lineHeightArray.get(lineHeightArray.size() - 1);
        }
        setMeasuredDimension(getMeasuredWidth(), height);
    }

    private void calculateChildViewSize() {
        int lineWidth = mPaddingLeft;
        int lineHeight = mPaddingTop;
        if (viewArray != null && viewArray.size() != 0) {
            for (int j = 0; j < viewArray.size(); j++) {
                View view = viewArray.get(j);
                int measuredWidth = view.getMeasuredWidth();
                int measuredHeight = view.getMeasuredHeight();
                if (mMeasureWidth - lineWidth - mPaddingRight < view.getMeasuredWidth()) {
                    lineWidth += mPaddingRight;
                    lineWidthArray.add(lineWidth);
                    lineWidth = mPaddingLeft;
                    lineHeight += view.getMeasuredHeight() + DensityUtils.dp2px(getContext(), 10);
                    lineHeightArray.add(lineHeight);
                }
                Rect rect = new Rect(lineWidth, lineHeight, lineWidth + measuredWidth, lineHeight + measuredHeight);
                childViewRectArray.put(j, rect);
                lineWidth += measuredWidth;
                if (mMeasureWidth - lineWidth - mPaddingRight >= DensityUtils.dp2px(getContext(), 10)) {
                    lineWidth += DensityUtils.dp2px(getContext(), 10);
                }
            }
            int measuredWidth = viewArray.get(viewArray.size() - 1).getMeasuredWidth();
            int measuredHeight = viewArray.get(viewArray.size() - 1).getMeasuredHeight();
            lineWidth = lineWidth + measuredWidth + mPaddingRight;
            lineHeight = lineHeight + measuredHeight + mPaddingBottom;
            lineWidthArray.add(lineWidth);
            lineHeightArray.add(lineHeight);
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if (childViewRectArray != null && childViewRectArray.size() > 0) {
            for (int j = 0; j < childViewRectArray.size(); j++) {
                Rect rect = childViewRectArray.get(j);
                viewArray.get(j).layout(rect.left, rect.top, rect.right, rect.bottom);
            }
        }
    }

    private void smoothScrollBy(int dX, int dY) {
        if (mLocalVisibleRect != null && mMeasureHeight <= mLocalVisibleRect.height()) {
            return;
        }
        if (dY + mScroller.getFinalY() < 0) {
            dY = -mScroller.getFinalY();
        } else if (dY + mScroller.getFinalY() > mMeasureHeight - mLocalVisibleRect.height()) {
            dY = mMeasureHeight - mLocalVisibleRect.height() - mScroller.getFinalY();
        }
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dX, dY);
        postInvalidate();
    }

    private View isClickOnChild(int x, int y) {
        if (viewArray != null && viewArray.size() > 0) {
            for (int i = 0; i < viewArray.size(); i++) {
                View view = viewArray.get(i);
                Rect rect = childViewRectArray.get(i);
                Rect visibleRect = new Rect();
                visibleRect.left = rect.left;
                visibleRect.right = rect.right;
                visibleRect.top = rect.top - getScrollY();
                visibleRect.bottom = rect.bottom - getScrollY();
                if (visibleRect.contains(x, y)) {
                    return view;
                }
            }
        }
        return null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mStartY = ev.getY();
                mY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dY = ev.getY() - mStartY;
                smoothScrollBy(0, -(int) dY);
                mStartY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(ev.getY() - mY) < 20) {
                    View clickOnChild = isClickOnChild(((int) ev.getX()), ((int) ev.getY()));
                    if (clickOnChild != null && mClickListener != null) {
                        clickOnChild.setSelected(!clickOnChild.isSelected());
                        mClickListener.onClick(clickOnChild);
                    }
                }

                final VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);//计算当前速度(按1秒为单位)
                int velocityY = -(int) velocityTracker.getYVelocity();//获取y方向速度
                if (Math.abs(velocityY) > minimumVelocity) {
                    mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, getMaxScrollY());
                } else {
                    if (this.velocityTracker != null) {
                        this.velocityTracker.recycle();
                        this.velocityTracker = null;
                    }
                }
                break;
        }
        return true;
    }

    public int getMaxScrollY() {
        if (mMeasureHeight - mLocalVisibleRect.height() > 0) {
            return (mMeasureHeight - mLocalVisibleRect.height());
        } else {
            return 0;
        }
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    public void notifyDataSetChanged() {
        if (mAdapter == null) {
            return;
        }
        clearData();
        inflateView();
    }

    private void clearData() {
        removeAllViews();
        if (viewArray != null) {
            viewArray.clear();
        }
        if (childViewRectArray != null) {
            childViewRectArray.clear();
        }
        if (lineHeightArray != null) {
            lineHeightArray.clear();
        }
        if (lineWidthArray != null) {
            lineWidthArray.clear();
        }
    }

    public void setOnClickListener(OnFlowLayoutClickListener listener) {
        mClickListener = listener;
    }

    public void setAdapter(BaseAdapter<?> adapter) {
        clearData();
        if (adapter == null) {
            return;
        }
        mAdapter = adapter;
        inflateView();
    }

    private void inflateView() {
        int count = mAdapter.getCount();
        if (count == 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            View view = mAdapter.getView(i, this);
            if (view == null) {
                throw new NullPointerException("View is null object");
            }
            MarginLayoutParams textMarginLayoutParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(view, textMarginLayoutParams);
            viewArray.put(i, view);
        }
        requestLayout();
    }

    private interface AdapterInterface<T> {
        T getItem(int position);

        int getCount();

        View getView(int position, ViewGroup parent);
    }

    public static abstract class BaseAdapter<T> implements AdapterInterface<T> {

    }

    private int getScreenSize(int type) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return type == 0 ? displayMetrics.widthPixels : displayMetrics.heightPixels;
    }
}

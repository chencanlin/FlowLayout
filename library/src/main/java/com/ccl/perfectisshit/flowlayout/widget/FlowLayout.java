package com.ccl.perfectisshit.flowlayout.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import com.ccl.perfectisshit.flowlayout.R;
import com.ccl.perfectisshit.flowlayout.listener.OnFlowLayoutClickListener;
import com.ccl.perfectisshit.flowlayout.util.DensityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccl on 2017/9/5.
 */

public class FlowLayout extends ViewGroup {
    private List<String> textData;
    private SparseArrayCompat<TextView> textViewArray = new SparseArrayCompat<>();
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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMeasureWidth = getMeasuredWidth();
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
        if (textData != null && textData.size() != 0) {
            for (int j = 0; j < textData.size(); j++) {
                TextView textView = textViewArray.get(j);
                int measuredWidth = textView.getMeasuredWidth();
                int measuredHeight = textView.getMeasuredHeight();
                if (mMeasureWidth - lineWidth - mPaddingRight < textView.getMeasuredWidth()) {
                    lineWidth += mPaddingRight;
                    lineWidthArray.add(lineWidth);
                    lineWidth = mPaddingLeft;
                    lineHeight += textView.getMeasuredHeight() + DensityUtils.dp2px(getContext(), 10);
                    lineHeightArray.add(lineHeight);
                }
                Rect rect = new Rect(lineWidth, lineHeight, lineWidth + measuredWidth, lineHeight + measuredHeight);
                childViewRectArray.put(j, rect);
                lineWidth += measuredWidth;
                if (mMeasureWidth - lineWidth - mPaddingRight >= DensityUtils.dp2px(getContext(), 10)) {
                    lineWidth += DensityUtils.dp2px(getContext(), 10);
                }
            }
            int measuredWidth = textViewArray.get(textViewArray.size() - 1).getMeasuredWidth();
            int measuredHeight = textViewArray.get(textViewArray.size() - 1).getMeasuredHeight();
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
                textViewArray.get(j).layout(rect.left, rect.top, rect.right, rect.bottom);
            }
        }
    }

    private void smoothScrollBy(int dX, int dY) {
        if (mLocalVisibleRect != null && getMeasuredHeight() <= mLocalVisibleRect.height()) {
            return;
        }
        if (dY + mScroller.getFinalY() < 0) {
            dY = -mScroller.getFinalY();
        } else if (dY + mScroller.getFinalY() > getMeasuredHeight() - mLocalVisibleRect.height() + mPaddingTop + mPaddingBottom) {
            dY = getMeasuredHeight() - mLocalVisibleRect.height() + mPaddingTop + mPaddingBottom - mScroller.getFinalY();
        }
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dX, dY);
        postInvalidate();
    }

    private View isClickOnChild(int x, int y) {
        if (textViewArray != null && textViewArray.size() > 0) {
            for (int i = 0; i < textViewArray.size(); i++) {
                TextView textView = textViewArray.get(i);
                Rect rect = childViewRectArray.get(i);
                rect.top = rect.top - getScrollY();
                rect.bottom = rect.bottom - getScrollY();
                if (rect.contains(x, y)) {
                    return textView;
                }
            }
        }
        return null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
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
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    public void setDataList(List<String> textData) {
        this.textData = textData;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        clearData();
        generateTextView();
    }

    private void clearData() {
        removeAllViews();
        if (textViewArray != null) {
            textViewArray.clear();
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

    private void generateTextView() {
        if (textData == null || textData.size() == 0) {
            return;
        }
        for (int i = 0; i < textData.size(); i++) {
            TextView textView = new TextView(getContext());
            textView.setPadding(DensityUtils.dp2px(getContext(), 5), DensityUtils.dp2px(getContext(), 5), DensityUtils.dp2px(getContext(), 5), DensityUtils.dp2px(getContext(), 5));
            textView.setId(i);
            textView.setBackground(getContext().getResources().getDrawable(R.drawable.selector_flow_layout_tv_bg));
            textView.setTextColor(getContext().getResources().getColor(android.R.color.holo_blue_bright));
            textView.setTextSize(13);
            textView.setText(textData.get(i));
            MarginLayoutParams textMarginLayoutParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(textView, textMarginLayoutParams);
            textViewArray.put(i, textView);
        }
        requestLayout();
    }

    private int getScreenSize(int type) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return type == 0 ? displayMetrics.widthPixels : displayMetrics.heightPixels;
    }
}

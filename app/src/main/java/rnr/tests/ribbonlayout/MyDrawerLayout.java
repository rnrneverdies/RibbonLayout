package rnr.tests.ribbonlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MyDrawerLayout extends RelativeLayout {
    int sdk = android.os.Build.VERSION.SDK_INT;

    private ViewDragHelper mDragHelper;
    private View mDragView;
    private View mPanelView;
    private View mMainView;
    private View mOverlayView;

    private int mDragRange;
    private int mLeft;
    private float mDragOffset;
    private int mMaxWidth;
    private boolean firstLayout = false;
    private int mRibbonTopPadding;

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            return view == mDragView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound = getWidth() - mDragView.getMeasuredWidth();

            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
            Log.d("DragLayout", "clampViewPositionHorizontal " + left + "," + newLeft);
            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dx) {
            return mRibbonTopPadding;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragRange;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            Log.d("DragLayout", "onViewPositionChanged " + left + "," + dx);
            mLeft = left;
            mDragOffset = (float) left / mDragRange;
            if(sdk > Build.VERSION_CODES.HONEYCOMB) {
                ViewCompat.setAlpha(mOverlayView, (1 - mDragOffset) / 2);
            } else {
                mOverlayView.getBackground().setAlpha((int) (1 - mDragOffset) * 128 );
            }
            invalidate();
            requestLayout();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.d("DragLayout", "conViewReleased " + xvel + "," + yvel);
            int target = 0;

            // direction is always positive if we are sliding in the expanded direction
            float direction = -xvel;

            if (direction > 0) {
                // swipe up -> expand
                target = 0;
            } else if (direction < 0) {
                // swipe down -> collapse
                target = mMaxWidth - mDragView.getMeasuredWidth();
            }
            mDragHelper.settleCapturedViewAt(target, releasedChild.getTop());
            invalidate();
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mDragHelper.continueSettling(true)) {
            //ViewCompat.postInvalidateOnAnimation(this);
            ViewCompat.postInvalidateOnAnimation(mOverlayView);
            ViewCompat.postInvalidateOnAnimation(mDragView);
            ViewCompat.postInvalidateOnAnimation(mPanelView);
        }
    }

    @Override
    protected void onFinishInflate() {
        mMainView = getChildAt(0);
        mOverlayView = getChildAt(1);
        mDragView = getChildAt(2);
        mPanelView = getChildAt(3);
    }

    public MyDrawerLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    @SuppressLint("NewApi")
    public MyDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("NewApi")
    public MyDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init (Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyDrawerLayout, 0, 0);
            try {
                mRibbonTopPadding = Math.round(ta.getDimension(R.styleable.MyDrawerLayout_ribbonTopPadding, 0.0f));
            } finally {
                ta.recycle();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                mDragHelper.cancel();
            return false;
        }*/
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        mMaxWidth = maxWidth;

        Log.d("DragLayout", "onMeasure " + maxWidth + "," + maxHeight);

        setMeasuredDimension(resolveSizeAndState2(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState2(maxHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("DragLayout", "onLayout " + t + "," + mLeft + "," + mRibbonTopPadding + "," ) ;

        if (!firstLayout) {
            firstLayout = true;
            mLeft = mMaxWidth - mDragView.getMeasuredWidth();
        }

        mDragRange = getMeasuredWidth() - mDragView.getMeasuredWidth();

        mMainView.layout(l,t,r,b);

        // overlay visible si se mueve la barra.
        if (mLeft < mMaxWidth - mDragView.getMeasuredWidth()) {
            mOverlayView.layout(l, t, r, b);
        }

        mDragView.layout(
                mLeft,
                t + mRibbonTopPadding,
                mLeft + mDragView.getMeasuredWidth() * 4,
                mDragView.getMeasuredHeight() + mRibbonTopPadding);

        mPanelView.layout(
                mLeft + mDragView.getMeasuredWidth(),
                t,
                mLeft + r,
                b);

    }

    private int resolveSizeAndState2(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result | (childMeasuredState&MEASURED_STATE_MASK);
    }


}

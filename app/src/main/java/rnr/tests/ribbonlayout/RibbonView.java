package rnr.tests.ribbonlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Created by Emanuel on 07/12/2014.
 */
public class RibbonView extends View {
    private static final String TAG = RibbonView.class.getCanonicalName();
    private Path mPath;
    private Paint mPaint = new Paint();

    public RibbonView(Context context) {
        super(context);
        init(context, null);
    }

    public RibbonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.argb(0xFF, 0x37, 0x71, 0xc8));
        mPaint.setStrokeWidth(5f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //Log.d(TAG, "onSizeChanged " + w + "," + h + "," + oldw + "," + oldh );
        mPath = new Path();
        mPath.moveTo(w/8,0);
        mPath.lineTo(w,0);
        mPath.lineTo(w,h);
        mPath.lineTo(w/8,h);
        mPath.lineTo(0,h/2);
        mPath.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enableShadow();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
    }

    @TargetApi(21)
    private void enableShadow() {
        setOutlineProvider(new RibbonOutline());
    }

    @TargetApi(21)
    private class RibbonOutline extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            RibbonView r = (RibbonView) view;
            outline.setConvexPath(r.mPath);
        }
    }

}
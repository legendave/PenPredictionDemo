package com.lenovo.lrpenpredictiondemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//add for prediction begin
import com.lenovo.api.stylusdda.LRDDA;
import com.lenovo.api.stylusprediction.TouchPointP;
//add for prediction end

/**
 * 预测加dda的demo
 */
public class HandWriteView extends View {
    public static final String TAG = "HandWriteView";

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Canvas moveCanvas;
    private Bitmap moveBitmap;
    private Paint mPaint;
    private Paint predictionPaint;
    private Path mPath;
    private float mX, mY;
    private float pdX, pdY;
    private boolean prediction = true;
    private Context mContext;

    //add for prediction begin
    private LRDDA.WindowProperty winProp;
    private PenPrediction mPenPrediction;
    //add for prediction end

    public void setPrediction(boolean prediction) {
        this.prediction = prediction;
    }

    public HandWriteView(Context context) {
        super(context);
        this.mContext = context;
        init(context);
    }

    public HandWriteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context);
    }

    public HandWriteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(context);
    }

    private void init(Context context) {
        //画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(3);


        //预测画笔
        predictionPaint = new Paint();
        predictionPaint.setAntiAlias(true);
        predictionPaint.setStyle(Paint.Style.STROKE);
        predictionPaint.setStrokeCap(Paint.Cap.ROUND);
        predictionPaint.setStrokeJoin(Paint.Join.ROUND);
        predictionPaint.setColor(Color.RED);
        predictionPaint.setStrokeWidth(3);
        mPath = new Path();

        //add for prediction begin
        mPenPrediction = new PenPrediction(mContext);
        initPrediction();
        //add for prediction end
    }

    //add for prediction begin
    private void initPrediction() {
        int rotateType = getRotateType();
        int[] screenLocation = new int[2];
        getLocationOnScreen(screenLocation);

        winProp = new LRDDA.WindowProperty(
                screenLocation[0], screenLocation[1],
                0, 0,
                rotateType
        );

        Point screenSize = getScreenSize();
        int screenHeight = Math.max(screenSize.x, screenSize.y);
        int screenWidth = Math.min(screenSize.x, screenSize.y);

        mPenPrediction.setTouchScreenInfo(screenWidth,screenHeight);
        mPenPrediction.setWinProp(winProp);
    }

    private  void updateWinProp() {//need be called when window changed
        int rotateType = getRotateType();
        int[] screenLocation = new int[2];
        getLocationOnScreen(screenLocation);

        winProp = new LRDDA.WindowProperty(
                screenLocation[0], screenLocation[1],
                0, 0,
                rotateType
        );
        mPenPrediction.setWinProp(winProp);
    }

    private int getRotateType() {
        int winRotate = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        return winRotate;
    }

    private Point getScreenSize() {//ScreenSize used by DDA
        Point point = new Point();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        Log.d(TAG, "SCREEN_SIZE: width = " + point.x + ", height = " + point.y);

        return point;
    }

    private boolean isStylusMotionEvent(@NonNull final MotionEvent event) {
        final int pointerIndex = event.getActionIndex();
        final int toolType = event.getToolType(pointerIndex);
        return toolType == MotionEvent.TOOL_TYPE_STYLUS;
    }
    //add for prediction end

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float press = event.getPressure() * 2048;
        long time = 0; //event.getEventTime();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    x =  event.getHistoricalX(i);
                    y= event.getHistoricalY(i);
                    touch_move(x, y, false);
                    invalidate();
                }

                x = event.getX();
                y = event.getY();
                press = event.getPressure() * 2048;
                touch_move(x, y, false);
                invalidate();

                //add for prediction begin
                if (isStylusMotionEvent(event)) {
                    if (prediction && mPenPrediction.couldDDA) {
                        LRDDA.PointData[] p = mPenPrediction.getDDaPoints();
                        //Log.d(TAG, "get dda point count =" + p.length);
                        for (int i = 0; i < p.length; i++) {
                            LRDDA.PointData temp = p[i];
                            if (temp.pres > 0) {
                                mPenPrediction.addPoints(new TouchPointP(temp.x, temp.y, temp.frame_t, temp.pres));
                            }
                        }

                        TouchPointP estimatedPoint = mPenPrediction.getEstimatedPoints();
                        if (estimatedPoint != null) { //before used, we need make sure it's not null
                            //Log.d(TAG, "pen predict point=" + estimatedPoint.getX() + "estimatedPoint.gety()=" + estimatedPoint.getY());
                            touch_move(estimatedPoint.getX(), estimatedPoint.getY(), true);
                            invalidate();
                        }
                    }
                }
                //add for prediction end
                break;

            case MotionEvent.ACTION_UP:
                touch_up(x, y);
                invalidate();
                if (isStylusMotionEvent(event) && prediction) {
                    mPenPrediction.clearPoints();
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG,"onDraw======");
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
        if(moveBitmap != null){

            canvas.drawBitmap(moveBitmap, 0, 0, mPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");

        //add for prediction begin
        mPenPrediction.deinit();
        //add for prediction end
    }

    public void clear() {
        mCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        moveBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        moveCanvas = new Canvas(moveBitmap);

        //add for prediction begin
        updateWinProp();
        Log.d(TAG, "winProp changed");
        //add for prediction end

    }

    private void touch_start(float x, float y) {
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y, boolean isPrediction) {
        Log.d(TAG, "call touch_move predictionPaint=" + isPrediction);
        if (isPrediction) {
            moveCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
            moveCanvas.drawLine(mX, mY, x, y, predictionPaint);
            pdX = x;
            pdY = y;

        } else {
            mCanvas.drawLine(mX, mY, x, y, mPaint);
            mX = x;
            mY = y;

        }
    }

    private void touch_up(float x, float y) {
        moveCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        pdY = 0;
        pdX = 0;
        mCanvas.drawLine(mX, mY, x, y, mPaint);
    }
}

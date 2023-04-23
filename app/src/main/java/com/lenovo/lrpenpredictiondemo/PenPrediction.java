package com.lenovo.lrpenpredictiondemo;

import android.content.Context;
import android.util.Log;

import com.lenovo.api.stylusdda.LRDDA;
import com.lenovo.api.stylusprediction.LRPenPrediction;
import com.lenovo.api.stylusprediction.TouchPointP;

public class PenPrediction {

    private static final String TAG = "PenPrediction";
    private Context myContext;
    private LRPenPrediction lrPenPrediction;
    private LRDDA lrDDA;

    public static final String DEVICE_NAME = "/dev/lenovo_penraw";
    public static final int SAMPLE_INTERVAL = 4; // The number of intervals between sample points used for prediction.
    public static final int STRAIGHT_LINE_BOOST = 1; // Radically predict 1 more point.
    public static final int STRAIGHT_LINE_JUDGE_TAP = 4; // the points used for line type judgment are the 0th, 4th and 8th points in reverse order in the time series.


    public boolean couldDDA;

    public PenPrediction(Context context) {
        this.myContext = context;
        init();
    }

    private void init() {
        lrPenPrediction = new LRPenPrediction();
        lrPenPrediction.initialPrediction(SAMPLE_INTERVAL,STRAIGHT_LINE_BOOST,STRAIGHT_LINE_JUDGE_TAP);
        lrDDA = new LRDDA();
        couldDDA = lrDDA.connectDDA(DEVICE_NAME);
        if (couldDDA) {
            Log.d(TAG, "DDA connection succeed.");
        } else {
            Log.d(TAG, "DDA connection failed.");
        }
    }

    public void deinit() {//when window closed or app exited, we need call lrDDA.disconnectDDA() to release resources
        if (couldDDA) {
            lrDDA.disconnectDDA();
            Log.d(TAG, "DDA disconnect.");
        }
    }

    public void setTouchScreenInfo(int screenWidth, int screenHeight) {
        LRDDA.TouchscreenInformation tsInfo = new LRDDA.TouchscreenInformation(
                screenWidth, screenHeight,
                1.0f, 1.0f,
                0
        );
        lrDDA.setTouchscreenInformation(tsInfo);
    }

    //called when init or window changed
    public void setWinProp(LRDDA.WindowProperty winProp) {
        lrDDA.setWindowProperty(winProp);
    }

    public void addPoints(TouchPointP point) {
        lrPenPrediction.addPoint(point);
    }

    public LRDDA.PointData[] getDDaPoints(){
        return lrDDA.getPoints();
    }

    public void clearPoints() {
        lrPenPrediction.clearPoints();
    }

    public TouchPointP getEstimatedPoints() {
        return lrPenPrediction.getEstimatedPoint();

    }


}

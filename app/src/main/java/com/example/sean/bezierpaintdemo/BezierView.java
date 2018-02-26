package com.example.sean.bezierpaintdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by sean on 2018/2/24.
 */

public class BezierView extends View {

    private static final int BEZIER_WIDTH = 10; // 贝塞尔曲线线宽
    private Paint mBezierPaint = null;
    private Paint mStrikesPaint = null;
    private Paint mRectPaint = null;
    private Path mBezierPath = null; // 贝塞尔曲线路径
    private Path mStrokesPath = null; // 画笔的曲线
    private Path mRectPath = null;
    private PorterDuffXfermode xorMode = new PorterDuffXfermode(PorterDuff.Mode.XOR);
    private PorterDuffXfermode srcInMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    public BezierView(Context context) {
        super(context);
        init();
    }

    public BezierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("onSizeChanged","w:" + w);
    }

    private void init() {
        // 贝塞尔曲线画笔
        mBezierPaint = new Paint();
        mBezierPaint.setStrokeWidth(BEZIER_WIDTH);
        mBezierPaint.setStyle(Paint.Style.FILL);
        mBezierPaint.setAntiAlias(true);

        // 描绘的笔触
        mStrikesPaint = new Paint();
        mStrikesPaint.setColor(Color.RED);
        mStrikesPaint.setStrokeWidth(150);
        mStrikesPaint.setStyle(Paint.Style.STROKE);
        mStrikesPaint.setStrokeCap(Paint.Cap.ROUND);
        mStrikesPaint.setAntiAlias(true);

        mRectPaint = new Paint();
        mRectPaint.setColor(Color.BLACK);
        mRectPaint.setStrokeWidth(2);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setAntiAlias(true);

        mBezierPath = new Path();
        mRectPath = new Path();
        mStrokesPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayer(0, 0, 1024, 1024, new Paint(), Canvas.ALL_SAVE_FLAG);
        canvas.drawColor(Color.GRAY);
        canvas.restore();
        canvas.saveLayer(0, 0, 1024, 1024, new Paint(), Canvas.ALL_SAVE_FLAG);
        canvas.drawColor(Color.WHITE);
        mRectPath.reset();
        mRectPath.moveTo(0,0);
        mRectPath.lineTo(1024, 0);
        mRectPath.lineTo(1024, 1024);
        mRectPath.lineTo(0, 1024);
        mRectPath.close();
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(512, 0);
        mRectPath.lineTo(512, 1024);
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(0, 512);
        mRectPath.lineTo(1024, 512);
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(0, 0);
        mRectPath.lineTo(1024, 1024);
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(1024, 0);
        mRectPath.lineTo(0, 1024);
        canvas.drawPath(mRectPath, mRectPaint);

        mBezierPath.reset();
        mBezierPath.moveTo(518, 382);
        mBezierPath.quadTo(572, 385, 623, 389);
        mBezierPath.quadTo(758, 399, 900, 383);
        mBezierPath.quadTo(928, 379, 935, 390);
        mBezierPath.quadTo(944, 405, 930, 419);
        mBezierPath.quadTo(896, 452, 845, 475);
        mBezierPath.quadTo(829, 482, 798, 473);
        mBezierPath.quadTo(723, 460, 480, 434);
        mBezierPath.quadTo(180, 409, 137, 408);
        mBezierPath.quadTo(130, 408, 124, 408);
        mBezierPath.quadTo(108, 408, 106, 395);
        mBezierPath.quadTo(105, 380, 127, 363);
        mBezierPath.quadTo(146, 348, 183, 334);
        mBezierPath.quadTo(195, 330, 216, 338);
        mBezierPath.quadTo(232, 344, 306, 354);
        mBezierPath.quadTo(400, 373, 518, 382);
        mBezierPath.close();
//        Matrix matrix = new Matrix();
//        matrix.postScale(1, -1f);
//        matrix.postTranslate(0, 915);
//        canvas.setMatrix(matrix);
        mBezierPaint.setXfermode(xorMode);
        canvas.drawPath(mBezierPath, mBezierPaint);
        mBezierPaint.setXfermode(null);
        canvas.restore();

        canvas.saveLayer(0, 0, 1024, 1024, mRectPaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawColor(Color.TRANSPARENT);

        mBezierPath.reset();
        mBezierPath.moveTo(518, 382);
        mBezierPath.quadTo(572, 385, 623, 389);
        mBezierPath.quadTo(758, 399, 900, 383);
        mBezierPath.quadTo(928, 379, 935, 390);
        mBezierPath.quadTo(944, 405, 930, 419);
        mBezierPath.quadTo(896, 452, 845, 475);
        mBezierPath.quadTo(829, 482, 798, 473);
        mBezierPath.quadTo(723, 460, 480, 434);
        mBezierPath.quadTo(180, 409, 137, 408);
        mBezierPath.quadTo(130, 408, 124, 408);
        mBezierPath.quadTo(108, 408, 106, 395);
        mBezierPath.quadTo(105, 380, 127, 363);
        mBezierPath.quadTo(146, 348, 183, 334);
        mBezierPath.quadTo(195, 330, 216, 338);
        mBezierPath.quadTo(232, 344, 306, 354);
        mBezierPath.quadTo(400, 373, 518, 382);
        mBezierPath.close();
        canvas.drawPath(mBezierPath, mBezierPaint);

        mStrokesPath.moveTo(121, 393);
        mStrokesPath.lineTo(193, 372);
        mStrokesPath.lineTo(417, 402);
        mStrokesPath.lineTo(827, 434);
        mStrokesPath.lineTo(920, 401);
        mStrikesPaint.setXfermode(srcInMode);
        canvas.drawPath(mStrokesPath, mStrikesPaint);
        canvas.restore();
    }
}

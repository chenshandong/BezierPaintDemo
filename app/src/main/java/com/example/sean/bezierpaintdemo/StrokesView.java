package com.example.sean.bezierpaintdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sean on 2018/2/24.
 */

public class StrokesView extends View {

    private List<List<PointData>> mBezierData = new ArrayList<>();
    private List<List<List<Integer>>> mPathData = new ArrayList<>();
    private String mBezierStr = "[[{\"C\": \"M\", \"P\": [326, 667]}, {\"C\": \"Q\", \"P\": [283, 663, 312, 640]}, {\"C\": \"Q\", \"P\": [369, 610, 428, 623]}, {\"C\": \"Q\", \"P\": [543, 641, 665, 661]}, {\"C\": \"Q\", \"P\": [720, 671, 729, 678]}, {\"C\": \"Q\", \"P\": [739, 688, 735, 698]}, {\"C\": \"Q\", \"P\": [728, 711, 693, 722]}, {\"C\": \"Q\", \"P\": [660, 731, 561, 701]}, {\"C\": \"Q\", \"P\": [420, 673, 326, 667]}, {\"C\": \"Z\", \"P\": []}], [{\"C\": \"M\", \"P\": [329, 421]}, {\"C\": \"Q\", \"P\": [304, 417, 332, 392]}, {\"C\": \"Q\", \"P\": [348, 379, 385, 383]}, {\"C\": \"Q\", \"P\": [557, 405, 685, 416]}, {\"C\": \"Q\", \"P\": [721, 420, 709, 440]}, {\"C\": \"Q\", \"P\": [694, 462, 657, 472]}, {\"C\": \"Q\", \"P\": [621, 479, 558, 466]}, {\"C\": \"Q\", \"P\": [435, 441, 329, 421]}, {\"C\": \"Z\", \"P\": []}], [{\"C\": \"M\", \"P\": [130, 165]}, {\"C\": \"Q\", \"P\": [102, 162, 122, 139]}, {\"C\": \"Q\", \"P\": [140, 120, 163, 113]}, {\"C\": \"Q\", \"P\": [191, 104, 212, 110]}, {\"C\": \"Q\", \"P\": [515, 179, 929, 157]}, {\"C\": \"Q\", \"P\": [930, 158, 933, 157]}, {\"C\": \"Q\", \"P\": [960, 156, 967, 167]}, {\"C\": \"Q\", \"P\": [974, 183, 953, 201]}, {\"C\": \"Q\", \"P\": [884, 255, 835, 246]}, {\"C\": \"Q\", \"P\": [643, 210, 130, 165]}, {\"C\": \"Z\", \"P\": []}]]";
    private String mPathStr = "[[[316,655],[367,645],[416,648],[660,692],[722,692]],[[331,407],[375,405],[628,443],[657,443],[700,432]],[[127,152],[158,142],[195,139],[500,178],[846,204],[881,200],[955,174]]]";
    private int mWidth;

    private static final int BEZIER_WIDTH = 1; // 贝塞尔曲线线宽
    private static final int FILL_PAINT_WIDTH = 160; // 填充线宽
    private static final int Matt_WIDTH = 2; // 田字格线宽
    private Paint mBezierPaint = null;
    private Paint mBezierPaint2 = null;
    private Paint mStrokesPaint = null;
    private Paint mRectPaint = null;
    private Paint mPointPaint = null;
    private Path mBezierPath = null; // 贝塞尔曲线路径
    private Path mStrokesPath = null; // 画笔的曲线
    private Path mRectPath = null;
    private PorterDuffXfermode srcInMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    public int index = -1;
    public int doneIndex = -1;
    private StrokeHandler mHandler;

    private static final int HANDLER_WHAT = 100;
    private static final int RATE = 1; // 移动速率
    private static final int FRAME = 10;  // 帧
    public int mR = 0;  // 移动速率
    public int mRate = RATE;   // 速率
    public ArrayList<ArrayList<PointF>> mMovePoints; // 所有笔画的移动点集合
    public ArrayList<PointF> mInstantPoints; // 内部线段之间的移动点集
    private Handler mHandlerLoop;
    public boolean isLooping; // 单个笔画的循环控制
    public boolean isStart; // 整个字或者单笔画是否开始描绘

    public StrokesView(Context context) {
        super(context);
        init();
    }

    public StrokesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static class StrokeHandler extends Handler {

        private StrokesView strokesView;

        public StrokeHandler(StrokesView strokesView) {
            WeakReference<StrokesView> reference = new WeakReference<>(strokesView);
            this.strokesView = reference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_WHAT) {
                Log.e("test", "handleMessage2222");
                strokesView.mR += strokesView.mRate;
                if (strokesView.index == -1 ||
                        strokesView.mR >= strokesView.mMovePoints.get(strokesView.index).size()) {
                    removeMessages(HANDLER_WHAT);
                    strokesView.mR = 0;
                    strokesView.mInstantPoints.clear();
                    strokesView.mInstantPoints = null;
                    strokesView.isLooping = false;
                    strokesView.doneIndex = strokesView.index;
                    return;
                }
                strokesView.isLooping = true;
                // 增加内部移动点
                if (strokesView.mInstantPoints == null) {
                    strokesView.mInstantPoints = new ArrayList<>();
                }
                float x = strokesView.mMovePoints.get(strokesView.index).get(strokesView.mR).x;
                float y = strokesView.mMovePoints.get(strokesView.index).get(strokesView.mR).y;
                strokesView.mInstantPoints.add(new PointF(x, y));
                strokesView.invalidate();
            }
        }
    }

    private void init() {
        // 贝塞尔曲线画笔
        mBezierPaint = new Paint();
        mBezierPaint.setColor(Color.GRAY);
        mBezierPaint.setStrokeWidth(BEZIER_WIDTH);
        mBezierPaint.setStyle(Paint.Style.FILL);
        mBezierPaint.setAntiAlias(true);

        // 贝塞尔曲线画笔
        mBezierPaint2 = new Paint();
        mBezierPaint2.setColor(Color.GRAY);
        mBezierPaint2.setStyle(Paint.Style.FILL);
        mBezierPaint2.setAntiAlias(true);

        // 描绘的笔触
        mStrokesPaint = new Paint();
        mStrokesPaint.setColor(Color.RED);
        mStrokesPaint.setStrokeWidth(FILL_PAINT_WIDTH);
        mStrokesPaint.setStyle(Paint.Style.STROKE);
        mStrokesPaint.setStrokeCap(Paint.Cap.ROUND);
        mStrokesPaint.setAntiAlias(true);

        mRectPaint = new Paint();
        mRectPaint.setColor(Color.BLACK);
        mRectPaint.setStrokeWidth(Matt_WIDTH);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setAntiAlias(true);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.BLACK);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setAntiAlias(true);

        mBezierPath = new Path();
        mRectPath = new Path();
        mStrokesPath = new Path();

        mHandler = new StrokeHandler(this);
        HandlerThread handlerThread = new HandlerThread("strokes");
        handlerThread.start();
        mHandlerLoop = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                Log.e("test", "mHandlerLoop");
                if (!isStart){
                    Log.e("test", "return 了 index: " + index);
                    return;
                }

                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!isLooping || !isStart){
                        break;
                    }
                }

                if (index < mBezierData.size() - 1) {
                    index++;
                } else {
                    doneIndex = -1;
                    index = -1;
                    isStart = false;
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                });
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0) {
            mWidth = w;
        }
    }

    private void refreshDataFormat() {
        mPathData.clear();
        mBezierData.clear();
        if (!TextUtils.isEmpty(mBezierStr)) {
            JSONArray bezierArray = JSON.parseArray(mBezierStr);
            for (int i = 0; i < bezierArray.size(); i++) {
                String s = bezierArray.getJSONArray(i).toJSONString();
                List<PointData> pointData = JSON.parseArray(s, PointData.class);
                mBezierData.add(pointData);
            }
            Log.e("mBezierData", "size: " + mBezierData.size());
        }

        if (!TextUtils.isEmpty(mPathStr)) {
            JSONArray pathArray = JSON.parseArray(mPathStr);
            for (int i = 0; i < pathArray.size(); i++) {
                String s = pathArray.getJSONArray(i).toJSONString();
                JSONArray jsonArray = JSON.parseArray(s);
                List<List<Integer>> list1 = new ArrayList<>();
                for (int j = 0; j < jsonArray.size(); j++) {
                    String s1 = jsonArray.getJSONArray(j).toJSONString();
                    List<Integer> ps = JSON.parseArray(s1, Integer.class);
                    list1.add(ps);
                }
                mPathData.add(list1);
            }
            Log.e("mPathData", "size: " + mBezierData.size());
        }

        //调整数据比例 默认数据源以1024为比
        for (int i = 0; i < mBezierData.size(); i++) {
            List<PointData> points = mBezierData.get(i);
            for (int j = 0; j < points.size(); j++) {
                PointData pointData = points.get(j);
                for (int k = 0; k < pointData.getP().size(); k++) {
                    int p = pointData.getP().get(k);
//                        p / 1024 = x / w;
                    int newP = (int) (p * mWidth * 1.0f / 1024);
                    Log.e("newP", "newP: " + newP);
                    pointData.getP().set(k, newP);
                }
            }
        }

        for (int i = 0; i < mPathData.size(); i++) {
            List<List<Integer>> lists = mPathData.get(i);
            for (int j = 0; j < lists.size(); j++) {
                List<Integer> integers = lists.get(j);
                for (int k = 0; k < integers.size(); k++) {
                    int p = integers.get(k);
                    int newP = (int) (p * mWidth * 1.0f / 1024);
                    integers.set(k, newP);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayer(0, 0, mWidth, mWidth, mBezierPaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawColor(Color.WHITE);

        //计算加上线宽后的四边
        int left = Matt_WIDTH / 2;
        int top = Matt_WIDTH / 2;
        int right = mWidth - Matt_WIDTH / 2;
        int bottom = mWidth - Matt_WIDTH / 2;
        //画田字格
        mRectPath.reset();
        mRectPath.moveTo(left, top);
        mRectPath.lineTo(right, top);
        mRectPath.lineTo(right, bottom);
        mRectPath.lineTo(left, bottom);
        mRectPath.close();
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(mWidth / 2, 0);
        mRectPath.lineTo(mWidth / 2, mWidth);
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(0, mWidth / 2);
        mRectPath.lineTo(mWidth, mWidth / 2);
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(0, 0);
        mRectPath.lineTo(mWidth, mWidth);
        canvas.drawPath(mRectPath, mRectPaint);

        mRectPath.reset();
        mRectPath.moveTo(mWidth, 0);
        mRectPath.lineTo(0, mWidth);
        canvas.drawPath(mRectPath, mRectPaint);

        //画底色贝塞尔文字
        mBezierPath.reset();
        for (int i = 0; i < mBezierData.size(); i++) {
            List<PointData> points = mBezierData.get(i);
            for (int j = 0; j < points.size(); j++) {
                PointData pointData = points.get(j);
                if (pointData.getC().toLowerCase().equals("m")
                        && pointData.getP() != null
                        && pointData.getP().size() >= 2) {
                    mBezierPath.moveTo(pointData.getP().get(0), pointData.getP().get(1));
                }

                if (pointData.getC().toLowerCase().equals("q")
                        && pointData.getP() != null
                        && pointData.getP().size() >= 4) {
                    mBezierPath.quadTo(pointData.getP().get(0), pointData.getP().get(1),
                            pointData.getP().get(2), pointData.getP().get(3));
                }

                if (pointData.getC().toLowerCase().equals("z")) {
                    mBezierPath.close();
                }
            }
        }
        canvas.drawPath(mBezierPath, mBezierPaint);
        mBezierPaint.setXfermode(null);
        canvas.restore();
        //画点集合
//        if (mMovePoints != null) {
//            for (int i = 0; i < mMovePoints.size(); i++) {
//                ArrayList<PointF> pointFS = mMovePoints.get(i);
//                for (int j = 0; j < pointFS.size(); j++) {
//                    PointF pointF = pointFS.get(j);
//                    canvas.drawPoint(pointF.x, pointF.y,  mPointPaint);
//                }
//            }
//        }
        //画文字路径
        if (index < mPathData.size() && index != -1) {
            for (int i = index; i >= 0; i--) {
                mBezierPath.reset();
                canvas.saveLayer(0, 0, mWidth, mWidth, mBezierPaint, Canvas.ALL_SAVE_FLAG);
                List<PointData> points = mBezierData.get(i);
                for (int j = 0; j < points.size(); j++) {
                    PointData pointData = points.get(j);
                    if (pointData.getC().toLowerCase().equals("m")
                            && pointData.getP() != null
                            && pointData.getP().size() >= 2) {
                        mBezierPath.moveTo(pointData.getP().get(0), pointData.getP().get(1));
                    }

                    if (pointData.getC().toLowerCase().equals("q")
                            && pointData.getP() != null
                            && pointData.getP().size() >= 4) {
                        mBezierPath.quadTo(pointData.getP().get(0), pointData.getP().get(1),
                                pointData.getP().get(2), pointData.getP().get(3));
                    }

                    if (pointData.getC().toLowerCase().equals("z")) {
                        mBezierPath.close();
                    }
                }
                canvas.drawPath(mBezierPath, mBezierPaint2);

                List<List<Integer>> lists = mPathData.get(i);
                mStrokesPath.reset();
                if (i <= doneIndex) {
                    for (int j = 0; j < lists.size(); j++) {
                        List<Integer> pathPoints = lists.get(j);
                        if (j == 0) {
                            mStrokesPath.moveTo(pathPoints.get(0), pathPoints.get(1));
                        } else {
                            mStrokesPath.lineTo(pathPoints.get(0), pathPoints.get(1));
                        }
                    }
                    mStrokesPaint.setXfermode(srcInMode);
                    canvas.drawPath(mStrokesPath, mStrokesPaint);
                    mStrokesPaint.setXfermode(null);
                } else {
                    if (mInstantPoints != null) {
                        int tsize = mInstantPoints.size();
                        Log.e("tsize", "tsize: " + tsize);
                        for (int j = 0; j < tsize; j++) {
                            if (j == 0) {
                                mStrokesPath.moveTo(mInstantPoints.get(j).x, mInstantPoints.get(j).y);
                            } else {
                                mStrokesPath.lineTo(mInstantPoints.get(j).x, mInstantPoints.get(j).y);
                            }
                        }
                        mStrokesPaint.setXfermode(srcInMode);
                        canvas.drawPath(mStrokesPath, mStrokesPaint);
                        mStrokesPaint.setXfermode(null);
                    }
                }
                canvas.restore();
                //防止界面resume时重新绘制
                if (doneIndex != index && isStart) {
                    mHandler.removeMessages(HANDLER_WHAT);
                    mHandler.sendEmptyMessage(HANDLER_WHAT);
                }
            }
        }
        Log.e("getSaveCount", "getSaveCount: " + canvas.getSaveCount());
    }

    public void drawStrokes(String mBezierStr, String pathStr) {
        Log.e("test", "drawStrokes");
        mHandlerLoop.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        isStart = false;
        isLooping = false;
        index = -1;
        doneIndex = -1;
        mR = 0;
        if (mMovePoints != null) {
            mMovePoints.clear();
        }
        if (mInstantPoints != null) {
            mInstantPoints.clear();
        }
        if (!TextUtils.isEmpty(mBezierStr)){
            this.mBezierStr = mBezierStr;
        }
        if (!TextUtils.isEmpty(pathStr)){
            this.mPathStr = pathStr;
        }
        refreshDataFormat();
        buildBezierPoints();
        invalidate();
    }

    public void doNextStroke() {
        if (!isStart) {
            index = -1;
            doneIndex = -1;
            isStart = true;
        }
        mHandlerLoop.sendEmptyMessage(HANDLER_WHAT);
    }

    public void doAllStrokes() {
        index = -1;
        doneIndex = -1;
        isStart = true;
        isLooping = false;
        for (int i = 0; i < mPathData.size(); i++) {
            Log.e("test", "doAllStrokes");
            mHandlerLoop.sendEmptyMessage(HANDLER_WHAT);
        }
    }

    /**
     * 创建Bezier点集
     *
     * @return
     */
    private void buildBezierPoints() {
        if (mMovePoints == null) {
            mMovePoints = new ArrayList<>();
        } else {
            mMovePoints.clear();
        }
        for (int i = 0; i < mPathData.size(); i++) {
            ArrayList<PointF> points = new ArrayList<>();
            List<List<Integer>> lists = mPathData.get(i);
            int maxDis = 0;
            //计算最大距离 调整移动速率
            for (int j = 0; j < lists.size() - 1; j++) {
                int x1 = lists.get(j).get(0);
                int x2 = lists.get(j + 1).get(0);
                int y1 = lists.get(j).get(1);
                int y2 = lists.get(j + 1).get(1);
                int distance = getDistance(x1, y1, x2, y2);
                if (maxDis < distance){
                    maxDis = distance;
                }
            }
            Log.e("maxDis", "maxDis: " + maxDis);
            for (int j = 0; j < lists.size() - 1; j++) {
                int x1 = lists.get(j).get(0);
                int x2 = lists.get(j + 1).get(0);
                int y1 = lists.get(j).get(1);
                int y2 = lists.get(j + 1).get(1);
                int distance = getDistance(x1, y1, x2, y2);
                int newFRAME = (int) (distance * FRAME * 1.0f / maxDis);
                float delta = 1.0f / newFRAME;
                Log.e("FRAME", "FRAME: " + newFRAME);
                for (float t = 0; t <= 1; t += delta) {
                    // Bezier点集
                    points.add(new PointF(deCasteljauX(t, x1, x2), deCasteljauY(t, y1, y2)));
                }
            }
            mMovePoints.add(points);
        }
    }

    private int getDistance(int x1, int y1, int x2, int y2){
        return (int) Math.sqrt(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2));
    }

    /**
     * deCasteljau算法
     * 一阶求控制点
     * @param t 时间
     * @return
     */
    private float deCasteljauX(float t, int x1, int x2) {
        return (1 - t) * x1 + t * x2;
    }

    /**
     * deCasteljau算法
     * 一阶求控制点
     * @param t 时间
     * @return
     */
    private float deCasteljauY(float t, int y1, int y2) {
        return (1 - t) * y1 + t * y2;
    }
}

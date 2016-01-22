package com.zj.example.progressview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;

/**
 * @Author: zhengjiong
 * Date: 2014-10-15
 * Time: 21:49
 */
public class ProgressView extends View implements ValueAnimator.AnimatorUpdateListener{
    private boolean isOndrawFirstBitmap;
    private boolean isOndrawEndBitmap;

    private float firstCircleTop = 0f;
    private float endCircleTop = 0f;

    private float phaseX = 1f;

    private float strokeWidth = 5f;
    private float radius = 8f;

    private Paint mPaint;
    private Paint mCirclePaint;

    private int mBitmapHeight;

    private ObjectAnimator endCircleAnimator;
    private ObjectAnimator firstCircleAnimator;

    public ProgressView(Context context) {
        super(context);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setWillNotDraw(false);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, strokeWidth, dm);
        radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, dm);

        mPaint = new Paint();
        mCirclePaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setColor(0xFF6cb40e);
        mPaint.setStrokeWidth(strokeWidth);//设置线宽
        mPaint.setAlpha(255);

        mCirclePaint.setAntiAlias(true);
        //mCirclePaint.setColor(0xFFda007b);
        mCirclePaint.setStyle(Paint.Style.FILL);//設置為實心
        mCirclePaint.setColor(0xb2000000);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //getX=0.0 ,getLeft=0 width=480 ,getTop=0 ,getY=0.0
        //Log.i("zj", "getX=" + getX() + " ,getLeft=" + getLeft() + " width=" + getWidth() + " ,getTop=" + getTop() + " ,getY=" + getY());

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_location_2);
        mBitmapHeight = bitmap.getHeight();

        canvas.drawLine(bitmap.getWidth() / 2, radius + mBitmapHeight, Math.max((getWidth() - bitmap.getWidth() / 2) * phaseX, bitmap.getWidth() / 2), radius + mBitmapHeight, mPaint);
        canvas.drawCircle(bitmap.getWidth() / 2, radius+mBitmapHeight, radius, mCirclePaint);
        canvas.drawCircle(getWidth() - bitmap.getWidth()/2, radius+mBitmapHeight, radius, mCirclePaint);
        if (phaseX >= 0.1f || isOndrawFirstBitmap) {

            if (firstCircleAnimator == null) {
                firstCircleAnimator = ObjectAnimator.ofFloat(this, "firstCircleTop", 100f, 0f);
                firstCircleAnimator.setDuration(700);
                firstCircleAnimator.addUpdateListener(this);
                firstCircleAnimator.setInterpolator(new BounceInterpolator());
                firstCircleAnimator.start();
            }
            isOndrawFirstBitmap = true;
            canvas.drawBitmap(bitmap, 0, firstCircleTop, mPaint);
        }
        if (phaseX == 1.0f || isOndrawEndBitmap) {
            if (endCircleAnimator == null) {
                endCircleAnimator = ObjectAnimator.ofFloat(this, "endCircleTop", 100f, 0f);
                endCircleAnimator.setDuration(700);
                endCircleAnimator.addUpdateListener(this);
                endCircleAnimator.setInterpolator(new BounceInterpolator());
                endCircleAnimator.start();
            }
            isOndrawEndBitmap = true;
            canvas.drawBitmap(bitmap, getWidth() - bitmap.getWidth(), endCircleTop, mPaint);
        }
    }

    public void animateX() {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(this, "phaseX", 0, 1);
        animatorX.setDuration(1500);
        animatorX.addUpdateListener(this);
        animatorX.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorX.start();

    }

    /**
     * EXACTLY:表示我们设置了MATCH_PARENT或者一个准确的数值，含义是父布局要给子布局一个确切的大小。
     * AT_MOST:表示子布局将被限制在一个最大值之内，通常是子布局设置了wrap_content,或者match_parent
     * UNSPECIFIED:表示子布局想要多大就可以要多大，通常出现在AdapterView中item的heightMode中。
     *
     *
     * 比onDraw先执行
     *
     * 一个MeasureSpec封装了父布局传递给子布局的布局要求，每个MeasureSpec代表了一组宽度和高度的要求。
     * 一个MeasureSpec由大小和模式组成
     * 它有三种模式：UNSPECIFIED(未指定),父元素不对子元素施加任何束缚，子元素可以得到任意想要的大小;
     *              EXACTLY(完全)，父元素决定自元素的确切大小，子元素将被限定在给定的边界里而忽略它本身大小；
     *              AT_MOST(至多)，子元素至多达到指定大小的值。
     *
     * 　　它常用的三个函数： 　　
     * 1.static int getMode(int measureSpec):根据提供的测量值(格式)提取模式(上述三个模式之一)
     * 2.static int getSize(int measureSpec):根据提供的测量值(格式)提取大小值(这个大小也就是我们通常所说的大小)
     * 3.static int makeMeasureSpec(int size,int mode):根据提供的大小值和模式创建一个测量值(格式)
     *
     *
     *  思路是这样的：我们首先判断是不是EXACTLY模式，如果是，那就可以直接设置值了，如果不是，
     *  我们先按照UNSPECIFIED模式处理，让子布局得到自己想要的最大值，然后判断是否是AT_MOST模式，
     *  来做最后的限制。
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //onMeasure width = 480 ,onMeasure height = 618
        Log.i("zj", "onMeasure width = " + getMeasuredWidth() + " ,onMeasure height = " + getMeasuredHeight());

        int width;
        int height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //widthSize = 480 ,heightSize = 618
        Log.i("zj", "widthSize = " + widthSize + " ,heightSize = " + heightSize);

        //widthMode=EXACTLY
        if (widthMode == MeasureSpec.EXACTLY) {
            Log.i("zj", "widthMode=EXACTLY");
            width = widthSize;
        }else{
            throw new IllegalArgumentException("寬度只能設置成match_parent或者指定dp大小");
        }

        //View.resolveSize()
        /**
         * 思路是这样的：我们首先判断是不是EXACTLY模式，如果是，那就可以直接设置值了，如果不是，
         *  我们先按照UNSPECIFIED模式处理，让子布局得到自己想要的最大值，然后判断是否是AT_MOST模式，
         *  来做最后的限制。
         */
        if (heightMode == MeasureSpec.EXACTLY) {
            Log.i("zj", "heightMode=EXACTLY");
            height = heightSize;
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_location_2);
            height = (int) (bitmap.getHeight() + radius * 2);
            if (heightMode == MeasureSpec.AT_MOST) {
                Log.i("zj", "heightMode=AT_MOST");
                height = Math.min(height, heightSize);
            } else {
                Log.i("zj", "heightMode=UNSPECIFIED");
            }
        }
        setMeasuredDimension(width, height);
    }

    public static int resolveSizeAndState(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
        //Log.i("zj", "onAnimationUpdate phaseX=" + phaseX + " ,mEndCircleTop=" + endCircleTop);
    }

    /**
     * 思路是这样的：我们首先判断是不是EXACTLY模式，如果是，那就可以直接设置值了，如果不是，
     *  我们先按照UNSPECIFIED模式处理，让子布局得到自己想要的最大值，然后判断是否是AT_MOST模式，
     *  来做最后的限制。
     */
    /*private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) mPaint.measureText(text) + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize);// 60,480
            }
        }

        return result;
    }*/

    /*private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + mPaint.descent()) + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }*/

    public float getPhaseX() {
        return phaseX;
    }

    public void setPhaseX(float phaseX) {
        this.phaseX = phaseX;
    }

    public float getEndCircleTop() {
        return endCircleTop;
    }

    public void setEndCircleTop(float endCircleTop) {
        this.endCircleTop = endCircleTop;
    }

    public float getFirstCircleTop() {
        return firstCircleTop;
    }

    public void setFirstCircleTop(float firstCircleTop) {
        this.firstCircleTop = firstCircleTop;
    }
}

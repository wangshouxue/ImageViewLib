package com.example.imageviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatButton;

/**
 * @author:wangshouxue
 * @date:2019-10-16 10:20
 * @description:圆角button,支持设置圆角大小，分开设置每个角，边框颜色,宽度,背景色
 */
public class RoundButton extends AppCompatButton {
    private Context mContext;
    GradientDrawable gd=new GradientDrawable();
    private int mTopLeft,mTopRight,mBottomLeft,mBottomRight;
    private int mBackgroundColor;
    private int mBorderWidth;//边框宽
    private int mBorderColor;//边框颜色
    private int mPressAlpha;//按下的透明度值
    private int mUnClickAlpha;
    private boolean mIsClickable;

    public RoundButton(Context context) {
        this(context, null, 0);
    }

    public RoundButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundButton);
        mBackgroundColor = ta.getColor(R.styleable.RoundButton_rb_backgroundColor, Color.parseColor("#8BC34A"));
        mBorderWidth = ta.getDimensionPixelSize(R.styleable.RoundButton_rb_borderWidth, dp2px(0));
        mBorderColor = ta.getColor(R.styleable.RoundButton_rb_borderColor,Color.TRANSPARENT);
        float pressAlpha = ta.getFloat(R.styleable.RoundButton_rb_pressAlpha,0.6f);
        float noClickAlpha = ta.getFloat(R.styleable.RoundButton_rb_unClickAlpha,0.6f);

        if (pressAlpha<0){
            pressAlpha=0f;
        }
        if (pressAlpha>1){
            pressAlpha=1f;
        }
        if (noClickAlpha<0){
            noClickAlpha=0f;
        }
        if (noClickAlpha>1){
            noClickAlpha=1f;
        }
        mPressAlpha= (int) (pressAlpha*255);
        mUnClickAlpha= (int) (noClickAlpha*255);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        gd.setColor(mBackgroundColor);
        gd.setStroke(mBorderWidth,mBorderColor);
        //左上，右上，右下，左下
        float[] f=new float[]{mTopLeft,mTopLeft,mTopRight,mTopRight,
                mBottomRight,mBottomRight,mBottomLeft,mBottomLeft};
        gd.setCornerRadii(f);
        setBackgroundDrawable(gd);
        setGravity(Gravity.CENTER);

        super.onDraw(canvas);
    }

    @Override
    public void setClickable(boolean clickable) {
        mIsClickable=clickable;
        if (gd!=null){
            if (clickable){
                gd.setAlpha(255);
            }else {
                gd.setAlpha(mUnClickAlpha);
            }
        }
        super.setClickable(clickable);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsClickable){
            if (gd!=null){
                if (event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_MOVE){
                    gd.setAlpha(mPressAlpha);
                }else {
                    gd.setAlpha(255);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void setCorner(int corner){
        this.mTopLeft=dp2px(corner);
        this.mTopRight=dp2px(corner);
        this.mBottomLeft=dp2px(corner);
        this.mBottomRight=dp2px(corner);
    }
    public void setCorner(int mTopLeft,int mTopRight,int mBottomLeft,int mBottomRight){
        this.mTopLeft=dp2px(mTopLeft);
        this.mTopRight=dp2px(mTopRight);
        this.mBottomLeft=dp2px(mBottomLeft);
        this.mBottomRight=dp2px(mBottomRight);
    }

    public void setmBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    public void setmBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
    }

    public void setmBorderWidth(int mBorderWidth) {
        this.mBorderWidth = dp2px(mBorderWidth);
    }

    protected int dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}

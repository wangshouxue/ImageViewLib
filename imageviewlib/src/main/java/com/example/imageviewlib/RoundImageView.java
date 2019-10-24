package com.example.imageviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author:wangshouxue
 * @date:2019-10-16 16:32
 * @description:类作用
 */
public class RoundImageView extends AppCompatImageView {
    private Context mContext;
    private boolean mIsCircle;
    //角
    private int mCorner;
    private int mCornerTopLeft;
    private int mCornerTopRight;
    private int mCornerBottomLeft;
    private int mCornerBottomRight;
    //边框
    private int mBorderWidth;
    private int mBorderColor;
    private float mCornerBorderRadii[]= new float[8];
    private RectF mBorderRect = new RectF();
    private Paint mBorderPaint;
    //bitmap
    private float mCornerBitmapRadii[]= new float[8];
    private Paint mBitmapPaint;
    private final RectF mDrawableRect = new RectF();

    public RoundImageView(Context context) {
        this(context,null,0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        mIsCircle = a.getBoolean(R.styleable.RoundImageView_is_circle, false);
        mCorner = a.getDimensionPixelSize(R.styleable.RoundImageView_corner_radius, -1);
        mCornerTopLeft = a.getDimensionPixelSize(R.styleable.RoundImageView_corner_radius_top_left, 0);
        mCornerTopRight = a.getDimensionPixelSize(R.styleable.RoundImageView_corner_radius_top_right, 0);
        mCornerBottomLeft = a.getDimensionPixelSize(R.styleable.RoundImageView_corner_radius_bottom_left, 0);
        mCornerBottomRight = a.getDimensionPixelSize(R.styleable.RoundImageView_corner_radius_bottom_right, 0);
        mBorderWidth = a.getDimensionPixelSize(R.styleable.RoundImageView_border_width, 0);
        mBorderColor = a.getColor(R.styleable.RoundImageView_border_color, Color.BLACK);

        a.recycle();

        init();
    }

    private void init() {
        //统一设置圆角弧度优先
        updateCornerBorderRadii();
        updateCornerBitmapRadii();

        //border
        if (mBorderPaint == null) {
            mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mBorderPaint.setStyle(Paint.Style.STROKE);
        //bitmap
        if (mBitmapPaint == null) {
            mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
    }
    //更新bitmap圆角弧度
    private void updateCornerBitmapRadii() {
        if (mCorner >= 0) {
            for (int i = 0; i < mCornerBitmapRadii.length; i++) {
                mCornerBitmapRadii[i] = mCorner;
            }
            return;
        }

        if (mCorner < 0) {
            mCornerBitmapRadii[0] = mCornerTopLeft;
            mCornerBitmapRadii[1] = mCornerTopLeft;
            mCornerBitmapRadii[2] = mCornerTopRight;
            mCornerBitmapRadii[3] = mCornerTopRight;
            mCornerBitmapRadii[4] = mCornerBottomRight;
            mCornerBitmapRadii[5] = mCornerBottomRight;
            mCornerBitmapRadii[6] = mCornerBottomLeft;
            mCornerBitmapRadii[7] = mCornerBottomLeft;
            return;
        }

    }
    //更新border圆角弧度
    private void updateCornerBorderRadii() {
        if (mCorner >= 0) {
            for (int i = 0; i < mCornerBorderRadii.length; i++) {
                mCornerBorderRadii[i] = mCorner == 0 ? mCorner : mCorner + mBorderWidth;
            }
            return;
        }

        if (mCorner < 0) {
            mCornerBorderRadii[0] = mCornerTopLeft == 0 ? mCornerTopLeft : mCornerTopLeft + mBorderWidth;
            mCornerBorderRadii[1] = mCornerTopLeft == 0 ? mCornerTopLeft : mCornerTopLeft + mBorderWidth;
            mCornerBorderRadii[2] = mCornerTopRight == 0 ? mCornerTopRight : mCornerTopRight + mBorderWidth;
            mCornerBorderRadii[3] = mCornerTopRight == 0 ? mCornerTopRight : mCornerTopRight + mBorderWidth;
            mCornerBorderRadii[4] = mCornerBottomRight == 0 ? mCornerBottomRight : mCornerBottomRight + mBorderWidth;
            mCornerBorderRadii[5] = mCornerBottomRight == 0 ? mCornerBottomRight : mCornerBottomRight + mBorderWidth;
            mCornerBorderRadii[6] = mCornerBottomLeft == 0 ? mCornerBottomLeft : mCornerBottomLeft + mBorderWidth;
            mCornerBorderRadii[7] = mCornerBottomLeft == 0 ? mCornerBottomLeft : mCornerBottomLeft + mBorderWidth;
            return;
        }

    }
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        //绘制图片
        drawBitmap(canvas);
        //绘制边框
        drawBorder(canvas);

    }
    private void drawBitmap(Canvas canvas) {
        if (getDrawable() != null) {
            //setLayerType(View.LAYER_TYPE_SOFTWARE, mBitmapPaint);//禁止硬件加速
            int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), mBitmapPaint, Canvas.ALL_SAVE_FLAG);//离屏绘制

            //drawable
            Drawable drawable = getDrawable();
            int bmpW = drawable.getIntrinsicWidth();
            int bmpH = drawable.getIntrinsicHeight();
            //matrix
            Matrix matrix;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                matrix = getMatrix();
            } else {
                matrix = new Matrix();
                matrix.set(getMatrix());
            }

            //ScaleType
            ScaleType scaleType = getScaleType();

            //图形轮廓
            Bitmap dst = makeDst(getWidth(), getHeight());//创建
            canvas.drawBitmap(dst, 0, 0, mBitmapPaint);//绘制

            //设置混合模型
            mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            //绘制展示图
            drawBitmapSrc(canvas, drawable, matrix, scaleType, bmpW, bmpH, getWidth(), getHeight());

            mBitmapPaint.setXfermode(null);
            canvas.restoreToCount(layerId); //离屏绘制
        }
    }
    /**
     * 绘制图片
     * 参考源码 configureBounds()
     * @param bmpW      图片宽
     * @param bmpH      图片高
     * @param w         控件宽
     * @param h         控件高
     */
    private void drawBitmapSrc(Canvas canvas, Drawable drawable, Matrix matrix, ScaleType scaleType, int bmpW, int bmpH, int w, int h) {
        /**
         * 支持padding 考虑边框宽度
         */
        int paddingLeft = getPaddingLeft() + mBorderWidth;
        int paddingTop = getPaddingTop() + mBorderWidth;
        int paddingRight = getPaddingRight() + mBorderWidth;
        int paddingBottom = getPaddingBottom() + mBorderWidth;
        /**
         * 实际宽高
         */
        float actualW = w - paddingLeft - paddingRight;
        float actualH = h - paddingTop - paddingBottom;
        /**
         * 宽高缩放比例
         */
        float scaleW = actualW / w;
        float scaleH = actualH / h;

        Bitmap viewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);//根据view大小创建bitmap
        Canvas viewCanvas = new Canvas(viewBitmap);//根据 viewBitmap 大小创建 canvas 画布
        viewCanvas.translate(paddingLeft, paddingTop);//移动画布,必须先于缩放，避免误差
        viewCanvas.scale(scaleW, scaleH);//缩放画布
        /**
         * 根据 scaleType 处理图片 参考 ImageView 源码 configureBounds()
         */
        float scale;
        float dx = 0, dy = 0;
        switch (scaleType) {
            default:
            case CENTER:
                matrix.setTranslate(Math.round((w - bmpW) * 0.5f), Math.round((h - bmpH) * 0.5f));
                break;
            case FIT_START:
            case FIT_END:
            case FIT_CENTER:
                RectF mTempSrc = new RectF(0, 0, bmpW, bmpH);
                RectF mTempDst = new RectF(0, 0, w, h);
                matrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(scaleType));
                break;
            case FIT_XY:
                drawable.setBounds(0, 0, w, h);
                matrix = null;
                break;
            case CENTER_CROP:
                if (bmpW * h > w * bmpH) {
                    scale = (float) h / (float) bmpH;
                    dx = (w - bmpW * scale) * 0.5f;
                } else {
                    scale = (float) w / (float) bmpW;
                    dy = (h - bmpH * scale) * 0.5f;
                }

                matrix.setScale(scale, scale);
                matrix.postTranslate(Math.round(dx), Math.round(dy));
                break;
            case CENTER_INSIDE:
                if (bmpW <= w && bmpH <= h) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) w / (float) bmpW, (float) h / (float) bmpH);
                }

                dx = Math.round((w - bmpW * scale) * 0.5f);
                dy = Math.round((h - bmpH * scale) * 0.5f);

                matrix.setScale(scale, scale);
                matrix.postTranslate(dx, dy);
                break;
            case MATRIX:
                if (matrix.isIdentity()) {
                    matrix = null;
                }
                break;
        }

        viewCanvas.concat(matrix);//设置变化矩阵
        drawable.draw(viewCanvas);//绘制drawable
        canvas.drawBitmap(viewBitmap, 0, 0, mBitmapPaint);
    }
    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        /**
         * 根据源码改造  sS2FArray[st.nativeInt - 1]
         */
        switch (st) {
            case FIT_XY:
                return Matrix.ScaleToFit.FILL;
            case FIT_START:
                return Matrix.ScaleToFit.START;
            case FIT_END:
                return Matrix.ScaleToFit.END;
            case FIT_CENTER:
            default:
                return Matrix.ScaleToFit.CENTER;
        }
    }
    /**
     * 获取目标资源bitmap(形状)
     * @param w
     * @param h
     * @return
     */
    private Bitmap makeDst(int w, int h) {
        updateCornerBitmapRadii();
        updateDrawableAndBorderRect();

        if (mIsCircle) {//圆形
            return makeDstCircle(w, h, mDrawableRect);
        } else {//圆角
            return makeDstRound(w, h, mDrawableRect, mCornerBitmapRadii);
        }
    }
    /**
     * 更新drawable和border Rect
     */
    private void updateDrawableAndBorderRect() {
        float half = mBorderWidth / 2;
        if (mIsCircle) {//圆形
            mBorderRect.set(0, 0, getWidth(), getHeight());//边框Rect圆形
            mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth, mBorderRect.height() - mBorderWidth);//drawableRect
        } else {//圆角
            mBorderRect.set(half, half, getWidth() - half, getHeight() - half);//边框Rect圆角
            mDrawableRect.set(mBorderRect.left + half, mBorderRect.top + half, mBorderRect.right - half, mBorderRect.bottom - half);//drawableRect
        }
    }

    /**
     * 获取目标资源bitmap-圆形
     */
    private Bitmap makeDstCircle(int w, int h, RectF rect) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, Math.min(rect.width() / 2, rect.width() / 2), paint);
        return bitmap;
    }

    /**
     * 获取目标资源bitmap-圆角
     */
    private Bitmap makeDstRound(int w, int h, RectF rect, float[] radii) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        Path path = new Path();
        path.addRoundRect(rect, radii, Path.Direction.CW);
        canvas.drawPath(path, paint);
        return bitmap;
    }
    /**
     * 绘制边框
     * @param canvas
     */
    private void drawBorder(Canvas canvas) {
        if (mBorderWidth > 0) {
            //重新设置 color & width
            mBorderPaint.setColor(mBorderColor);
            mBorderPaint.setStrokeWidth(mBorderWidth);
            if (mIsCircle) {
                float borderRadiusX = (mBorderRect.width() - mBorderWidth) / 2;
                float borderRadiusY = (mBorderRect.height() - mBorderWidth) / 2;
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, Math.min(borderRadiusX, borderRadiusY), mBorderPaint);
            } else {
                updateCornerBorderRadii();
                Path path = new Path();
                path.addRoundRect(mBorderRect, mCornerBorderRadii, Path.Direction.CW);
                canvas.drawPath(path, mBorderPaint);
            }
        }
    }
    //设置是否为圆形
    public void setmIsCircle(boolean mIsCircle) {
        this.mIsCircle = mIsCircle;
        invalidate();
    }

    //设置圆角大小
    public void setmCorner(int mCorner) {
        this.mCorner = dp2px(mCorner);
        init();
        invalidate();
    }

    //分别设置四个角的大小
    public void setmCorner(int mCornerTopLeft,int mCornerTopRight,int mCornerBottomLeft,int mCornerBottomRight) {
        this.mCornerTopLeft = dp2px(mCornerTopLeft);
        this.mCornerTopRight = dp2px(mCornerTopRight);
        this.mCornerBottomLeft = dp2px(mCornerBottomLeft);
        this.mCornerBottomRight = dp2px(mCornerBottomRight);
        init();
        invalidate();
    }

    //设置边框的颜色
    public void setmBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
        invalidate();
    }

    //设置边框的宽
    public void setmBorderWidth(int mBorderWidth) {
        this.mBorderWidth = dp2px(mBorderWidth);
        invalidate();
    }
    protected int dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}

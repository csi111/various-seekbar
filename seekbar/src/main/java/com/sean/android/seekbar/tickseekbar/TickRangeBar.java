package com.sean.android.seekbar.tickseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.sean.android.seekbar.AbstractSeekBar;
import com.sean.android.seekbar.ProgressLine;
import com.sean.android.seekbar.R;
import com.sean.android.seekbar.Thumb;
import com.sean.android.seekbar.util.BitmapUtil;

/**
 * Created by Seonil on 2016-12-13.
 * <p>
 * Reference https://github.com/edmodo/range-bar
 */

public class TickRangeBar extends AbstractSeekBar {

    private static final int DEFAULT_TICK_COUNT = 2;
    private static final int DEFAULT_TICK_CHILD_COUNT = 1;
    private static final float DEFAULT_TICK_HEIGHT_DP = 24;
    private static final float DEFAULT_CHILD_TICK_HEIGHT_DP = 12;
    private static final float DEFAULT_SEEKBAR_WEIGHT = 2;
    private static final int DEFAULT_THUMB_IMAGE = R.drawable.seek_thumb_normal;
    private static final int DEFAULT_THUMB_PRESSED = R.drawable.seek_thumb_pressed;
    private static final int DEFAULT_SEEKBAR_WIDTH = 300;
    private static final int DEFAULT_SEEKBAR_HEIGHT = 100;
    private static final int DEFAULT_COLOR = Color.LTGRAY;
    private static final int DEFAULT_ACTIVE_COLOR = Color.CYAN;
    private static final int DEFAULT_THUMB_COLOR = -1;
    private static final int DEFAULT_THUMB_COLOR_PRESSED = -1;
    private static final float DEFAULT_THUMB_RADIUS = -1;


    private int currentThumbindex = 0; //Thumb의 현재 index 값
    private int tickCount = 0; //Tick total count
    private int tickChildCount = 0; //ChildTick total count
    private float tickHeight; //Tick Height DP
    private float tickChildHeight; //Tick Height DP;

    private float lineWeight;

    private int defaultColor; //Seekbar Color
    private int activeColor; //Seekbar Color on Activated

    private Bitmap thumbImage; //Thumb Image
    private Bitmap thumbImagePressed; // Thumb Image on pressed

    private float thumbRadius;
    private int thumbColor;
    private int thumbColorPressed;

    private float internalPad;

    private TickSeekBarLine seekBarLine;
    private ProgressLine progressLine;
    private Thumb thumb;

    private boolean isActiveSeekbarVisible;


    private int scaledTouchSlop;

    private OnTickSeekBarChangeListener onTickSeekBarChangeListener;

    public TickRangeBar(Context context) {
        super(context);
        init(context, null);
    }

    public TickRangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TickRangeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TickRangeBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = DEFAULT_SEEKBAR_WIDTH;
        int height = DEFAULT_SEEKBAR_HEIGHT;
        if (thumbImage != null) {
            height = thumbImage.getHeight();
        }

        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = View.MeasureSpec.getSize(widthMeasureSpec);
        }

        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec));
        }


        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float yPos = h / 2f;

        thumb = new Thumb(getResources(), yPos, thumbRadius, thumbImage, thumbImagePressed, thumbColor, thumbColorPressed);

        internalPad = thumb.getThumbHalfWidthNormal();

        float barWidth = w - 2 * internalPad;

        seekBarLine = new TickSeekBarLine(getResources(), internalPad, yPos, barWidth, tickCount, tickChildCount, tickHeight, tickChildHeight, lineWeight, defaultColor);

        progressLine = new ProgressLine(getResources(), yPos, lineWeight, activeColor);

        thumb.setxPosition(internalPad + (currentThumbindex / (float) tickCount) * barWidth);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        seekBarLine.draw(canvas);

        if (isActiveSeekbarVisible) {
            progressLine.draw(canvas, internalPad, thumb.getXPosition());
        }

        thumb.draw(canvas);
    }

    private void init(Context context, AttributeSet attrs) {
        int thumbNormal = DEFAULT_THUMB_IMAGE;
        int thumbPressed = DEFAULT_THUMB_PRESSED;


        if (attrs == null) {
            this.tickCount = DEFAULT_TICK_COUNT;
            this.tickChildCount = DEFAULT_TICK_CHILD_COUNT;
            this.tickHeight = DEFAULT_TICK_HEIGHT_DP;
            this.tickChildHeight = DEFAULT_CHILD_TICK_HEIGHT_DP;
            this.defaultColor = DEFAULT_COLOR;
            this.activeColor = DEFAULT_ACTIVE_COLOR;
            this.lineWeight = DEFAULT_SEEKBAR_WEIGHT;
            this.isActiveSeekbarVisible = false;
        } else {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TickSeekBar, 0, 0);
            try {
                Integer tickCount = typedArray.getInteger(R.styleable.TickSeekBar_tickCount, DEFAULT_TICK_COUNT);
                Integer tickChildCount = typedArray.getInteger(R.styleable.TickSeekBar_tickChildCount, DEFAULT_TICK_CHILD_COUNT);


                if (isValidTickCount(tickCount)) {
                    this.tickCount = tickCount;
                } else {
                    this.tickCount = DEFAULT_TICK_COUNT; // Default 데이터로 다시 변경
                }

                if (isValidTickChildCount(tickChildCount)) {
                    this.tickChildCount = tickChildCount;
                } else {
                    this.tickChildCount = DEFAULT_TICK_CHILD_COUNT;
                }
                this.currentThumbindex = 0;

                tickHeight = typedArray.getDimension(R.styleable.TickSeekBar_tickHeight, DEFAULT_TICK_HEIGHT_DP);
                tickChildHeight = typedArray.getDimension(R.styleable.TickSeekBar_tickChildHeight, DEFAULT_CHILD_TICK_HEIGHT_DP);
                defaultColor = typedArray.getColor(R.styleable.TickSeekBar_tickBarColor, DEFAULT_COLOR);
                activeColor = typedArray.getColor(R.styleable.TickSeekBar_tickBarActiveColor, DEFAULT_ACTIVE_COLOR);

                lineWeight = typedArray.getDimension(R.styleable.TickSeekBar_tickBarWeight, DEFAULT_SEEKBAR_WEIGHT);

                Drawable thumbImage = typedArray.getDrawable(R.styleable.TickSeekBar_tickBarThumbImage);

                if (thumbImage != null) {
                    this.thumbImage = BitmapUtil.drawableToBitmap(thumbImage);
                }

                Drawable thumbPressedImage = typedArray.getDrawable(R.styleable.TickSeekBar_tickBarThumbImagePressed);

                if (thumbPressedImage != null) {
                    this.thumbImagePressed = BitmapUtil.drawableToBitmap(thumbPressedImage);
                }

                thumbRadius = typedArray.getDimension(R.styleable.TickSeekBar_tickBarThumbRadius, DEFAULT_THUMB_RADIUS);
                thumbColor = typedArray.getColor(R.styleable.TickSeekBar_tickBarThumbColor, DEFAULT_THUMB_COLOR);
                thumbColorPressed = typedArray.getColor(R.styleable.TickSeekBar_tickBarThumbColorPressed, DEFAULT_THUMB_COLOR_PRESSED);
                isActiveSeekbarVisible = typedArray.getBoolean(R.styleable.TickSeekBar_tickBarActiveVisible, false);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                typedArray.recycle();
            }
        }

        if (thumbImage == null) {
            thumbImage = BitmapFactory.decodeResource(getResources(), thumbNormal);
        }
        if (thumbImagePressed == null) {
            thumbImagePressed = BitmapFactory.decodeResource(getResources(), thumbPressed);
        }

        scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }


    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(event.getPointerCount() - 1); // 현재 Touch한 Point의 ID 값
                pointerIndex = event.findPointerIndex(activePointerId);
                downMotionX = event.getX(pointerIndex);
                downMotionY = event.getY(pointerIndex);

                if (thumb.isInThumbRange(downMotionX, downMotionY)) {
                    setPressed(true);
                    thumb.pressThumb();
                    invalidate();
                    trackTouchEvent(event);
                    attemptClaimDrag();


                    if (onTickSeekBarChangeListener != null) {
                        onTickSeekBarChangeListener.onStartTrackingTouch(this);
                    }

                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (thumb.isPressed()) {
                    trackTouchEvent(event);
                } else {
                    setPressed(true);
                    invalidate();
                    trackTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (thumb.isPressed()) {
                    setPressed(false);
                }

                trackTouchEvent(event);
                releaseThumb(thumb);
                attemptReleaseDrag();
                if (onTickSeekBarChangeListener != null) {
                    onTickSeekBarChangeListener.onStopTrackingTouch(this);
                    onTickSeekBarChangeListener.onTickIndexChanged(this, currentThumbindex);
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;
        }

        return true;
    }

    public void setTickCount(int count) {
        if (isValidTickCount(count)) {
            tickCount = count;


            if (indexOutOfRange(currentThumbindex)) {
                currentThumbindex = 0;

                if (onTickSeekBarChangeListener != null) {
                    onTickSeekBarChangeListener.onTickIndexChanged(this, currentThumbindex);
                }
            }
        }

        reDrawLine();
        reDrawThumb();
    }

    public void setTickChildCount(int count) {
        if (isValidTickChildCount(count)) {
            tickChildCount = count;


            if (indexOutOfRange(currentThumbindex)) {
                currentThumbindex = 0;

                if (onTickSeekBarChangeListener != null) {
                    onTickSeekBarChangeListener.onTickIndexChanged(this, currentThumbindex);
                }
            }
        }

        reDrawLine();
        reDrawThumb();
    }

    public void setTickHeight(float height) {
        tickHeight = height;
        reDrawLine();
    }

    public void setChildTickHeight(float height) {
        tickChildHeight = height;
        reDrawLine();
    }

    public void setLineWeight(float weight) {
        lineWeight = weight;
        reDrawLine();
    }

    public void setLineColor(int color) {
        defaultColor = color;
        reDrawLine();
    }

    public void setThumbRadius(float radius) {
        thumbRadius = radius;
        reDrawThumb();
    }

    public void setThumbColor(int color) {
        thumbColor = color;
        reDrawThumb();
    }

    public void setThumbColorPressed(int color) {
        thumbColorPressed = color;
        reDrawThumb();
    }

    public void setThumbImage(int resourceId) {
        try {
            Drawable drawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = getResources().getDrawable(resourceId, null);
            } else {
                drawable = getResources().getDrawable(resourceId);
            }

            setThumbImage(BitmapUtil.drawableToBitmap(drawable));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setThumbImage(Bitmap bitmap) {
        thumbImage = bitmap;
        reDrawThumb();
    }

    public void setThumbImagePressed(int resourceId) {
        try {
            Drawable drawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = getResources().getDrawable(resourceId, null);
            } else {
                drawable = getResources().getDrawable(resourceId);
            }
            setThumbImagePressed(BitmapUtil.drawableToBitmap(drawable));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setThumbImagePressed(Bitmap bitmap) {
        thumbImagePressed = bitmap;
        reDrawThumb();
    }

    public void setThumbIndex(int index) {
        if (indexOutOfRange(index)) {
            return;
        } else {
            currentThumbindex = index;

            reDrawThumb();
            if (onTickSeekBarChangeListener != null) {
                onTickSeekBarChangeListener.onTickIndexChanged(this, currentThumbindex);
            }
        }

        invalidate();
        requestLayout();
    }


    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(activePointerId);
        try {
            final float x = event.getX(pointerIndex);

            if (x < seekBarLine.getLeftXPosition() || x > seekBarLine.getRightXPosition()) {
                return;
            } else {
                float nearestTickX = seekBarLine.getNearestTickCoodinate(x);
                int nearestTickIndex = seekBarLine.getNearestTickindex(x);
                thumb.setxPosition(nearestTickX);
                currentThumbindex = nearestTickIndex;
                invalidate();


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseThumb(Thumb thumb) {
        thumb.releaseThumb();
        invalidate();
    }

    private boolean isValidTickCount(int tickCount) {
        return (tickCount > 1);
    }

    private boolean isValidTickChildCount(int tickChildCount) {
        return (tickChildCount > 1);
    }

    private boolean indexOutOfRange(int index) {
        return (index < 1 || index >= tickCount);
    }

    private void reDrawLine() {
        seekBarLine = new TickSeekBarLine(getResources(), internalPad, getYPosition(), getBarWidth(), tickCount, tickChildCount, tickHeight, tickChildHeight, lineWeight, defaultColor);

        invalidate();
    }

    private void reDrawThumb() {
        thumb = new Thumb(getResources(), getYPosition(), thumbRadius, thumbImage, thumbImagePressed, thumbColor, thumbColorPressed);

        float barWidth = getBarWidth();
        int count = (tickCount - 1) * tickChildCount;
        thumb.setxPosition(internalPad + (currentThumbindex / count) * barWidth);
        invalidate();
    }

    private float getYPosition() {
        return getHeight() / 2f;
    }

    private float getBarWidth() {
        return getWidth() - 2 * internalPad;
    }

    public void setOnTickSeekBarChangeListener(OnTickSeekBarChangeListener onTickSeekBarChangeListener) {
        this.onTickSeekBarChangeListener = onTickSeekBarChangeListener;
    }

    public interface OnTickSeekBarChangeListener {

        void onStartTrackingTouch(TickRangeBar seekBar);

        void onStopTrackingTouch(TickRangeBar seekBar);

        void onTickIndexChanged(TickRangeBar tickRangeBar, int index);
    }

}

package com.sean.android.seekbar.framerangseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.sean.android.seekbar.AbstractSeekBar;
import com.sean.android.seekbar.NumberType;
import com.sean.android.seekbar.R;
import com.sean.android.seekbar.util.BitmapUtil;
import com.sean.android.seekbar.util.PixelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 프레임 편집 및 슬로우 모션 편집 기능이 추가된 Frame이 노출되는 Seekbar
 * 주로 MediaPlayer에서 사용하도록 권장됨
 *
 * @param <T> 범위 설정에 대한 Number Type값 설정(Long, Double, Integer, Float, Short, Byte or BigDecimal)
 *            Created by Seon il on 2016-12-05.
 *            <p>
 *            Reference RangeSeekbar
 *            https://github.com/anothem/android-range-seek-bar
 */
public class FrameRangeSeekBar<T extends Number> extends AbstractSeekBar {

    public static final Integer DEFAULT_MINIMUM = 0;
    public static final Integer DEFAULT_MAXIMUM = 100;
    public static final Integer DEFAULT_STEP = 1;
    public static final Integer DEFAULT_TOP_HEIGHT = 120;
    public static final Integer DEFAULT_BOTTOM_HEIGHT = 30;
    public static final Integer DEFAULT_SLOW_MINIMUM = 30;
    public static final Integer DEFAULT_SLOW_MAXIMUM = 60;
    public static final int DEFAULT_SLOW_MOTION_COLOR = 0xFF000000;
    public static final double DEFAULT_INTERVAL = 0.1d;

    public static final int INVALID_POINTER_ID = 255;

    private static final int INITIAL_PADDING_IN_DP = 50;

    private int scaledTouchSlop;

    private float internalPad; // Seekbar 양사이드 Padding

    // Thumb(Frame 조절) 왼쪽(MIN) 이미지
    private Bitmap thumbLeftImage; // Thumb Left Default 이미지
    private Bitmap thumbLeftPressedImage; // Thumb Left Pressed 이미지
    private Bitmap thumbLeftDisabledImage; // Thumb Left Disabled 이미지

    // Thumb(Frame 조절) 오른쪽(MAX) 이미지
    private Bitmap thumbRightImage; // Thumb Right Default 이미지
    private Bitmap thumbRightPressedImage; // Thumb Right Pressed 이미지
    private Bitmap thumbRightDisabledImage; // Thumb Right Disabled 이미지

    private Bitmap thumbSlowImage;
    private Bitmap thumbSlowPressedImage;

    private float thumbHalfWidth;
    private float thumbHalfHeight;

    private float thumbSlowHalfWidth;
    private float thumbSlowHalfHeight;
    private int slowRangeColor;

    private float padding;

    private List<Bitmap> frames;

    private int screenWidth;
    private int screenHeight;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected T absoluteMinValue, absoluteMaxValue, absoluteStepValue;

    protected double normalizedMinValue = 0d;  // Seekbar의 MIN Value값을 0.0d ~ 1.0d로 표현
    protected double normalizedMaxValue = 1d;  // Seekbar의 MAX Value값을 1.0d ~ 0.0d로 표현
    protected double normalizedValue = 0d;
    protected double minDeltaForDefault = 0;

    protected double slowMinValue = 0d; // SlowMotion의 Min Value값을 0.0d ~ 1.0d로 표현
    protected double slowMaxValue = 1d; // SlowMotion의 Max Value값을 0.0d ~ 1.0d로 표현

    protected double absoluteMinValuePrim, absoluteMaxValuePrim, absoluteStepValuePrim;

    protected NumberType numberType;

    protected boolean isSlowMotionVideo = false;

    //Draw
    private Rect rect;


    //Touch && Drag Motion
    private boolean isPlaying = false;
    private boolean notifyWhileDragging = false;
    private boolean isDragging;
    private Thumb pressedThumb = null;
    private float downMotionX;
    private OnFrameRangeSeekBarChangeListener listener;

    public FrameRangeSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public FrameRangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FrameRangeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    public FrameRangeSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int thumbLeftNormal = R.drawable.btn_control_frame_left;
        int thumbLeftPressed = R.drawable.btn_control_frame_left_pressed;
        int thumbLeftDisabled = R.drawable.btn_control_frame_left;

        int thumbRightNormal = R.drawable.btn_control_frame_right;
        int thumbRightPressed = R.drawable.btn_control_frame_right_pressed;
        int thumbRightDisabled = R.drawable.btn_control_frame_right;

        int slowThumbNormal = R.drawable.ic_location_on_grey;
        int slowThumbPressed = R.drawable.ic_location_on_grey_pressed;


        if (attrs == null) {
            setRangeToDefaultValues();
            internalPad = PixelUtil.dpToPx(context, INITIAL_PADDING_IN_DP);
        } else {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FrameRangeSeekBar, 0, 0);
            try {
                setRangeValues(extractNumericValueFromAttributes(typedArray, R.styleable.FrameRangeSeekBar_absoluteMinValue, DEFAULT_MINIMUM),
                        extractNumericValueFromAttributes(typedArray, R.styleable.FrameRangeSeekBar_absoluteMaxValue, DEFAULT_MAXIMUM),
                        extractNumericValueFromAttributes(typedArray, R.styleable.FrameRangeSeekBar_step, DEFAULT_STEP)
                );

                isSlowMotionVideo = typedArray.getBoolean(R.styleable.FrameRangeSeekBar_slowMotion, false);

                internalPad = typedArray.getDimensionPixelSize(R.styleable.FrameRangeSeekBar_internalPadding, INITIAL_PADDING_IN_DP);

                slowRangeColor = typedArray.getColor(R.styleable.FrameRangeSeekBar_slowMotionRangeColor, DEFAULT_SLOW_MOTION_COLOR);

                Drawable leftNormalDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_thumbLeftNormal);
                if (leftNormalDrawable != null) {
                    thumbLeftImage = BitmapUtil.drawableToBitmap(leftNormalDrawable);
                }
                Drawable leftDisabledDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_thumbLeftDisabled);
                if (leftDisabledDrawable != null) {
                    thumbLeftDisabledImage = BitmapUtil.drawableToBitmap(leftDisabledDrawable);
                }
                Drawable leftPressedDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_thumbLeftPressed);
                if (leftPressedDrawable != null) {
                    thumbLeftPressedImage = BitmapUtil.drawableToBitmap(leftPressedDrawable);
                }

                Drawable rightNormalDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_thumbRightNormal);
                if (rightNormalDrawable != null) {
                    thumbRightImage = BitmapUtil.drawableToBitmap(rightNormalDrawable);
                }
                Drawable rightDisabledDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_thumbRightDisabled);
                if (rightDisabledDrawable != null) {
                    thumbRightDisabledImage = BitmapUtil.drawableToBitmap(rightDisabledDrawable);
                }
                Drawable rightPressedDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_thumbRightPressed);
                if (rightPressedDrawable != null) {
                    thumbRightPressedImage = BitmapUtil.drawableToBitmap(rightPressedDrawable);
                }

                Drawable slowThumbNormalDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_slowThumbNormal);
                if (slowThumbNormalDrawable != null) {
                    thumbSlowImage = BitmapUtil.drawableToBitmap(slowThumbNormalDrawable);
                }

                Drawable slowThumbPressedDrawable = typedArray.getDrawable(R.styleable.FrameRangeSeekBar_slowThumbPressed);
                if (slowThumbPressedDrawable != null) {
                    thumbSlowPressedImage = BitmapUtil.drawableToBitmap(slowThumbPressedDrawable);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                typedArray.recycle();
            }
        }

        if (thumbLeftImage == null) {
            thumbLeftImage = BitmapFactory.decodeResource(getResources(), thumbLeftNormal);
        }
        if (thumbLeftPressedImage == null) {
            thumbLeftPressedImage = BitmapFactory.decodeResource(getResources(), thumbLeftPressed);
        }
        if (thumbLeftDisabledImage == null) {
            thumbLeftDisabledImage = BitmapFactory.decodeResource(getResources(), thumbLeftDisabled);
        }


        if (thumbRightImage == null) {
            thumbRightImage = BitmapFactory.decodeResource(getResources(), thumbRightNormal);
        }
        if (thumbRightPressedImage == null) {
            thumbRightPressedImage = BitmapFactory.decodeResource(getResources(), thumbRightPressed);
        }
        if (thumbRightDisabledImage == null) {
            thumbRightDisabledImage = BitmapFactory.decodeResource(getResources(), thumbRightDisabled);
        }

        if (thumbSlowImage == null) {
            thumbSlowImage = BitmapFactory.decodeResource(getResources(), slowThumbNormal);
        }

        if (thumbSlowPressedImage == null) {
            thumbSlowPressedImage = BitmapFactory.decodeResource(getResources(), slowThumbPressed);
        }


        thumbHalfWidth = 0.5f * thumbLeftImage.getWidth();
        thumbHalfHeight = 0.5f * thumbLeftImage.getHeight();

        thumbSlowHalfWidth = 0.5f * thumbSlowImage.getWidth();
        thumbSlowHalfHeight = 0.5f * thumbSlowImage.getHeight();

        setValuePrimAndNumberType();
        setFocusable(true);
        setFocusableInTouchMode(true);

        scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    /**
     * XML inflate시 데이터 세팅이 없는 경우에 Default값으로 설정
     */
    @SuppressWarnings("unchecked")
    private void setRangeToDefaultValues() {
        this.absoluteMinValue = (T) DEFAULT_MINIMUM;
        this.absoluteMaxValue = (T) DEFAULT_MAXIMUM;
        this.absoluteStepValue = (T) DEFAULT_STEP;
        setValuePrimAndNumberType();
    }

    private void setValuePrimAndNumberType() {
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        absoluteStepValuePrim = absoluteStepValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);
    }

    public void setRangeValues(T minValue, T maxValue, T step) {
        this.absoluteStepValue = step;
        setRangeValues(minValue, maxValue);
    }

    public void setRangeValues(T minValue, T maxValue) {
        this.absoluteMinValue = minValue;
        this.absoluteMaxValue = maxValue;
        setValuePrimAndNumberType();
    }

    public void setSlowRangeValues(T minValue, T maxValue) {
        if (isSlowMotionVideo) {
            setNormalizedSlowMinValue(valueToNormalized(minValue));
            setNormalizedSlowMaxValue(valueToNormalized(maxValue));
        } else {
            setNormalizedSlowMinValue(valueToNormalized(absoluteMinValue));
            setNormalizedSlowMaxValue(valueToNormalized(absoluteMaxValue));
        }
    }

    @SuppressWarnings("unused")
    public void resetSelectedValues() {
        setSelectedMinValue(absoluteMinValue);
        setSelectedMaxValue(absoluteMaxValue);
    }

    public void setSelectedMinValue(T value) {
        // absoluteMinValue == absoluteMaxValue일 경우 0으로 나뉘는 것을 피하기 위해 Minimum값을 0으로 지정
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    public void setSelectedMaxValue(T value) {
        // absoluteMinValue == absoluteMaxValue일 경우 0으로 나뉘는 것을 피하기 위해 Maximum값을 1로 지정
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    protected double valueToNormalized(T value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            // 0으로 나누는 것을 피하기 위해 0을 return
            return 0d;
        }
        return (value.doubleValue() - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    private void setNormalizedMinValue(double value) {

        if (isSlowMotionVideo) {
            normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, slowMaxValue - DEFAULT_INTERVAL < normalizedMaxValue ? slowMaxValue - DEFAULT_INTERVAL : normalizedMaxValue)));
        } else {
            normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
        }
        invalidate();
    }

    private void setNormalizedValue(double value) {
        normalizedValue = Math.max(0, Math.min(1d, Math.min(value, normalizedMaxValue)));
        invalidate();
    }

    private void setNormalizedMaxValue(double value) {
        if (isSlowMotionVideo) {
            normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, slowMinValue + DEFAULT_INTERVAL > normalizedMinValue ? slowMinValue + DEFAULT_INTERVAL : normalizedMinValue)));
        } else {
            normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
        }
        invalidate();
    }

    private void setNormalizedSlowMinValue(double value) {
        slowMinValue = Math.max(normalizedMinValue, Math.min(normalizedMaxValue, Math.min(value, slowMaxValue - DEFAULT_INTERVAL)));
        invalidate();
    }

    private void setNormalizedSlowMaxValue(double value) {
        slowMaxValue = Math.max(normalizedMinValue, Math.min(normalizedMaxValue, Math.max(value, slowMinValue + DEFAULT_INTERVAL)));
        invalidate();
    }

    @SuppressWarnings("unchecked")
    private T extractNumericValueFromAttributes(TypedArray a, int attribute, int defaultValue) {
        TypedValue tv = a.peekValue(attribute);
        if (tv == null) {
            return (T) Integer.valueOf(defaultValue);
        }

        int type = tv.type;
        if (type == TypedValue.TYPE_FLOAT) {
            return (T) Float.valueOf(a.getFloat(attribute, defaultValue));
        } else {
            return (T) Integer.valueOf(a.getInteger(attribute, defaultValue));
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200; // 별도 지정 없는 Width값일 경우 임의의 값을 지정
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = View.MeasureSpec.getSize(widthMeasureSpec);
        }

        int height = thumbLeftImage.getHeight() + DEFAULT_TOP_HEIGHT + DEFAULT_BOTTOM_HEIGHT;

        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec));
        }

        screenWidth = width;
        screenHeight = height;
        setMeasuredDimension(width, height);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);

        padding = internalPad + thumbHalfWidth * 2;

        boolean selectedValuesAreDefault = (normalizedMinValue <= minDeltaForDefault && normalizedMaxValue >= 1 - minDeltaForDefault);

        if (isPlaying) {
            drawBackground(canvas);
        }

        if (frames != null && !frames.isEmpty()) {
            int frameScreen = (int) ((screenWidth - (padding * 2)) / frames.size());
            for (int index = 0; index < frames.size(); index++) {
                Bitmap bitmap = frames.get(index);

                if (rect == null) {
                    rect = new Rect((int) (index * frameScreen + padding), DEFAULT_TOP_HEIGHT, (int) ((index + 1) * frameScreen + padding), DEFAULT_TOP_HEIGHT + thumbLeftImage.getHeight());
                } else {
                    rect.set((int) (index * frameScreen + padding), DEFAULT_TOP_HEIGHT, (int) ((index + 1) * frameScreen + padding), DEFAULT_TOP_HEIGHT + thumbLeftImage.getHeight());
                }
                canvas.drawBitmap(bitmap, null, rect, null);
            }

            if (!isPlaying) {
                //Draw Skip Frame Range Box
                drawOpacity(padding, normalizedLeftThumbToScreen(normalizedMinValue) - thumbHalfWidth / 2, canvas);

                //Draw Min Thumb
                drawLeftThumb(normalizedLeftThumbToScreen(normalizedMinValue), Thumb.MIN.equals(pressedThumb), canvas, selectedValuesAreDefault);


                //Draw Skip Frame Range Box
                drawOpacity(normalizedRightThumbToScreen(normalizedMaxValue) + thumbHalfWidth / 2, screenWidth - padding, canvas);

                //Draw Max Thumb
                drawRightThumb(normalizedRightThumbToScreen(normalizedMaxValue), Thumb.MAX.equals(pressedThumb), canvas, selectedValuesAreDefault);


                if (isSlowMotionVideo) {
                    //Draw Start ~ End Slow Thumb
                    drawSlowThumb(normalizedToScreen(slowMinValue), Thumb.SLOW_MIN.equals(pressedThumb), canvas);
                    drawSlowThumb(normalizedToScreen(slowMaxValue), Thumb.SLOW_MAX.equals(pressedThumb), canvas);
                }


            } else {
                if (isSlowMotionVideo) {
                    //Draw Box
                    drawSlowMotionSection(normalizedToScreen(slowMinValue), normalizedToScreen(slowMaxValue), canvas);
                }

                //Draw Path Thumb
                drawPlayingPath(normalizedToScreen(normalizedValue), canvas);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
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
                float downMotionY = event.getY(pointerIndex);
                if (!isPlaying) {
                    pressedThumb = evalPressedThumb(downMotionX, downMotionY);

                    // Thumb이 눌려있을때만 동작하도록 변경
                    if (pressedThumb == null) {
                        return super.onTouchEvent(event);
                    }

                    setPressed(true);
                    invalidate();
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();
                } else {
                    pressedThumb = evalPressedThumb(downMotionX, downMotionY);

                    if (pressedThumb != null && pressedThumb.equals(Thumb.PATH)) {
                        setPressed(true);
                        invalidate();
                        onStartTrackingTouch();
                        trackTouchEvent(event);
                        attemptClaimDrag();
                    } else {
                        pressedThumb = null;
                        return super.onTouchEvent(event);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {

                    if (isDragging) {
                        trackTouchEvent(event);

                        if (listener != null) {
                            listener.onFrameRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                        }
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(activePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - downMotionX) > scaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }


                if (listener != null) {

                    if (pressedThumb.equals(Thumb.PATH)) {
                        listener.onFrameRangeSeekChanged(this, getSelectedPathValue());
                    } else {
                        listener.onFrameRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                        listener.onFrameRangeSeekBarSlowValuesChanged(this, getSelectedSlowMinValue(), getSelectedSlowMaxValue());
                    }
                }

                pressedThumb = null;
                invalidate();

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    /**
     * 왼쪽(최소) Thumb Image를 일반/눌림상태의 이미지로 특정 x좌표에 그려주도록 도와주는 Method
     *
     * @param screenCoord 이미지를 그릴 특정 x 좌표 위치
     * @param pressed     해당 Thumb이 눌려있는 상태인지 상태 값
     * @param canvas      해당 Thumb 이미지를 그릴 canvas
     */
    private void drawLeftThumb(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
        Bitmap buttonToDraw;
        if (areSelectedValuesDefault) {
            buttonToDraw = thumbLeftDisabledImage;
        } else {
            buttonToDraw = pressed ? thumbLeftPressedImage : thumbLeftImage;
        }

        canvas.drawBitmap(buttonToDraw, screenCoord - thumbHalfWidth,
                DEFAULT_TOP_HEIGHT,
                paint);
    }

    /**
     * 오른쪽(최대) Thumb Image를 일반/눌림상태의 이미지로 특정 x좌표에 그려주도록 도와주는 Method
     *
     * @param screenCoord 이미지를 그릴 특정 x 좌표 위치
     * @param pressed     해당 Thumb이 눌려있는 상태인지 상태 값
     * @param canvas      해당 Thumb 이미지를 그릴 canvas
     */
    private void drawRightThumb(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
        Bitmap buttonToDraw;
        if (areSelectedValuesDefault) {
            buttonToDraw = thumbRightDisabledImage;
        } else {
            buttonToDraw = pressed ? thumbRightPressedImage : thumbRightImage;
        }

        canvas.drawBitmap(buttonToDraw, screenCoord - thumbHalfWidth,
                DEFAULT_TOP_HEIGHT,
                paint);
    }

    /**
     * Slow Thumb을 일반/눌림상태의 이미지로 특정 x좌표에 그려주도록 도와주는 Method
     *
     * @param screenCoord 이미지를 그릴 특정 x 좌표 위치
     * @param pressed     해당 Thumb이 눌려있는 상태인지 상태 값
     * @param canvas      해당 Thumb 이미지를 그릴 canvas
     */
    private void drawSlowThumb(float screenCoord, boolean pressed, Canvas canvas) {
        Bitmap buttonToDraw;

        buttonToDraw = pressed ? thumbSlowPressedImage : thumbSlowImage;

        canvas.drawBitmap(buttonToDraw, screenCoord - thumbSlowHalfWidth,
                DEFAULT_TOP_HEIGHT - (thumbSlowHalfHeight * 2),
                paint);
    }

    /**
     * Frame 이미지의 뒤에 뒷배경을 그리는 Method
     *
     * @param canvas Background를 그릴 canvas
     */
    private void drawBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.rgb(155, 155, 155));
        paint.setStyle(Paint.Style.FILL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(normalizedToScreen(0.0d) - 20, DEFAULT_TOP_HEIGHT, normalizedToScreen(1.0d) + 20, thumbLeftImage.getHeight() + DEFAULT_TOP_HEIGHT, 8.0f, 8.0f, paint);
        } else {
            RectF rectF = new RectF();
            rectF.set(normalizedToScreen(0.0d) - 20, DEFAULT_TOP_HEIGHT, normalizedToScreen(1.0d) + 20, thumbLeftImage.getHeight() + DEFAULT_TOP_HEIGHT);
            canvas.drawRoundRect(rectF, 8.0f, 8.0f, paint);
        }
    }

    /**
     * SlowMotion의 범위부분을 그리는 Method
     *
     * @param canvas Background를 그릴 canvas
     */
    private void drawSlowMotionSection(float startScreenCoord, float endScreenCoord, Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(slowRangeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(8.0f);

        Rect rect = new Rect((int) startScreenCoord, DEFAULT_TOP_HEIGHT, (int) endScreenCoord, thumbLeftImage.getHeight() + DEFAULT_TOP_HEIGHT);
        canvas.drawRect(rect, paint);
    }

    /**
     * Frame 이미지의 뒤에 뒷배경을 그리는걸 도와주는 Method
     *
     * @param canvas Background를 그릴 canvas
     */
    private void drawOpacity(float startScreenCoord, float endScreenCoord, Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.rgb(99, 99, 99));
        paint.setAlpha(150);
        paint.setStyle(Paint.Style.FILL);
        Rect rect = new Rect((int) startScreenCoord, DEFAULT_TOP_HEIGHT, (int) endScreenCoord, thumbLeftImage.getHeight() + DEFAULT_TOP_HEIGHT);

        canvas.drawRect(rect, paint);
    }


    /**
     * Play Path 이미지를 특정 x좌표에 그려주도록 도와주는 Method
     *
     * @param screenCoord 이미지를 그릴 특정 x 좌표 위치
     * @param canvas      해당 PlayPath 이미지를 그릴 canvas
     */
    private void drawPlayingPath(float screenCoord, Canvas canvas) {

        int barSize = 10;
        int yPostion = 20;
        int radius = 20;

        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 116, 110));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        RectF rectF = new RectF();
        rectF.set(screenCoord, yPostion, screenCoord + barSize, thumbLeftImage.getHeight() + DEFAULT_TOP_HEIGHT);


        canvas.drawCircle(screenCoord + (barSize / 2), yPostion, radius, paint);
        canvas.drawRoundRect(rectF, 2f, 2f, paint);
    }

    private Thumb evalPressedThumb(float touchX, float touchY) {
        Thumb result = null;
        boolean minThumbPressed = isInLeftThumbRange(touchX, touchY, normalizedMinValue);
        boolean maxThumbPressed = isInRightThumbRange(touchX, touchY, normalizedMaxValue);
        boolean pathThumbPressed = isInThumbRange(touchX, normalizedValue);
        boolean minSlowThumbPressed = isInSlowThumbRange(touchX, touchY, slowMinValue);
        boolean maxSlowThumbPressed = isInSlowThumbRange(touchX, touchY, slowMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        } else if (pathThumbPressed && isPlaying) {
            result = Thumb.PATH;
        } else if (!isPlaying && isSlowMotionVideo && minSlowThumbPressed && maxSlowThumbPressed) {
            float minTouchRange = getThumbTouchRange(touchX, slowMinValue);
            float maxTouchRange = getThumbTouchRange(touchX, slowMaxValue);
            result = Float.compare(minTouchRange, maxTouchRange) == 1 ? Thumb.SLOW_MIN : Thumb.SLOW_MAX;
        } else if (!isPlaying && isSlowMotionVideo && minSlowThumbPressed) {
            result = Thumb.SLOW_MIN;
        } else if (!isPlaying && isSlowMotionVideo && maxSlowThumbPressed) {
            result = Thumb.SLOW_MAX;
        }
        return result;
    }

    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
    }

    private boolean isInLeftThumbRange(float touchX, float touchY, double normalizedThumbValue) {
        boolean isTouchX = Math.abs(touchX - normalizedLeftThumbToScreen(normalizedThumbValue)) <= thumbHalfWidth;
        boolean isTouchY = touchY >= DEFAULT_TOP_HEIGHT;
        return isTouchX && isTouchY;
    }

    private boolean isInRightThumbRange(float touchX, float touchY, double normalizedThumbValue) {
        boolean isTouchX = Math.abs(touchX - normalizedRightThumbToScreen(normalizedThumbValue)) <= thumbHalfWidth;
        boolean isTouchY = touchY >= DEFAULT_TOP_HEIGHT;
        return isTouchX && isTouchY;
    }

    private boolean isInSlowThumbRange(float touchX, float touchY, double normalizedThumbValue) {
        boolean isTouchX = Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbSlowHalfWidth * 2;
        boolean isTouchY = touchY <= (thumbSlowHalfHeight * 2);

        return isTouchX && isTouchY;
    }

    private float getThumbTouchRange(float touchX, double normalizedValueThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedValueThumbValue));
    }

    /**
     * Double(0.0d ~ 1.0d) 형태의 현재 x위치를 Screen의 Width값의 비율만큼으로 x값을 전환
     *
     * @param normalizedCoord 선택한 x 포지션값이 0.0d ~ 1.0d의 값으로 컨버팅된 데이터
     * @return 선택한 x 포지션값을 float 형태로 리턴
     */
    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    private float normalizedLeftThumbToScreen(double normalizedCoord) {
        return (float) (padding - thumbHalfWidth + normalizedCoord * (getWidth() - 2 * padding));
    }

    private float normalizedRightThumbToScreen(double normalizedCoord) {
        return (float) (padding + thumbHalfWidth + normalizedCoord * (getWidth() - 2 * padding));
    }

    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(activePointerId);
        try {
            final float x = event.getX(pointerIndex);

            if (Thumb.MIN.equals(pressedThumb)) {
                setNormalizedMinValue(screenToNormalized(x));
            } else if (Thumb.MAX.equals(pressedThumb)) {
                setNormalizedMaxValue(screenToNormalized(x));
            } else if (Thumb.PATH.equals(pressedThumb)) {
                setNormalizedValue(screenToNormalized(x));
            } else if (isSlowMotionVideo && Thumb.SLOW_MIN.equals(pressedThumb)) {
                setNormalizedSlowMinValue(screenToNormalized(x));
            } else if (isSlowMotionVideo && Thumb.SLOW_MAX.equals(pressedThumb)) {
                setNormalizedSlowMaxValue(screenToNormalized(x));
            }

            if (isSlowMotionVideo) {
                if (normalizedMinValue > slowMinValue) {

                    setNormalizedSlowMinValue(screenToNormalized(x));
                }

                if (normalizedMaxValue < slowMaxValue) {

                    setNormalizedSlowMaxValue(screenToNormalized(x));
                }
            }


            if (!isPlaying) {
                setNormalizedValue(normalizedMinValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    @SuppressWarnings("unchecked")
    private T roundOffValueToStep(T value) {
        double d = Math.round(value.doubleValue() / absoluteStepValuePrim) * absoluteStepValuePrim;
        return (T) numberType.toNumber(Math.max(absoluteMinValuePrim, Math.min(absoluteMaxValuePrim, d)));
    }

    /**
     * onTouchEvent에서 ActionDown이 실행될때 Dragging이 시작 된것으로 간주
     */
    void onStartTrackingTouch() {
        isDragging = true;
        if (listener != null) {
            listener.onStartTrackingTouch(this);
        }
    }

    /**
     * onTouchEvent에서 ActionUp이 실행될때 Dragging이 끝난 것으로 간주
     */
    void onStopTrackingTouch() {
        isDragging = false;
        if (listener != null) {
            listener.onStopTrackingTouch(this);
        }
    }

    @SuppressWarnings("unchecked")
    protected T normalizedToValue(double normalized) {
        double v = absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim);
        // Decimalpoints가 적용되기 위해서는 Math.round함수를 적용하도록 하여야함.
        return (T) numberType.toNumber(Math.round(v * 100) / 100d);
    }

    /**
     * 현재 적용된 Frame 조정 최대값을 return
     *
     * @return Frame 조정 최대값 return
     */
    public T getSelectedMaxValue() {
        return roundOffValueToStep(normalizedToValue(normalizedMaxValue));
    }

    public T getSelectedMinValue() {
        return roundOffValueToStep(normalizedToValue(normalizedMinValue));
    }

    public T getSelectedSlowMaxValue() {
        return roundOffValueToStep(normalizedToValue(slowMaxValue));
    }

    public T getSelectedSlowMinValue() {
        return roundOffValueToStep(normalizedToValue(slowMinValue));
    }

    public T getSelectedPathValue() {
        return roundOffValueToStep(normalizedToValue(normalizedValue));
    }

    public void setFrames(List<Bitmap> list) {
        calculateBitmap(list);
        invalidate();
    }

    private void calculateBitmap(List<Bitmap> list) {
        if (frames == null) {
            frames = new ArrayList<>();
        } else {
            frames.clear();
        }

        if (list != null && !list.isEmpty()) {
            int frameScreen = (screenWidth - (thumbLeftImage.getWidth() * 2)) / list.size();
            for (int index = 0; index < list.size(); index++) {
                Bitmap bitmap = list.get(index);
                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, frameScreen, frameScreen, false);

                if (frames != null) {
                    frames.add(resizeBitmap);
                }
                bitmap.recycle();
            }
        }
    }

    public void release() {
        if (frames != null) {
            for (Bitmap bitmap : frames) {
                bitmap.recycle();
            }
            frames.clear();
            frames = null;
        }

        if (thumbLeftImage != null) {
            thumbLeftImage.recycle();
            thumbLeftImage = null;
        }

        if (thumbLeftDisabledImage != null) {
            thumbLeftDisabledImage.recycle();
            thumbLeftDisabledImage = null;
        }

        if (thumbLeftPressedImage != null) {
            thumbLeftPressedImage.recycle();
            thumbLeftPressedImage = null;
        }

        if (thumbRightImage != null) {
            thumbRightImage.recycle();
            thumbRightImage = null;
        }

        if (thumbRightDisabledImage != null) {
            thumbRightDisabledImage.recycle();
            thumbRightDisabledImage = null;
        }

        if (thumbRightPressedImage != null) {
            thumbRightPressedImage.recycle();
            thumbRightPressedImage = null;
        }

        if (thumbSlowImage != null) {
            thumbSlowImage.recycle();
            thumbSlowImage = null;
        }

        if (thumbSlowPressedImage != null) {
            thumbSlowPressedImage.recycle();
            thumbSlowPressedImage = null;
        }
    }


    @SuppressWarnings("unused")
    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    @SuppressWarnings("unused")
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    @SuppressWarnings("unused")
    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;

        if (!playing) {
            setNormalizedValue(normalizedMinValue);
        }

        invalidate();
    }

    public void setSlowMotionVideo(boolean slowMotionVideo) {
        isSlowMotionVideo = slowMotionVideo;
    }

    @SuppressWarnings("unused")
    public boolean isSlowMotionVideo() {
        return isSlowMotionVideo;
    }

    public void setOnFrameRangeSeekBarChangeListener(OnFrameRangeSeekBarChangeListener listener) {
        this.listener = listener;
    }


    public void setProgress(T progress) {
        if (isPlaying) {
            setNormalizedValue(valueToNormalized(progress));
            invalidate();
        }
    }

    private enum Thumb {
        MIN, MAX, PATH, SLOW_MIN, SLOW_MAX
    }


    public interface OnFrameRangeSeekBarChangeListener<T extends Number> {

        void onStartTrackingTouch(FrameRangeSeekBar seekBar);

        void onStopTrackingTouch(FrameRangeSeekBar seekBar);

        void onFrameRangeSeekBarValuesChanged(FrameRangeSeekBar<T> bar, T minValue, T maxValue);

        void onFrameRangeSeekBarSlowValuesChanged(FrameRangeSeekBar<T> bar, T minValue, T maxValue);

        void onFrameRangeSeekChanged(FrameRangeSeekBar<T> bar, T value);
    }
}

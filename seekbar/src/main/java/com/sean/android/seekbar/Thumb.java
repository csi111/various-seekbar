package com.sean.android.seekbar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;

/**
 * Created by Seonil on 2016-12-13.
 */

public class Thumb extends Figure {
    private static final int DEFAULT_THUMB_COLOR_NORMAL = Color.BLUE;
    private static final int DEFAULT_THUMB_COLOR_PRESSED = Color.BLUE;
    private static final int DEFAULT_THUMB_COLOR_DISABLED = Color.LTGRAY;
    private static final float DEFAULT_THUMB_RADIUS_DP = 14;
    private static final float DEFAULT_TOUCH_AREA_THUMB_RADIUS_DP = 24;


    private Bitmap thumbImageNormal;
    private Bitmap thumbImagePressed;
    private Bitmap thumbImageDisabled; //TODO it will be developed soon...

    private int thumbColorNormal;
    private int thumbColorPressed;
    private int thumbColorDisabled; //TODO it will be developed soon...

    private float touchAreaRadiusPx;
    private float thumbRadiusPx;

    private float thumbHalfWidthNormal;
    private float thumbHalfWidthPressed;
    private float thumbHalfWidthDisabled; //TODO it will be developed soon...

    private float thumbHalfHeightNormal;
    private float thumbHalfHeightPressed;
    private float thumbHalfHeightDisabled; //TODO it will be developed soon...

    private ThumbStatus thumbStatus;

    private boolean useBitmap;

    private float yPosition;
    private float xPosition;

    private Paint paintThumb;
    private Paint paintThumbPressed;
    private Paint paintThumbDisabled; //TODO it will be developed soon...

    public Thumb(Resources resources, float yPosition, Bitmap thumbImageNoraml, Bitmap thumbImagePressed) {
        this(resources, yPosition, -1, thumbImageNoraml, thumbImagePressed, -1, -1);
    }

    public Thumb(Resources resources, float yPosition, float thumbRadiusDP, Bitmap thumbImageNormal, Bitmap thumbImagePressed, int thumbColorNormal, int thumbColorPressed) {
        thumbStatus = ThumbStatus.NORMAL;
        this.thumbImageNormal = thumbImageNormal;
        this.thumbImagePressed = thumbImagePressed;

        if (thumbRadiusDP == -1 && thumbColorNormal == -1 && thumbColorPressed == -1) {
            useBitmap = true;


        } else {
            useBitmap = false;


            if (thumbRadiusDP == -1) {
                thumbRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_THUMB_RADIUS_DP, resources.getDisplayMetrics());
            } else {
                thumbRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, thumbRadiusDP, resources.getDisplayMetrics());
            }

            if (thumbColorNormal == -1) {
                this.thumbColorNormal = DEFAULT_THUMB_COLOR_NORMAL;
            } else {
                this.thumbColorNormal = thumbColorNormal;
            }

            if (thumbColorPressed == -1) {
                this.thumbColorPressed = DEFAULT_THUMB_COLOR_PRESSED;
            }
            thumbColorDisabled = DEFAULT_THUMB_COLOR_DISABLED; //TODO it will be developed soon...
            initPaint();
        }

        initSize();
        int touchArea = (int) Math.max(DEFAULT_TOUCH_AREA_THUMB_RADIUS_DP, thumbRadiusDP);
        touchAreaRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, touchArea, resources.getDisplayMetrics());

        this.xPosition = thumbHalfWidthNormal;
        this.yPosition = yPosition;
    }

    private void initSize() {
        if (thumbImageNormal != null) {
            thumbHalfWidthNormal = getHalfSize(thumbImageNormal.getWidth());
            thumbHalfHeightNormal = getHalfSize(thumbImageNormal.getHeight());
        }

        if (thumbImagePressed != null) {
            thumbHalfWidthPressed = getHalfSize(thumbImagePressed.getWidth());
            thumbHalfHeightPressed = getHalfSize(thumbImagePressed.getHeight());
        }

        if (thumbImageDisabled != null) {
            thumbHalfWidthDisabled = getHalfSize(thumbImageDisabled.getWidth());
            thumbHalfHeightDisabled = getHalfSize(thumbImageDisabled.getHeight());
        }
    }

    private float getHalfSize(float size) {
        return size / 2f;
    }


    private void initPaint() {
        paintThumb = new Paint();
        paintThumb.setColor(thumbColorNormal);
        paintThumb.setAntiAlias(true);

        paintThumbPressed = new Paint();
        paintThumbPressed.setColor(thumbColorPressed);
        paintThumbPressed.setAntiAlias(true);

        paintThumbDisabled = new Paint();
        paintThumbDisabled.setColor(thumbColorDisabled);
        paintThumbDisabled.setAntiAlias(true);
    }

    public ThumbStatus getThumbStatus() {
        return thumbStatus;
    }

    public boolean isPressed() {
        return ThumbStatus.PRESSED.equals(thumbStatus);
    }

    public float getThumbHalfWidthNormal() {
        return thumbHalfWidthNormal;
    }

    public float getThumbHalfWidthPressed() {
        return thumbHalfWidthPressed;
    }

    public float getThumbHalfWidthDisabled() {
        return thumbHalfWidthDisabled;
    }

    public float getThumbHalfHeightNormal() {
        return thumbHalfHeightNormal;
    }

    public float getThumbHalfHeightPressed() {
        return thumbHalfHeightPressed;
    }

    public float getThumbHalfHeightDisabled() {
        return thumbHalfHeightDisabled;
    }

    public float getXPosition() {
        return xPosition;
    }

    public void setxPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public void pressThumb() {
        thumbStatus = ThumbStatus.PRESSED;
    }

    public void releaseThumb() {
        thumbStatus = ThumbStatus.NORMAL;
    }

    public boolean isInThumbRange(float touchX, float touchY) {
        boolean isThumbRange = false;


        if (useBitmap) {
            isThumbRange = (Math.abs(touchX - xPosition) <= thumbHalfWidthNormal) && (Math.abs(touchY - yPosition) <= thumbHalfHeightNormal);
        } else {
            isThumbRange = (Math.abs(touchX - xPosition) <= touchAreaRadiusPx) && (Math.abs(touchY - yPosition) <= touchAreaRadiusPx);
        }

        return isThumbRange;
    }

    @Override
    public void draw(Canvas canvas) {

        if (useBitmap) {
            float topPosition;
            float leftPosition;
            if (ThumbStatus.PRESSED.equals(thumbStatus)) {
                topPosition = yPosition - thumbHalfHeightPressed;
                leftPosition = xPosition - thumbHalfWidthPressed;
            } else {
                topPosition = yPosition - thumbHalfHeightNormal;
                leftPosition = xPosition - thumbHalfWidthNormal;
            }
            canvas.drawBitmap(ThumbStatus.PRESSED.equals(thumbStatus) ? thumbImagePressed : thumbImageNormal, leftPosition, topPosition, null);
        } else {
            if (ThumbStatus.PRESSED.equals(thumbStatus)) {
                canvas.drawCircle(xPosition, yPosition, thumbRadiusPx, paintThumbPressed);
            } else {
                canvas.drawCircle(xPosition, yPosition, thumbRadiusPx, paintThumb);
            }
        }
    }
}

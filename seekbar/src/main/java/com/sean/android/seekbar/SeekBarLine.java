package com.sean.android.seekbar;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Seonil on 2016-12-13.
 */

public class SeekBarLine extends Figure {

    protected Paint paint;
    protected final float yPosition;
    protected final float leftXPosition;
    protected final float rightXPosition;
    protected final float width;
    protected final float lineWeight;
    protected final int lineColor;




    public SeekBarLine(Resources resources, float x, float y, float width, float lineWeight, int lineColor) {
        this.yPosition = y;
        this.leftXPosition = x;
        this.width = width;
        this.rightXPosition = x + width;
        this.lineWeight = lineWeight;
        this.lineColor = lineColor;
        init();
    }

    private void init() {
        if (paint == null) {
            paint = new Paint();
        }

        paint.setColor(lineColor);
        paint.setStrokeWidth(lineWeight);
        paint.setAntiAlias(true);
    }

    public float getLeftXPosition() {
        return leftXPosition;
    }


    public float getRightXPosition() {
        return rightXPosition;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawLine(leftXPosition, yPosition, rightXPosition, yPosition, paint);
    }
}

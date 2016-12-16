package com.sean.android.seekbar;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;

/**
 * Created by Seonil on 2016-12-15.
 */

public class ProgressLine extends Figure {

    protected Paint paint;
    protected float yPosition;
    protected float leftXPosition;
    protected float rightXPosition;

    public ProgressLine(Resources resources, float yPosition, float stroke, int progressColor) {
        this(resources, yPosition, -1, -1, stroke, progressColor);
    }

    public ProgressLine(Resources resources, float yPosition, float leftXPosition, float rightXPosition, float stroke, int progressColor) {
        this.yPosition = yPosition;
        this.leftXPosition = leftXPosition;
        this.rightXPosition = rightXPosition;

        float strokeWidth = stroke;


        paint = new Paint();
        paint.setColor(progressColor);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        draw(canvas, leftXPosition, rightXPosition);
    }

    public void draw(Canvas canvas, float leftX, float rightX) {
        leftXPosition = leftX;
        rightXPosition = rightX;

        canvas.drawLine(leftXPosition, yPosition, rightXPosition, yPosition, paint);
    }
}

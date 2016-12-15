package com.sean.android.seekbar.tickseekbar;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.TypedValue;

import com.sean.android.seekbar.SeekBarLine;
import com.sean.android.seekbar.Thumb;

/**
 * Created by Seonil on 2016-12-14.
 */

public class TickSeekBarLine extends SeekBarLine {

    private final int tickCount;
    private final int tickChildCount;
    private float tickDistance;
    private float tickChildDistance;
    private final float tickHeight;
    private final float tickChildHeight;

    private float tickStartY;
    private float tickEndY;
    private float tickChildStartY;
    private float tickChildEndY;
    private int tickDrawCount;
    private int tickChildDrawCount;


    public TickSeekBarLine(Resources resources, float x, float y, float width, int tickCount, int tickChildCount, float tickHeight, float tickChildHeight, float lineWeight, int lineColor) {
        super(resources, x, y, width, lineWeight, lineColor);
        this.tickHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tickHeight, resources.getDisplayMetrics());
        this.tickChildHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tickChildHeight, resources.getDisplayMetrics());
        this.tickCount = tickCount;
        this.tickChildCount = tickChildCount;
        initializeTick();
    }


    private void initializeTick() {
        tickDrawCount = tickCount - 1;
        tickChildDrawCount = tickChildCount;

        tickDistance = width / tickDrawCount;
        tickChildDistance = tickDistance / tickChildDrawCount;


        tickStartY = yPosition - tickHeight / 2f;
        tickEndY = yPosition + tickHeight / 2f;

        tickChildStartY = yPosition - tickChildHeight / 2f;
        tickChildEndY = yPosition + tickChildHeight / 2f;
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawTicks(canvas);
    }


    private void drawTicks(Canvas canvas) {
        // Loop through and draw each tick (except final tick).
        for (int i = 0; i < tickDrawCount; i++) {
            final float x = i * tickDistance + leftXPosition;
            canvas.drawLine(x, tickStartY, x, tickEndY, paint);

            for (int j = 1; j < tickChildDrawCount; j++) {
                float childX = j * tickChildDistance + x;
                canvas.drawLine(childX, tickChildStartY, childX, tickChildEndY, paint);
            }

        }
        // Draw final tick. We draw the final tick outside the loop to avoid any
        // rounding discrepancies.
        canvas.drawLine(rightXPosition, tickStartY, rightXPosition, tickEndY, paint);
    }

    public float getNearestTickCoodinate(Thumb thumb) {
        return getNearestTickCoodinate(thumb.getXPosition());
    }

    public float getNearestTickCoodinate(float posX) {
        // Total Tick Count is (tickDrawCount * tickChildDrawCount)
        int index = getNearestTickindex(posX);

        float coordinateValue = leftXPosition + (index * tickChildDistance);

        return coordinateValue;
    }


    public int getNearestTickindex(Thumb thumb) {
        return getNearestTickindex(thumb.getXPosition());
    }

    public int getNearestTickindex(float posX) {
        // Total Tick Count is (tickDrawCount * tickChildDrawCount)
        int index = (int) ((posX - leftXPosition + tickChildDistance / 2f) / tickChildDistance);

        return index;
    }


}

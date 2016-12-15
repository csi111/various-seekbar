package com.sean.android.seekbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Seonil on 2016-12-14.
 */

public class AbstractSeekBar extends View {
    public static final int INVALID_POINTER_ID = 255;


    protected int activePointerId = INVALID_POINTER_ID;
    protected float downMotionX, downMotionY;
    protected float internalPad;

    protected OnSeekBarChangeListener seekBarChangeListener;

    public AbstractSeekBar(Context context) {
        super(context);
    }

    public AbstractSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AbstractSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    protected void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    protected void attemptReleaseDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
    }
}

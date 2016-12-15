package com.sean.android.seekbar;

/**
 * Created by Seonil on 2016-12-15.
 */

public interface OnSeekBarChangeListener<E extends AbstractSeekBar, T extends Number> {
    void onStartTrackingTouch(E seekBar);

    void onStopTrackingTouch(E seekBar);

    void onSeekChanged(E seekBar, T value);

}

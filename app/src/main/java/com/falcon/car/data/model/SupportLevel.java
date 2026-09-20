package com.falcon.car.data.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.falcon.car.R;

/**
 * How well a given adapter covers a capability. Kept explicit so no screen can
 * imply that every dongle speaks every protocol.
 */
public enum SupportLevel {

    SUPPORTED(R.string.support_supported, R.color.state_good, R.drawable.bg_pill_good),
    LIMITED(R.string.support_limited, R.color.state_warning, R.drawable.bg_pill_warning),
    UNSUPPORTED(R.string.support_unsupported, R.color.text_tertiary, R.drawable.bg_pill_neutral);

    @StringRes
    private final int labelRes;
    @ColorRes
    private final int colorRes;
    @DrawableRes
    private final int pillRes;

    SupportLevel(@StringRes int labelRes, @ColorRes int colorRes, @DrawableRes int pillRes) {
        this.labelRes = labelRes;
        this.colorRes = colorRes;
        this.pillRes = pillRes;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    @ColorRes
    public int getColorRes() {
        return colorRes;
    }

    @DrawableRes
    public int getPillRes() {
        return pillRes;
    }
}

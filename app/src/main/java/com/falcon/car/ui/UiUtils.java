package com.falcon.car.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

/** Small view helpers shared by the screens. */
public final class UiUtils {

    private UiUtils() {
    }

    public static void setIcon(ImageView view, @DrawableRes int iconRes, @ColorRes int colorRes) {
        view.setImageResource(iconRes);
        tint(view, colorRes);
    }

    public static void tint(ImageView view, @ColorRes int colorRes) {
        view.setColorFilter(ContextCompat.getColor(view.getContext(), colorRes));
    }

    /** Applies a pill background plus matching text colour, e.g. a support badge. */
    public static void stylePill(TextView view, @DrawableRes int backgroundRes,
                                 @ColorRes int textColorRes) {
        view.setBackgroundResource(backgroundRes);
        view.setTextColor(ContextCompat.getColor(view.getContext(), textColorRes));
    }

    /** Recolours a background drawable without mutating the shared resource. */
    public static void tintBackground(View view, @ColorRes int colorRes) {
        Drawable background = view.getBackground();
        if (background == null) {
            return;
        }
        Context context = view.getContext();
        Drawable wrapped = DrawableCompat.wrap(background.mutate());
        DrawableCompat.setTint(wrapped, ContextCompat.getColor(context, colorRes));
        view.setBackground(wrapped);
    }
}

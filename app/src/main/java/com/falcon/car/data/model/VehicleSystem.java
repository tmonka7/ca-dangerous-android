package com.falcon.car.data.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.falcon.car.R;

/** One ECU grouping and the state the last scan left it in. */
public final class VehicleSystem {

    public enum Id {
        ENGINE(R.string.system_engine, R.drawable.ic_engine),
        TRANSMISSION(R.string.system_transmission, R.drawable.ic_transmission),
        ABS(R.string.system_abs, R.drawable.ic_abs),
        SRS(R.string.system_srs, R.drawable.ic_srs),
        BODY(R.string.system_body, R.drawable.ic_body),
        TPMS(R.string.system_tpms, R.drawable.ic_tpms),
        HVAC(R.string.system_hvac, R.drawable.ic_hvac),
        GATEWAY(R.string.system_gateway, R.drawable.ic_gateway);

        @StringRes
        private final int labelRes;
        @DrawableRes
        private final int iconRes;

        Id(@StringRes int labelRes, @DrawableRes int iconRes) {
            this.labelRes = labelRes;
            this.iconRes = iconRes;
        }

        @StringRes
        public int getLabelRes() {
            return labelRes;
        }

        @DrawableRes
        public int getIconRes() {
            return iconRes;
        }
    }

    public enum Status {
        GOOD(R.string.status_good, R.color.state_good, R.drawable.ic_check),
        WARNING(R.string.status_warning, R.color.state_warning, R.drawable.ic_warning),
        FAULT(R.string.status_fault, R.color.state_fault, R.drawable.ic_error),
        PENDING(R.string.status_pending, R.color.state_pending, R.drawable.ic_pending);

        @StringRes
        private final int labelRes;
        @ColorRes
        private final int colorRes;
        @DrawableRes
        private final int iconRes;

        Status(@StringRes int labelRes, @ColorRes int colorRes, @DrawableRes int iconRes) {
            this.labelRes = labelRes;
            this.colorRes = colorRes;
            this.iconRes = iconRes;
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
        public int getIconRes() {
            return iconRes;
        }
    }

    private final Id id;
    private final Status status;
    private final int issueCount;

    public VehicleSystem(Id id, Status status, int issueCount) {
        this.id = id;
        this.status = status;
        this.issueCount = issueCount;
    }

    public Id getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public int getIssueCount() {
        return issueCount;
    }
}

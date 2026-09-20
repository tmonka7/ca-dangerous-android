package com.falcon.car.data.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.falcon.car.R;

/** One trouble code as read from the vehicle. */
public final class Dtc {

    /** Which service the code came from. */
    public enum Status {
        STORED(R.string.dtc_status_stored, R.color.state_fault, "03"),
        PENDING(R.string.dtc_status_pending, R.color.state_warning, "07"),
        PERMANENT(R.string.dtc_status_permanent, R.color.redline_red_bright, "0A");

        @StringRes
        private final int labelRes;
        @ColorRes
        private final int colorRes;
        private final String mode;

        Status(@StringRes int labelRes, @ColorRes int colorRes, String mode) {
            this.labelRes = labelRes;
            this.colorRes = colorRes;
            this.mode = mode;
        }

        @StringRes
        public int getLabelRes() {
            return labelRes;
        }

        @ColorRes
        public int getColorRes() {
            return colorRes;
        }

        /** OBD service that reports this class of code. */
        public String getMode() {
            return mode;
        }

        /** Expected first byte of the reply, i.e. the mode plus 0x40. */
        public String getResponseMode() {
            return Integer.toHexString(Integer.parseInt(mode, 16) + 0x40).toUpperCase();
        }
    }

    private final String code;
    private final Status status;

    public Dtc(String code, Status status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Dtc)) {
            return false;
        }
        Dtc that = (Dtc) other;
        return code.equals(that.code) && status == that.status;
    }

    @Override
    public int hashCode() {
        return code.hashCode() * 31 + status.hashCode();
    }
}

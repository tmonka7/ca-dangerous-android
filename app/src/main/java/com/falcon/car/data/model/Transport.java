package com.falcon.car.data.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.falcon.car.R;

/**
 * Physical link between the phone and the VCI. Deliberately separate from
 * {@link Protocol} and {@link ConnectorType}: a single adapter model may offer
 * several transports while speaking only a subset of the protocols, and the UI
 * has to be able to say so.
 */
public enum Transport {

    BLUETOOTH_CLASSIC(R.string.transport_bt, R.drawable.ic_bluetooth),
    BLUETOOTH_LE(R.string.transport_ble, R.drawable.ic_bluetooth_le),
    WIFI(R.string.transport_wifi, R.drawable.ic_wifi),
    USB_OTG(R.string.transport_usb, R.drawable.ic_usb),
    ETHERNET(R.string.transport_ethernet, R.drawable.ic_ethernet),
    J2534(R.string.transport_j2534, R.drawable.ic_connector);

    @StringRes
    private final int labelRes;
    @DrawableRes
    private final int iconRes;

    Transport(@StringRes int labelRes, @DrawableRes int iconRes) {
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

    /** Transports the connect screen offers as a starting point. */
    public static Transport[] selectable() {
        return new Transport[]{BLUETOOTH_CLASSIC, BLUETOOTH_LE, WIFI, USB_OTG};
    }
}

package com.falcon.car.data.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.falcon.car.R;

import java.util.Arrays;
import java.util.List;

/**
 * The physical socket in the vehicle. Pin count, protocol set and vehicle class
 * are data rather than layout, so adding a connector is a data change only.
 */
public enum ConnectorType {

    OBD2_16PIN("OBD-II", 16, Group.PASSENGER, R.drawable.ic_connector,
            Protocol.ISO_15765_4_CAN, Protocol.ISO_14230_4_KWP, Protocol.ISO_9141_2,
            Protocol.SAE_J1850_PWM, Protocol.SAE_J1850_VPW),

    OBD2_CAN_FD("OBD-II CAN FD", 16, Group.PASSENGER, R.drawable.ic_connector,
            Protocol.ISO_15765_CAN_FD, Protocol.ISO_15765_4_CAN),

    J1939_9PIN("J1939", 9, Group.COMMERCIAL, R.drawable.ic_truck,
            Protocol.SAE_J1939),

    J1708_6PIN("J1708", 6, Group.COMMERCIAL, R.drawable.ic_truck,
            Protocol.SAE_J1708),

    BENZ_14PIN("Benz", 14, Group.MANUFACTURER, R.drawable.ic_car,
            Protocol.ISO_15765_4_CAN, Protocol.ISO_9141_2),

    TOYOTA_17PIN("Toyota", 17, Group.MANUFACTURER, R.drawable.ic_car,
            Protocol.ISO_14230_4_KWP, Protocol.ISO_9141_2);

    /** Vehicle class the connector belongs to; drives the section headers. */
    public enum Group {
        PASSENGER(R.string.connector_group_passenger),
        COMMERCIAL(R.string.connector_group_commercial),
        MANUFACTURER(R.string.connector_group_manufacturer);

        @StringRes
        private final int titleRes;

        Group(@StringRes int titleRes) {
            this.titleRes = titleRes;
        }

        @StringRes
        public int getTitleRes() {
            return titleRes;
        }
    }

    private final String displayName;
    private final int pinCount;
    private final Group group;
    @DrawableRes
    private final int iconRes;
    private final List<Protocol> protocols;

    ConnectorType(String displayName, int pinCount, Group group,
                  @DrawableRes int iconRes, Protocol... protocols) {
        this.displayName = displayName;
        this.pinCount = pinCount;
        this.group = group;
        this.iconRes = iconRes;
        this.protocols = Arrays.asList(protocols);
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPinCount() {
        return pinCount;
    }

    public Group getGroup() {
        return group;
    }

    @DrawableRes
    public int getIconRes() {
        return iconRes;
    }

    public List<Protocol> getProtocols() {
        return protocols;
    }
}

package com.falcon.car.data.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.falcon.car.R;

/**
 * An on-board readiness monitor as reported by mode 01 PID 01.
 *
 * <p>Continuous monitors live in byte B of the reply, the non-continuous ones
 * in bytes C (supported) and D (still incomplete), which is why each constant
 * carries the byte it belongs to along with its bit.
 */
public enum Monitor {

    MISFIRE(Group.CONTINUOUS, 0, R.string.monitor_misfire, R.drawable.ic_engine),
    FUEL_SYSTEM(Group.CONTINUOUS, 1, R.string.monitor_fuel_system, R.drawable.ic_fuel),
    COMPONENTS(Group.CONTINUOUS, 2, R.string.monitor_components, R.drawable.ic_gateway),

    CATALYST(Group.NON_CONTINUOUS, 0, R.string.monitor_catalyst, R.drawable.ic_catalyst),
    HEATED_CATALYST(Group.NON_CONTINUOUS, 1, R.string.monitor_heated_catalyst,
            R.drawable.ic_catalyst),
    EVAP(Group.NON_CONTINUOUS, 2, R.string.monitor_evap, R.drawable.ic_fuel),
    SECONDARY_AIR(Group.NON_CONTINUOUS, 3, R.string.monitor_secondary_air, R.drawable.ic_hvac),
    AC_REFRIGERANT(Group.NON_CONTINUOUS, 4, R.string.monitor_ac, R.drawable.ic_hvac),
    O2_SENSOR(Group.NON_CONTINUOUS, 5, R.string.monitor_o2, R.drawable.ic_sensor),
    O2_HEATER(Group.NON_CONTINUOUS, 6, R.string.monitor_o2_heater, R.drawable.ic_sensor),
    EGR(Group.NON_CONTINUOUS, 7, R.string.monitor_egr, R.drawable.ic_connector);

    public enum Group {
        CONTINUOUS, NON_CONTINUOUS
    }

    private final Group group;
    private final int bit;
    @StringRes
    private final int labelRes;
    @DrawableRes
    private final int iconRes;

    Monitor(Group group, int bit, @StringRes int labelRes, @DrawableRes int iconRes) {
        this.group = group;
        this.bit = bit;
        this.labelRes = labelRes;
        this.iconRes = iconRes;
    }

    public Group getGroup() {
        return group;
    }

    public int getBit() {
        return bit;
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

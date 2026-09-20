package com.falcon.car.ui;

import android.content.Context;

import androidx.annotation.StringRes;

import com.falcon.car.R;
import com.falcon.car.obd.Pid;

/**
 * Display names for live parameters. Kept out of {@link Pid} so the OBD layer
 * stays free of Android resources and remains unit-testable.
 */
public final class PidLabels {

    private PidLabels() {
    }

    public static String labelFor(Context context, Pid pid) {
        return context.getString(labelRes(pid));
    }

    @StringRes
    public static int labelRes(Pid pid) {
        switch (pid) {
            case ENGINE_RPM:
                return R.string.label_rpm;
            case VEHICLE_SPEED:
                return R.string.label_speed;
            case COOLANT_TEMP:
                return R.string.label_coolant;
            case INTAKE_TEMP:
                return R.string.pid_intake_temp;
            case ENGINE_LOAD:
                return R.string.pid_engine_load;
            case THROTTLE_POSITION:
                return R.string.pid_throttle;
            case FUEL_LEVEL:
                return R.string.pid_fuel_level;
            case MAF:
                return R.string.pid_maf;
            case MAP:
                return R.string.pid_map;
            case TIMING_ADVANCE:
                return R.string.pid_timing;
            case SHORT_FUEL_TRIM_1:
                return R.string.pid_stft;
            case LONG_FUEL_TRIM_1:
                return R.string.pid_ltft;
            case CONTROL_MODULE_VOLTAGE:
            default:
                return R.string.label_battery;
        }
    }
}

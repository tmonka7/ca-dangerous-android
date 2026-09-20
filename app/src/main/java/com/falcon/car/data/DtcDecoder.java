package com.falcon.car.data;

import android.content.Context;

import androidx.annotation.StringRes;

import com.falcon.car.R;

import java.util.Locale;

/**
 * Describes a trouble code, preferring the bundled dictionary and falling back
 * to what the code itself encodes.
 *
 * <p>A code is not opaque: the first character names the system, the second
 * says whether the definition is legislated or left to the manufacturer, and
 * for powertrain codes the third groups the subsystem. That is enough to give
 * a technician a useful line for a code no table covers, without inventing a
 * fault description.
 */
public final class DtcDecoder {

    private DtcDecoder() {
    }

    /** Dictionary text when known, otherwise a description built from structure. */
    public static String describe(Context context, String code) {
        String known = DtcDictionary.lookup(context, code);
        return known != null ? known : structuralDescription(context, code);
    }

    /** True when the code came out of the bundled table rather than the decoder. */
    public static boolean isKnown(Context context, String code) {
        return DtcDictionary.lookup(context, code) != null;
    }

    public static String structuralDescription(Context context, String code) {
        String system = context.getString(systemRes(code));
        String kind = context.getString(isManufacturerSpecific(code)
                ? R.string.dtc_kind_manufacturer
                : R.string.dtc_kind_generic);

        int subsystemRes = subsystemRes(code);
        if (subsystemRes == 0) {
            return system + " \u00B7 " + kind;
        }
        return system + " \u00B7 " + kind + " \u00B7 " + context.getString(subsystemRes);
    }

    @StringRes
    public static int systemRes(String code) {
        switch (systemLetter(code)) {
            case 'C':
                return R.string.dtc_system_chassis;
            case 'B':
                return R.string.dtc_system_body;
            case 'U':
                return R.string.dtc_system_network;
            default:
                return R.string.dtc_system_powertrain;
        }
    }

    /**
     * Second character: 0 is the legislated set, 2 and 3 are further generic
     * blocks for powertrain codes, and 1 is always the manufacturer's own.
     */
    public static boolean isManufacturerSpecific(String code) {
        if (code == null || code.length() < 2) {
            return false;
        }
        char second = code.charAt(1);
        if (systemLetter(code) == 'P') {
            return second == '1';
        }
        return second == '1' || second == '2';
    }

    /** Powertrain subsystem from the third character; 0 for other systems. */
    @StringRes
    public static int subsystemRes(String code) {
        if (code == null || code.length() < 3 || systemLetter(code) != 'P') {
            return 0;
        }
        switch (Character.toUpperCase(code.charAt(2))) {
            case '0':
            case '1':
                return R.string.dtc_sub_fuel_air;
            case '2':
                return R.string.dtc_sub_injector;
            case '3':
                return R.string.dtc_sub_ignition;
            case '4':
                return R.string.dtc_sub_emissions;
            case '5':
                return R.string.dtc_sub_speed_idle;
            case '6':
                return R.string.dtc_sub_computer;
            case '7':
            case '8':
            case '9':
                return R.string.dtc_sub_transmission;
            case 'A':
            case 'B':
            case 'C':
                return R.string.dtc_sub_hybrid;
            default:
                return 0;
        }
    }

    private static char systemLetter(String code) {
        if (code == null || code.isEmpty()) {
            return 'P';
        }
        return Character.toUpperCase(code.charAt(0));
    }

    /** Maps a code to the vehicle system tile it belongs under on the home screen. */
    public static String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.US);
    }
}

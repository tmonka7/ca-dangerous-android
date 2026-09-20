package com.falcon.car.util;

import com.falcon.car.R;

/**
 * ISO 3779 VIN checks.
 *
 * <p>Length and character rules are hard errors. The check digit in position 9
 * is only mandatory for North American vehicles, so a mismatch is reported as a
 * warning and does not block entry - rejecting it outright would lock out most
 * JDM and European VINs.
 */
public final class VinValidator {

    private static final int VIN_LENGTH = 17;
    private static final String WEIGHTS = "8765432X098765432";

    private VinValidator() {
    }

    /** Outcome of a check: a blocking error, a soft warning, or neither. */
    public static final class Result {
        public final int errorRes;
        public final int warningRes;

        Result(int errorRes, int warningRes) {
            this.errorRes = errorRes;
            this.warningRes = warningRes;
        }

        public boolean isValid() {
            return errorRes == 0;
        }
    }

    public static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(java.util.Locale.US);
    }

    public static Result validate(String raw) {
        String vin = normalize(raw);

        if (vin.length() != VIN_LENGTH) {
            return new Result(R.string.vin_error_length, 0);
        }
        for (int i = 0; i < VIN_LENGTH; i++) {
            char c = vin.charAt(i);
            if (c == 'I' || c == 'O' || c == 'Q') {
                return new Result(R.string.vin_error_chars, 0);
            }
            if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                return new Result(R.string.vin_error_chars, 0);
            }
        }
        if (!checkDigitMatches(vin)) {
            return new Result(0, R.string.vin_error_checksum);
        }
        return new Result(0, 0);
    }

    private static boolean checkDigitMatches(String vin) {
        int sum = 0;
        for (int i = 0; i < VIN_LENGTH; i++) {
            int value = transliterate(vin.charAt(i));
            if (value < 0) {
                return false;
            }
            sum += value * weight(i);
        }
        int remainder = sum % 11;
        char expected = remainder == 10 ? 'X' : (char) ('0' + remainder);
        return vin.charAt(8) == expected;
    }

    private static int weight(int index) {
        char w = WEIGHTS.charAt(index);
        return w == 'X' ? 10 : w - '0';
    }

    private static int transliterate(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        switch (c) {
            case 'A': case 'J': return 1;
            case 'B': case 'K': case 'S': return 2;
            case 'C': case 'L': case 'T': return 3;
            case 'D': case 'M': case 'U': return 4;
            case 'E': case 'N': case 'V': return 5;
            case 'F': case 'W': return 6;
            case 'G': case 'P': case 'X': return 7;
            case 'H': case 'Y': return 8;
            case 'R': case 'Z': return 9;
            default: return -1;
        }
    }
}

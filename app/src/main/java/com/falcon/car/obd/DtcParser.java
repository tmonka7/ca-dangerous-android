package com.falcon.car.obd;

import com.falcon.car.data.model.Dtc;

import java.util.ArrayList;
import java.util.List;

/** Turns a mode 03 / 07 / 0A reply into trouble codes. */
public final class DtcParser {

    private static final char[] SYSTEMS = {'P', 'C', 'B', 'U'};

    private DtcParser() {
    }

    /**
     * @param hex    cleaned reply, hex digits only
     * @param status which service produced it, used for the expected mode byte
     */
    public static List<Dtc> parse(String hex, Dtc.Status status) {
        List<Dtc> codes = new ArrayList<>();
        if (hex == null || hex.isEmpty()) {
            return codes;
        }

        String marker = status.getResponseMode();
        int index = hex.indexOf(marker);
        if (index < 0) {
            return codes;
        }

        String payload = hex.substring(index + marker.length());
        int byteCount = payload.length() / 2;

        // CAN replies prefix the list with a count byte; the ISO 9141 and KWP
        // formats do not. An odd number of payload bytes means the count is
        // there, because the codes themselves are always two bytes each.
        if (byteCount % 2 == 1) {
            payload = payload.substring(2);
            byteCount--;
        }

        for (int i = 0; i + 4 <= payload.length(); i += 4) {
            String raw = payload.substring(i, i + 4);
            String code = decode(raw);
            if (code != null && !containsCode(codes, code)) {
                codes.add(new Dtc(code, status));
            }
        }
        return codes;
    }

    private static boolean containsCode(List<Dtc> codes, String code) {
        for (Dtc dtc : codes) {
            if (dtc.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Expands two bytes into a code string. The top two bits select the system
     * letter, the next two the first digit, and the remaining twelve bits are
     * the three hex digits.
     */
    public static String decode(String fourHexDigits) {
        if (fourHexDigits.length() != 4) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(fourHexDigits, 16);
        } catch (NumberFormatException e) {
            return null;
        }
        if (value == 0) {
            return null; // padding
        }

        char system = SYSTEMS[(value >> 14) & 0x03];
        int firstDigit = (value >> 12) & 0x03;
        String remainder = String.format("%03X", value & 0x0FFF);
        return system + String.valueOf(firstDigit) + remainder;
    }
}

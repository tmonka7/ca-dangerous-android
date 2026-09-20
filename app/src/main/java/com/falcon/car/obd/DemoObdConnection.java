package com.falcon.car.obd;

import java.util.Locale;

/**
 * Answers the ELM327 command set from canned data so every screen above the
 * transport works without a vehicle. Values drift slightly on each read so the
 * live gauges behave like real telemetry.
 *
 * <p>The fault set is a stored P0301 and P0420 with a pending P0133.
 */
public class DemoObdConnection implements ObdConnection {

    private static final String NAME = "REDLINE Demo VCI";

    private boolean open;
    private long tick;

    @Override
    public void open() {
        open = true;
    }

    @Override
    public void close() {
        open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String send(String command, long timeoutMs) throws ObdException {
        if (!open) {
            throw new ObdException("Not connected");
        }
        tick++;
        String request = command.replace(" ", "").toUpperCase(Locale.US);

        if (request.startsWith("AT")) {
            return atResponse(request);
        }
        if (request.startsWith("02") && request.length() >= 6) {
            // Freeze frame mirrors the live value for the same PID.
            return "42" + request.substring(2, 4) + "00"
                    + pidPayload(request.substring(2, 4));
        }
        switch (request) {
            case "0100":
                return "4100BE3FA813";
            case "0101":
                // MIL on, two confirmed faults, EVAP monitor still running.
                return "41018207E504";
            case "03":
                return "430203010420";
            case "07":
                return "47010133";
            case "0A":
                return "4A00";
            case "04":
                return "44";
            default:
                break;
        }
        if (request.startsWith("01") && request.length() == 4) {
            String pid = request.substring(2, 4);
            String payload = pidPayload(pid);
            return payload.isEmpty() ? "NO DATA" : "41" + pid + payload;
        }
        return "NO DATA";
    }

    private String atResponse(String request) {
        if (request.startsWith("ATZ")) {
            return "ELM327 v1.5";
        }
        if (request.startsWith("ATDP")) {
            return "AUTO, ISO 15765-4 (CAN 11/500)";
        }
        return "OK";
    }

    /** Hex payload for a mode 01 PID, empty when the demo ECU has no value. */
    private String pidPayload(String pid) {
        switch (pid) {
            case "0C": // rpm, two bytes at four counts per rpm
                return word((int) ((1840 + drift(60)) * 4));
            case "0D": // speed
                return singleByte((int) (64 + drift(3)));
            case "05": // coolant
                return singleByte((int) (91 + drift(1)) + 40);
            case "0F": // intake air
                return singleByte(32 + 40);
            case "04": // engine load
                return percentByte(43 + drift(4));
            case "11": // throttle
                return percentByte(24 + drift(3));
            case "2F": // fuel level
                return percentByte(62);
            case "10": // MAF
                return word((int) ((12.6 + drift(1)) * 100));
            case "0B": // MAP
                return singleByte((int) (32 + drift(2)));
            case "0E": // timing advance
                return singleByte((int) ((12 + 64) * 2));
            case "06": // short fuel trim, bank 1
                return singleByte(128 + (int) drift(3));
            case "07": // long fuel trim, bank 1
                return singleByte(131);
            case "42": // control module voltage
                return word((int) ((13.8 + drift(0.1)) * 1000));
            default:
                return "";
        }
    }

    /** Small deterministic wobble so the gauges are never perfectly static. */
    private double drift(double amplitude) {
        return Math.sin(tick / 3.0) * amplitude;
    }

    private String singleByte(int value) {
        return String.format(Locale.US, "%02X", clamp(value, 0, 255));
    }

    private String percentByte(double percent) {
        return singleByte((int) Math.round(percent * 255.0 / 100.0));
    }

    private String word(int value) {
        return String.format(Locale.US, "%04X", clamp(value, 0, 65535));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

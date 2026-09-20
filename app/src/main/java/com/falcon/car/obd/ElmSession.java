package com.falcon.car.obd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Speaks ELM327 over an {@link ObdConnection}: brings the adapter into a known
 * state, then issues OBD requests and hands back clean hex.
 */
public class ElmSession {

    private static final long SHORT_TIMEOUT_MS = 3000L;
    private static final long LONG_TIMEOUT_MS = 9000L;

    private final ObdConnection connection;
    private String adapterId = "";
    private String protocolName = "";

    public ElmSession(ObdConnection connection) {
        this.connection = connection;
    }

    public ObdConnection getConnection() {
        return connection;
    }

    public String getAdapterId() {
        return adapterId;
    }

    /** Protocol the adapter negotiated, e.g. "ISO 15765-4 (CAN 11/500)". */
    public String getProtocolName() {
        return protocolName;
    }

    /**
     * Resets the adapter, turns off the noise that makes parsing fragile, and
     * lets it auto-detect the bus.
     */
    public void initialize() throws IOException {
        adapterId = raw("ATZ", LONG_TIMEOUT_MS).replace("ATZ", "").trim();

        command("ATE0");  // echo off - otherwise every reply repeats the request
        command("ATL0");  // no linefeeds
        command("ATS0");  // no spaces
        command("ATH0");  // no headers
        command("ATSP0"); // automatic protocol search

        // The first real request is what actually forces protocol detection.
        String probe = raw("0100", LONG_TIMEOUT_MS);
        if (isError(probe)) {
            throw new ObdException("No response from the vehicle bus");
        }

        protocolName = raw("ATDP", SHORT_TIMEOUT_MS).trim();
    }

    /** Sends an AT command and fails if the adapter does not accept it. */
    private void command(String at) throws IOException {
        String response = raw(at, SHORT_TIMEOUT_MS);
        if (response.toUpperCase(Locale.US).contains("?")) {
            throw new ObdException("Adapter rejected " + at);
        }
    }

    private String raw(String request, long timeoutMs) throws IOException {
        return connection.send(request, timeoutMs);
    }

    /**
     * Runs an OBD request and returns the payload as continuous hex, with the
     * adapter's chatter, frame indices and whitespace stripped.
     */
    public String request(String request) throws IOException {
        String response = raw(request, LONG_TIMEOUT_MS);
        if (isError(response)) {
            throw new ObdException(errorText(response));
        }
        return clean(response);
    }

    /** Same as {@link #request}, but an empty result instead of an exception. */
    public String requestOrEmpty(String request) {
        try {
            return request(request);
        } catch (IOException e) {
            return "";
        }
    }

    static boolean isError(String response) {
        String upper = response.toUpperCase(Locale.US);
        return upper.contains("NO DATA")
                || upper.contains("UNABLE TO CONNECT")
                || upper.contains("CAN ERROR")
                || upper.contains("BUS INIT")
                || upper.contains("BUS ERROR")
                || upper.contains("STOPPED")
                || upper.contains("ERROR")
                || upper.trim().equals("?");
    }

    private static String errorText(String response) {
        String trimmed = response.replace('\r', ' ').replace('\n', ' ').trim();
        return trimmed.isEmpty() ? "No data" : trimmed;
    }

    /**
     * Normalises a raw reply into hex digits only.
     *
     * <p>Handles the three shapes an ELM327 produces: a single line, an
     * ISO-TP multi-line reply with {@code 0:} frame indices, and a multi-line
     * reply preceded by a three digit length header.
     */
    static String clean(String response) {
        StringBuilder hex = new StringBuilder();

        for (String line : splitLines(response)) {
            String candidate = line.replace(" ", "").trim();
            if (candidate.isEmpty() || candidate.startsWith("SEARCHING")) {
                continue;
            }
            int colon = candidate.indexOf(':');
            if (colon >= 0) {
                candidate = candidate.substring(colon + 1);
            } else if (candidate.length() == 3 && isHex(candidate)) {
                continue; // ISO-TP total length header
            }
            if (!isHex(candidate)) {
                continue;
            }
            hex.append(candidate);
        }
        return hex.toString().toUpperCase(Locale.US);
    }

    private static List<String> splitLines(String response) {
        List<String> lines = new ArrayList<>();
        for (String part : response.split("[\r\n]+")) {
            lines.add(part);
        }
        return lines;
    }

    private static boolean isHex(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            boolean digit = c >= '0' && c <= '9';
            boolean letter = c >= 'A' && c <= 'F';
            if (!digit && !letter) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads one mode 01 parameter. Returns null when the ECU does not support
     * it, which is normal and not an error.
     */
    public Double readPid(Pid pid) {
        return readPid(pid, pid.getCommand(), "41");
    }

    /** Reads the same parameter out of the stored freeze frame (mode 02). */
    public Double readFreezeFrame(Pid pid) {
        return readPid(pid, pid.getFreezeFrameCommand(), "42");
    }

    private Double readPid(Pid pid, String command, String expectedMode) {
        String hex = requestOrEmpty(command);
        int[] payload = payloadFor(hex, expectedMode, pid.getPid());
        if (payload == null) {
            return null;
        }
        try {
            return pid.decode(payload);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Finds "{@code <mode><pid>}" in the reply and returns the bytes after it.
     * Searching rather than assuming position tolerates adapters that leave
     * extra bytes in front.
     */
    static int[] payloadFor(String hex, String expectedMode, String pid) {
        String marker = expectedMode + pid;
        int index = hex.indexOf(marker);
        if (index < 0) {
            return null;
        }
        String payload = hex.substring(index + marker.length());
        // Mode 02 replies carry a frame number before the data.
        if ("42".equals(expectedMode) && payload.length() >= 2) {
            payload = payload.substring(2);
        }
        int byteCount = payload.length() / 2;
        if (byteCount == 0) {
            return null;
        }
        int[] data = new int[byteCount];
        for (int i = 0; i < byteCount; i++) {
            data[i] = Integer.parseInt(payload.substring(i * 2, i * 2 + 2), 16);
        }
        return data;
    }

    /**
     * Reads mode 01 PID 01: lamp state, confirmed fault count and readiness.
     * Returns null when the ECU does not answer.
     */
    public MonitorStatus readStatus() {
        String hex = requestOrEmpty("0101");
        int[] payload = payloadFor(hex, "41", "01");
        return payload == null ? null : MonitorStatus.parse(payload);
    }
}

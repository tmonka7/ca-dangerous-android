package com.falcon.car.obd;

import com.falcon.car.data.model.Monitor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Decoded mode 01 PID 01: the malfunction indicator lamp, the confirmed fault
 * count, and which readiness monitors have finished running.
 */
public final class MonitorStatus {

    private final boolean milOn;
    private final int dtcCount;
    private final Map<Monitor, Boolean> supported = new EnumMap<>(Monitor.class);
    private final Map<Monitor, Boolean> complete = new EnumMap<>(Monitor.class);

    private MonitorStatus(boolean milOn, int dtcCount) {
        this.milOn = milOn;
        this.dtcCount = dtcCount;
    }

    public boolean isMilOn() {
        return milOn;
    }

    /** Confirmed faults the ECU is reporting, which drives the MIL. */
    public int getDtcCount() {
        return dtcCount;
    }

    public boolean isSupported(Monitor monitor) {
        Boolean value = supported.get(monitor);
        return value != null && value;
    }

    /** True when the monitor has finished its drive cycle. */
    public boolean isComplete(Monitor monitor) {
        Boolean value = complete.get(monitor);
        return value != null && value;
    }

    public int supportedCount() {
        int count = 0;
        for (Monitor monitor : Monitor.values()) {
            if (isSupported(monitor)) {
                count++;
            }
        }
        return count;
    }

    public int incompleteCount() {
        int count = 0;
        for (Monitor monitor : Monitor.values()) {
            if (isSupported(monitor) && !isComplete(monitor)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Builds the status from the four payload bytes of PID 01.
     *
     * <p>Byte A holds the lamp in bit 7 and the fault count in the low seven
     * bits. In byte B the low nibble marks continuous monitors as supported and
     * the high nibble marks them as still running. Bytes C and D carry the
     * non-continuous monitors, supported and incomplete respectively.
     */
    public static MonitorStatus parse(int[] data) {
        if (data == null || data.length < 4) {
            return null;
        }
        int a = data[0];
        int b = data[1];
        int c = data[2];
        int d = data[3];

        MonitorStatus status = new MonitorStatus((a & 0x80) != 0, a & 0x7F);

        for (Monitor monitor : Monitor.values()) {
            int bit = monitor.getBit();
            if (monitor.getGroup() == Monitor.Group.CONTINUOUS) {
                boolean isSupported = (b & (1 << bit)) != 0;
                boolean running = (b & (1 << (bit + 4))) != 0;
                status.supported.put(monitor, isSupported);
                status.complete.put(monitor, isSupported && !running);
            } else {
                boolean isSupported = (c & (1 << bit)) != 0;
                boolean running = (d & (1 << bit)) != 0;
                status.supported.put(monitor, isSupported);
                status.complete.put(monitor, isSupported && !running);
            }
        }
        return status;
    }
}

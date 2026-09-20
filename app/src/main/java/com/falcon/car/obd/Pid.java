package com.falcon.car.obd;

/**
 * Mode 01 parameters the app reads, with the arithmetic from SAE J1979.
 *
 * <p>{@code decode} receives the payload bytes that follow the {@code 41 xx}
 * echo, so each constant only deals with its own A/B bytes.
 */
public enum Pid {

    ENGINE_RPM("0C", "rpm", 0) {
        @Override
        public double decode(int[] d) {
            return ((d[0] * 256) + d[1]) / 4.0;
        }
    },
    VEHICLE_SPEED("0D", "km/h", 0) {
        @Override
        public double decode(int[] d) {
            return d[0];
        }
    },
    COOLANT_TEMP("05", "\u00B0C", 0) {
        @Override
        public double decode(int[] d) {
            return d[0] - 40;
        }
    },
    INTAKE_TEMP("0F", "\u00B0C", 0) {
        @Override
        public double decode(int[] d) {
            return d[0] - 40;
        }
    },
    ENGINE_LOAD("04", "%", 0) {
        @Override
        public double decode(int[] d) {
            return d[0] * 100.0 / 255.0;
        }
    },
    THROTTLE_POSITION("11", "%", 0) {
        @Override
        public double decode(int[] d) {
            return d[0] * 100.0 / 255.0;
        }
    },
    FUEL_LEVEL("2F", "%", 0) {
        @Override
        public double decode(int[] d) {
            return d[0] * 100.0 / 255.0;
        }
    },
    MAF("10", "g/s", 2) {
        @Override
        public double decode(int[] d) {
            return ((d[0] * 256) + d[1]) / 100.0;
        }
    },
    MAP("0B", "kPa", 0) {
        @Override
        public double decode(int[] d) {
            return d[0];
        }
    },
    TIMING_ADVANCE("0E", "\u00B0", 1) {
        @Override
        public double decode(int[] d) {
            return (d[0] / 2.0) - 64.0;
        }
    },
    SHORT_FUEL_TRIM_1("06", "%", 1) {
        @Override
        public double decode(int[] d) {
            return (d[0] - 128) * 100.0 / 128.0;
        }
    },
    LONG_FUEL_TRIM_1("07", "%", 1) {
        @Override
        public double decode(int[] d) {
            return (d[0] - 128) * 100.0 / 128.0;
        }
    },
    CONTROL_MODULE_VOLTAGE("42", "V", 1) {
        @Override
        public double decode(int[] d) {
            return ((d[0] * 256) + d[1]) / 1000.0;
        }
    };

    private final String pid;
    private final String unit;
    private final int decimals;

    Pid(String pid, String unit, int decimals) {
        this.pid = pid;
        this.unit = unit;
        this.decimals = decimals;
    }

    /** Two hex digits identifying the parameter, e.g. "0C". */
    public String getPid() {
        return pid;
    }

    /** Mode 01 request, e.g. "010C". */
    public String getCommand() {
        return "01" + pid;
    }

    /** Mode 02 request for the stored freeze frame, e.g. "020C00". */
    public String getFreezeFrameCommand() {
        return "02" + pid + "00";
    }

    public String getUnit() {
        return unit;
    }

    public int getDecimals() {
        return decimals;
    }

    public abstract double decode(int[] data);
}

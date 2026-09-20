package com.falcon.car.data.model;

/** A vehicle the technician works on. */
public final class Vehicle {

    private final String make;
    private final String model;
    private final int year;
    private final String engine;
    private final String vin;
    private final String lastScan;

    public Vehicle(String make, String model, int year, String engine, String vin, String lastScan) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.engine = engine;
        this.vin = vin;
        this.lastScan = lastScan;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public String getEngine() {
        return engine;
    }

    public String getVin() {
        return vin;
    }

    public String getLastScan() {
        return lastScan;
    }

    public String getDisplayName() {
        return make + " " + model;
    }

    /** Year and engine on one line, e.g. "2021 - 2.5L Hybrid". */
    public String getSpecLine() {
        if (engine == null || engine.isEmpty()) {
            return String.valueOf(year);
        }
        return year + " \u00B7 " + engine;
    }

    /** VIN with everything but the last four characters masked, for reports. */
    public String getMaskedVin() {
        if (vin == null || vin.length() < 4) {
            return "";
        }
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < vin.length() - 4; i++) {
            masked.append('*');
        }
        masked.append(vin.substring(vin.length() - 4));
        return masked.toString();
    }
}

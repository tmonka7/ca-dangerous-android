package com.falcon.car.data.model;

/** One adapter as it appears during a scan: a profile plus live link state. */
public final class DiscoveredDevice {

    public enum State {
        AVAILABLE, CONNECTING, CONNECTED
    }

    private final String name;
    private final String address;
    private final AdapterProfile profile;
    private final Transport transport;
    private final int signalLevel;
    private State state;

    public DiscoveredDevice(String name, AdapterProfile profile, Transport transport,
                            int signalLevel, State state) {
        this(name, "", profile, transport, signalLevel, state);
    }

    public DiscoveredDevice(String name, String address, AdapterProfile profile,
                            Transport transport, int signalLevel, State state) {
        this.name = name;
        this.address = address == null ? "" : address;
        this.profile = profile;
        this.transport = transport;
        this.signalLevel = signalLevel;
        this.state = state;
    }

    /** Bluetooth MAC of a real device; empty for simulated ones. */
    public String getAddress() {
        return address;
    }

    public boolean isSimulated() {
        return address.isEmpty();
    }

    public String getName() {
        return name;
    }

    public AdapterProfile getProfile() {
        return profile;
    }

    public Transport getTransport() {
        return transport;
    }

    /** 0-4, as drawn by the signal bars widget. */
    public int getSignalLevel() {
        return signalLevel;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}

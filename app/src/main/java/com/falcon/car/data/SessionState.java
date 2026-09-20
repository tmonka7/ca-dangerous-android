package com.falcon.car.data;

import com.falcon.car.data.model.ConnectorType;
import com.falcon.car.data.model.DiscoveredDevice;
import com.falcon.car.data.model.Transport;
import com.falcon.car.data.model.Vehicle;

/**
 * What the technician has picked so far: vehicle, connector, transport and the
 * connected VCI. Process-scoped on purpose - nothing here survives a cold start
 * until the persistence layer arrives.
 */
public final class SessionState {

    private static SessionState instance;

    private Vehicle vehicle = MockData.currentVehicle();
    private ConnectorType connector = ConnectorType.OBD2_16PIN;
    private Transport transport = Transport.BLUETOOTH_CLASSIC;
    private DiscoveredDevice connectedDevice;

    private SessionState() {
    }

    public static synchronized SessionState get() {
        if (instance == null) {
            instance = new SessionState();
        }
        return instance;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ConnectorType getConnector() {
        return connector;
    }

    public void setConnector(ConnectorType connector) {
        this.connector = connector;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public DiscoveredDevice getConnectedDevice() {
        return connectedDevice;
    }

    public void setConnectedDevice(DiscoveredDevice device) {
        this.connectedDevice = device;
    }

    public boolean isConnected() {
        return connectedDevice != null;
    }
}

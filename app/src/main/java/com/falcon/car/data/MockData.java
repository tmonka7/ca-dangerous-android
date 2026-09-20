package com.falcon.car.data;

import com.falcon.car.data.model.AdapterProfile;
import com.falcon.car.data.model.DiscoveredDevice;
import com.falcon.car.data.model.Transport;
import com.falcon.car.data.model.Vehicle;
import com.falcon.car.data.model.VehicleSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stand-in data for the phase 1 shell. Every screen reads through here, so the
 * real sources (adapter discovery, vehicle database, scan results) can replace
 * these methods one at a time without touching the UI.
 */
public final class MockData {

    private MockData() {
    }

    public static Vehicle currentVehicle() {
        return recentVehicles().get(0);
    }

    public static List<Vehicle> recentVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("Toyota", "Camry", 2021, "2.5L Hybrid",
                "JTNAK3BEXK3034567", "Today 00:32"));
        vehicles.add(new Vehicle("BMW", "320i", 2020, "2.0L",
                "WBA8E9103G5T12345", "Sep 18"));
        vehicles.add(new Vehicle("Nissan", "Note", 2019, "1.2L",
                "SJNFAAE12U1234567", "Sep 12"));
        return vehicles;
    }

    /** Last scan result for the current vehicle: 82% health, three issues. */
    public static List<VehicleSystem> systems() {
        List<VehicleSystem> systems = new ArrayList<>();
        systems.add(new VehicleSystem(VehicleSystem.Id.ENGINE, VehicleSystem.Status.GOOD, 0));
        systems.add(new VehicleSystem(VehicleSystem.Id.TRANSMISSION, VehicleSystem.Status.GOOD, 0));
        systems.add(new VehicleSystem(VehicleSystem.Id.ABS, VehicleSystem.Status.GOOD, 0));
        systems.add(new VehicleSystem(VehicleSystem.Id.SRS, VehicleSystem.Status.FAULT, 1));
        systems.add(new VehicleSystem(VehicleSystem.Id.BODY, VehicleSystem.Status.WARNING, 2));
        systems.add(new VehicleSystem(VehicleSystem.Id.TPMS, VehicleSystem.Status.GOOD, 0));
        return systems;
    }

    public static int healthScore() {
        return 82;
    }

    public static int dtcCount() {
        int total = 0;
        for (VehicleSystem system : systems()) {
            total += system.getIssueCount();
        }
        return total;
    }

    public static float batteryVoltage() {
        return 13.8f;
    }

    public static int coolantTemp() {
        return 91;
    }

    public static int engineRpm() {
        return 0;
    }

    /**
     * Devices a scan would surface. Each is tied to a catalogue profile so the
     * row can show real transport and protocol coverage rather than a guess.
     */
    public static List<DiscoveredDevice> discoveredDevices() {
        List<DiscoveredDevice> devices = new ArrayList<>();
        devices.add(device("OBDLink MX+", "OBDLink", Transport.BLUETOOTH_CLASSIC, 4,
                DiscoveredDevice.State.CONNECTED));
        devices.add(device("Veepeak OBDCheck BLE+", "Veepeak", Transport.BLUETOOTH_LE, 3,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("vLinker MC+", "Vgate", Transport.BLUETOOTH_LE, 3,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("MaxiAP AP200", "Autel", Transport.BLUETOOTH_CLASSIC, 2,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("X-431 VCI", "LAUNCH", Transport.WIFI, 4,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("TOPDON VCI", "TOPDON", Transport.BLUETOOTH_CLASSIC, 2,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("THINKDIAG 2", "THINKCAR", Transport.BLUETOOTH_CLASSIC, 1,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("ANCEL BD500", "ANCEL", Transport.BLUETOOTH_CLASSIC, 2,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("ELM327 v1.5", "ELM327", Transport.WIFI, 3,
                DiscoveredDevice.State.AVAILABLE));
        devices.add(device("J2534 Pass-Thru", "J2534", Transport.USB_OTG, 4,
                DiscoveredDevice.State.AVAILABLE));
        return devices;
    }

    private static DiscoveredDevice device(String name, String brand, Transport transport,
                                           int signal, DiscoveredDevice.State state) {
        AdapterProfile profile = AdapterCatalog.byBrand(brand);
        return new DiscoveredDevice(name, profile, transport, signal, state);
    }

    /** Devices reachable over the given transport. */
    public static List<DiscoveredDevice> devicesOn(Transport transport) {
        List<DiscoveredDevice> result = new ArrayList<>();
        for (DiscoveredDevice device : discoveredDevices()) {
            if (device.getProfile() != null
                    && device.getProfile().getTransports().contains(transport)) {
                result.add(device);
            }
        }
        return result;
    }

    public static Map<String, List<String>> modelsByMake() {
        Map<String, List<String>> models = new LinkedHashMap<>();
        models.put("Toyota", Arrays.asList("Camry", "Corolla", "RAV4", "Prius", "Hilux"));
        models.put("BMW", Arrays.asList("320i", "520d", "X3", "X5"));
        models.put("Nissan", Arrays.asList("Note", "X-Trail", "Leaf", "Navara"));
        models.put("Honda", Arrays.asList("Civic", "Fit", "CR-V"));
        models.put("Mercedes-Benz", Arrays.asList("C200", "E220d", "Sprinter"));
        models.put("Ford", Arrays.asList("Focus", "Ranger", "Transit"));
        models.put("Volkswagen", Arrays.asList("Golf", "Passat", "Tiguan"));
        models.put("Subaru", Arrays.asList("Forester", "Impreza"));
        return models;
    }

    public static List<String> makes() {
        return new ArrayList<>(modelsByMake().keySet());
    }

    public static List<String> modelsFor(String make) {
        List<String> models = modelsByMake().get(make);
        return models == null ? new ArrayList<String>() : new ArrayList<>(models);
    }

    public static List<String> years() {
        List<String> years = new ArrayList<>();
        for (int year = 2025; year >= 2005; year--) {
            years.add(String.valueOf(year));
        }
        return years;
    }

    public static List<String> engines() {
        return Arrays.asList("1.2L", "1.5L", "1.5L Hybrid", "2.0L", "2.0L Diesel",
                "2.5L Hybrid", "3.0L");
    }
}

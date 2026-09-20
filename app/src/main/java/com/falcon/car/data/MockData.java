package com.falcon.car.data;

import com.falcon.car.data.model.Vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The vehicle catalogue the selection screen drills through, plus the recent
 * vehicles list.
 *
 * <p>Telemetry, trouble codes and readiness no longer live here - those come
 * from the adapter through {@code ObdManager}, with demo mode served by
 * {@code DemoObdConnection} so the simulation sits behind the same protocol
 * rather than beside it. What remains is reference data that a vehicle
 * database will replace.
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

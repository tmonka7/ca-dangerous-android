package com.falcon.car.data;

import com.falcon.car.data.model.AdapterProfile;
import com.falcon.car.data.model.ConnectorType;
import com.falcon.car.data.model.Protocol;
import com.falcon.car.data.model.SupportLevel;
import com.falcon.car.data.model.Transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.List;

/**
 * Capability catalogue for the VCI families the app knows about.
 *
 * <p>Entries describe a product family, not one SKU: the models line lists the
 * variants, and the transport set is the union across them, which is why the
 * device list carries the "capabilities differ between models" note. Levels here
 * are placeholders for the shell and must be checked against each vendor's
 * current specification before the connectivity layer ships.
 */
public final class AdapterCatalog {

    private static List<AdapterProfile> catalog;

    private AdapterCatalog() {
    }

    public static synchronized List<AdapterProfile> all() {
        if (catalog == null) {
            catalog = Collections.unmodifiableList(build());
        }
        return catalog;
    }

    public static List<AdapterProfile> filtered(AdapterProfile.Category category) {
        if (category == null) {
            return all();
        }
        List<AdapterProfile> result = new ArrayList<>();
        for (AdapterProfile profile : all()) {
            if (profile.isIn(category)) {
                result.add(profile);
            }
        }
        return result;
    }

    private static List<AdapterProfile> build() {
        List<AdapterProfile> list = new ArrayList<>();

        list.add(AdapterProfile.builder("OBDLink")
                .models("MX+", "CX", "LX", "EX")
                .transports(Transport.BLUETOOTH_CLASSIC, Transport.BLUETOOTH_LE, Transport.USB_OTG)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .protocols(SupportLevel.SUPPORTED, Protocol.FORD_MS_CAN, Protocol.GM_SW_CAN)
                .protocols(SupportLevel.LIMITED, Protocol.ISO_15765_CAN_FD)
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .build());

        list.add(AdapterProfile.builder("Veepeak")
                .models("OBDCheck BLE+", "OBDCheck BLE", "Mini")
                .transports(Transport.BLUETOOTH_LE, Transport.BLUETOOTH_CLASSIC)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .build());

        list.add(AdapterProfile.builder("Vgate")
                .models("vLinker MC+", "vLinker FS", "vLinker MX")
                .transports(Transport.BLUETOOTH_CLASSIC, Transport.BLUETOOTH_LE, Transport.WIFI)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .protocols(SupportLevel.LIMITED, Protocol.FORD_MS_CAN)
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .build());

        list.add(AdapterProfile.builder("Autel")
                .models("MaxiAP AP200")
                .transports(Transport.BLUETOOTH_CLASSIC)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .protocols(SupportLevel.LIMITED, Protocol.ISO_15765_CAN_FD)
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .build());

        list.add(AdapterProfile.builder("LAUNCH")
                .models("X-431 VCI", "SmartLink HD")
                .transports(Transport.BLUETOOTH_CLASSIC, Transport.WIFI, Transport.USB_OTG)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .protocols(SupportLevel.SUPPORTED, Protocol.ISO_15765_CAN_FD)
                .protocols(SupportLevel.LIMITED, Protocol.SAE_J1939, Protocol.SAE_J1708)
                .connectors(ConnectorType.OBD2_16PIN, ConnectorType.OBD2_CAN_FD,
                        ConnectorType.J1939_9PIN, ConnectorType.J1708_6PIN,
                        ConnectorType.BENZ_14PIN)
                .categories(AdapterProfile.Category.OBD2, AdapterProfile.Category.HEAVY_DUTY)
                .build());

        list.add(AdapterProfile.builder("TOPDON")
                .models("TopScan", "Phoenix VCI")
                .transports(Transport.BLUETOOTH_CLASSIC, Transport.WIFI)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .protocols(SupportLevel.LIMITED, Protocol.ISO_15765_CAN_FD)
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .build());

        list.add(AdapterProfile.builder("THINKCAR")
                .models("THINKDIAG 2")
                .transports(Transport.BLUETOOTH_CLASSIC)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .protocols(SupportLevel.LIMITED, Protocol.ISO_15765_CAN_FD)
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .build());

        list.add(AdapterProfile.builder("ANCEL")
                .models("BD500", "V6 PRO")
                .transports(Transport.BLUETOOTH_CLASSIC)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .build());

        list.add(AdapterProfile.builder("ELM327")
                .models("Generic clone")
                .transports(Transport.BLUETOOTH_CLASSIC, Transport.BLUETOOTH_LE,
                        Transport.WIFI, Transport.USB_OTG)
                .protocols(SupportLevel.SUPPORTED, Protocol.ISO_15765_4_CAN,
                        Protocol.ISO_14230_4_KWP, Protocol.ISO_9141_2)
                .protocols(SupportLevel.LIMITED, Protocol.SAE_J1850_PWM, Protocol.SAE_J1850_VPW)
                .connectors(ConnectorType.OBD2_16PIN)
                .categories(AdapterProfile.Category.OBD2)
                .overall(SupportLevel.LIMITED)
                .build());

        list.add(AdapterProfile.builder("J2534")
                .models("Pass-through interface")
                .transports(Transport.USB_OTG, Transport.WIFI, Transport.J2534)
                .protocols(SupportLevel.SUPPORTED, Protocol.legacyObd2())
                .protocols(SupportLevel.SUPPORTED, Protocol.ISO_15765_CAN_FD)
                .protocols(SupportLevel.LIMITED, Protocol.FORD_MS_CAN, Protocol.GM_SW_CAN,
                        Protocol.SAE_J1939)
                .connectors(ConnectorType.OBD2_16PIN, ConnectorType.OBD2_CAN_FD,
                        ConnectorType.J1939_9PIN)
                .categories(AdapterProfile.Category.OBD2, AdapterProfile.Category.J2534,
                        AdapterProfile.Category.HEAVY_DUTY)
                .overall(SupportLevel.LIMITED)
                .build());

        return list;
    }

    /** Looks a family up by brand, for wiring discovered devices to a profile. */
    public static AdapterProfile byBrand(String brand) {
        for (AdapterProfile profile : all()) {
            if (profile.getBrand().equalsIgnoreCase(brand)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Best guess at the family behind a Bluetooth device name. Adapters
     * advertise names like "OBDII", "vLinker MC+" or "OBDLink MX+", so the
     * match is on the brand or a distinctive model string, and anything
     * unrecognised falls back to the generic ELM327 profile.
     */
    public static AdapterProfile matchByName(String deviceName) {
        if (deviceName == null || deviceName.isEmpty()) {
            return byBrand("ELM327");
        }
        String name = deviceName.toUpperCase(Locale.US);

        for (AdapterProfile profile : all()) {
            if (name.contains(profile.getBrand().toUpperCase(Locale.US))) {
                return profile;
            }
            for (String model : profile.getModels()) {
                String token = model.toUpperCase(Locale.US);
                if (token.length() >= 4 && name.contains(token)) {
                    return profile;
                }
            }
        }
        return byBrand("ELM327");
    }
}

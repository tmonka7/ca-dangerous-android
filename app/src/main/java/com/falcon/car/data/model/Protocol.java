package com.falcon.car.data.model;

/**
 * Bus-level protocol. Names are standards designations and stay untranslated so
 * they match what is printed on the adapter and in the service literature.
 */
public enum Protocol {

    ISO_15765_4_CAN("ISO 15765-4 CAN"),
    ISO_15765_CAN_FD("ISO 15765 CAN FD"),
    ISO_14230_4_KWP("ISO 14230-4 KWP2000"),
    ISO_9141_2("ISO 9141-2"),
    SAE_J1850_PWM("SAE J1850 PWM"),
    SAE_J1850_VPW("SAE J1850 VPW"),
    FORD_MS_CAN("Ford MS-CAN"),
    GM_SW_CAN("GM SW-CAN"),
    SAE_J1939("SAE J1939"),
    SAE_J1708("SAE J1708 / J1587");

    private final String displayName;

    Protocol(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** The five legislated OBD-II protocols every compliant adapter must cover. */
    public static Protocol[] legacyObd2() {
        return new Protocol[]{
                ISO_15765_4_CAN, ISO_14230_4_KWP, ISO_9141_2, SAE_J1850_PWM, SAE_J1850_VPW
        };
    }
}

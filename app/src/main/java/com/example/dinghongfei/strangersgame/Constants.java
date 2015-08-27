package com.example.dinghongfei.strangersgame;

class Constants {

    private Constants() {
    }

    /**
     * Eddystone-UID frame type value.
     */
    static final byte UID_FRAME_TYPE = 0x00;

    /**
     * Eddystone-URL frame type value.
     */
    static final byte URL_FRAME_TYPE = 0x10;

    /**
     * Eddystone-TLM frame type value.
     */
    static final byte TLM_FRAME_TYPE = 0x20;

    /**
     * Minimum expected Tx power (in dBm) in UID and URL frames.
     */
    static final int MIN_EXPECTED_TX_POWER = -100;

    /**
     * Maximum expected Tx power (in dBm) in UID and URL frames.
     */
    static final int MAX_EXPECTED_TX_POWER = 20;

    /**
     * Fixed namespace for this application.
     */
    static final String NAMESPACE = "AAAABBBBCCCCDDDDEEEE";

    /**
     * Key for shared preference.
     */
    static final String SHARED_PREFERENCE = "SHARED_PREFERENCE";

    /**
     * Signal below the threshold will be ignored (in dBm).
     */
    static final int MIN_SIGNAL_STRENGTH = -90;
}

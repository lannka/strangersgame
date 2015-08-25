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
    static final String NAMESPACE = "2F234454F4911BA9FFA6"; //"5A2989A38A4668B4E8AD";
}


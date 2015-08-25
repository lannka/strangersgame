package com.example.dinghongfei.strangersgame;

import android.util.Log;

import static com.example.dinghongfei.strangersgame.Constants.MIN_EXPECTED_TX_POWER;
import static com.example.dinghongfei.strangersgame.Constants.MAX_EXPECTED_TX_POWER;

import java.util.Arrays;


/**
 * Basic validation of an Eddystone-URL frame. <p>
 *
 * @see <a href="https://github.com/google/eddystone/eddystone-url">URL frame specification</a>
 */
class UrlValidator {

    private static final String TAG = UrlValidator.class.getSimpleName();

    private UrlValidator() {
    }

    static void validate(String deviceAddress, byte[] serviceData, Beacon beacon) {
        beacon.hasUrlFrame = true;

        // Tx power should have reasonable values.
        int txPower = (int) serviceData[1];
        if (txPower < MIN_EXPECTED_TX_POWER || txPower > MAX_EXPECTED_TX_POWER) {
            String err = String.format("Expected URL Tx power between %d and %d, got %d",
                    MIN_EXPECTED_TX_POWER, MAX_EXPECTED_TX_POWER, txPower);
            beacon.urlStatus.txPower = err;
            logDeviceError(deviceAddress, err);
        }

        // The URL bytes should not be all zeroes.
        byte[] urlBytes = Arrays.copyOfRange(serviceData, 2, 20);
        if (Utils.isZeroed(urlBytes)) {
            String err = "URL bytes are all 0x00";
            beacon.urlStatus.urlNotSet = err;
            logDeviceError(deviceAddress, err);
        }

        // If we have a previous frame, verify the URL isn't changing.
        String url = UrlUtils.decodeUrl(serviceData);
        if (beacon.urlServiceData == null) {
            beacon.urlServiceData = serviceData;
        } else {
            String previousUrl = UrlUtils.decodeUrl(beacon.urlServiceData);
            if (!url.equals(previousUrl)) {
                String err = String.format("URL should be invariant.\nLast: %s\nthis: %s",
                        previousUrl, url);
                beacon.urlStatus.urlNotInvariant = err;
                logDeviceError(deviceAddress, err);
                beacon.urlServiceData = serviceData;
            }
        }

        beacon.urlStatus.urlValue = url;
    }

    private static void logDeviceError(String deviceAddress, String err) {
        Log.e(TAG, deviceAddress + ": " + err);
    }
}
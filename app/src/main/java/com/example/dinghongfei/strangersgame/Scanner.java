package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dingkai on 8/24/15.
 */
public class Scanner {
    private static final String TAG = "Scanner";

    // An aggressive scan for nearby devices that reports immediately.
    private static final ScanSettings SCAN_SETTINGS =
            new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0)
                    .build();

    // The Eddystone Service UUID, 0xFEAA.
    private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

    private final Activity context;
    private final BluetoothLeScanner scanner;

    private List<ScanFilter> scanFilters;
    private ScanCallback scanCallback;

    private Map<String /* device address */, Beacon> deviceToBeaconMap = new HashMap<>();

    public Scanner(Activity context, BluetoothLeScanner scanner) {
        this.context = context;
        this.scanner = scanner;
        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());
    }

    public void Start(final ScannerCallback callback) {
        if (scanner != null) {
            final TextView logView = (TextView) (context.findViewById(R.id.logView));
            logView.setMovementMethod(new ScrollingMovementMethod());
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    ScanRecord scanRecord = result.getScanRecord();
                    if (scanRecord == null) {
                        return;
                    }

                    String deviceAddress = result.getDevice().getAddress();
                    Beacon beacon;
                    if (!deviceToBeaconMap.containsKey(deviceAddress)) {
                        beacon = new Beacon(deviceAddress, result.getRssi());
                        deviceToBeaconMap.put(deviceAddress, beacon);
                    } else {
                        deviceToBeaconMap.get(deviceAddress).rssi = result.getRssi();
                    }

                    byte[] serviceData = scanRecord.getServiceData(EDDYSTONE_SERVICE_UUID);
                    validateServiceData(deviceAddress, serviceData);

                    beacon = deviceToBeaconMap.get(deviceAddress);

                    if (beacon.uidStatus != null && beacon.uidStatus.uidValue != null) {
                        String namespace = beacon.uidStatus.uidValue.substring(0, 20);
                        String instance_id = beacon.uidStatus.uidValue.substring(20, 32);
                        if (namespace.toLowerCase().equals(Constants.NAMESPACE.toLowerCase())){
//                            && (result.getRssi() > Constants.MIN_SIGNAL_STRENGTH)) {
                            callback.onDetected(instance_id);

                            Log.i(TAG, deviceAddress + " " + Utils.toHexString(serviceData) + " " + result.getRssi());
                            logView.append(instance_id + " " + result.getRssi() + " " + beacon.uidStatus.txPower + "\n");
                            // find the amount we need to scroll.  This works by
                            // asking the TextView's internal layout for the position
                            // of the final line and then subtracting the TextView's height
                            final int scrollAmount = logView.getLayout().getLineTop(logView.getLineCount()) - logView.getHeight();
                            // if there is no need to scroll, scrollAmount will be <=0
                            if (scrollAmount > 0)
                                logView.scrollTo(0, scrollAmount);
                            else
                                logView.scrollTo(0, 0);
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    switch (errorCode) {
                        case SCAN_FAILED_ALREADY_STARTED:
                            Log.e(TAG, "SCAN_FAILED_ALREADY_STARTED");
                            break;
                        case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                            Log.e(TAG, "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                            break;
                        case SCAN_FAILED_FEATURE_UNSUPPORTED:
                            Log.e(TAG, "SCAN_FAILED_FEATURE_UNSUPPORTED");
                            break;
                        case SCAN_FAILED_INTERNAL_ERROR:
                            Log.e(TAG, "SCAN_FAILED_INTERNAL_ERROR");
                            break;
                        default:
                            Log.e(TAG, "Scan failed, unknown error code");
                            break;
                    }
                }
            };
            scanner.startScan(scanFilters, SCAN_SETTINGS, scanCallback);
        }
    }

    public void Stop() {
        if (scanner != null) {
            scanner.stopScan(scanCallback);
        }
    }

    // Checks the frame type and hands off the service data to the validation module.
    private void validateServiceData(String deviceAddress, byte[] serviceData) {
        Beacon beacon = deviceToBeaconMap.get(deviceAddress);
        if (serviceData == null) {
            String err = "Null Eddystone service data";
            beacon.frameStatus.nullServiceData = err;
            logDeviceError(deviceAddress, err);
            return;
        }
        switch (serviceData[0]) {
            case Constants.UID_FRAME_TYPE:
                UidValidator.validate(deviceAddress, serviceData, beacon);
                break;
            case Constants.TLM_FRAME_TYPE:
                TlmValidator.validate(deviceAddress, serviceData, beacon);
                break;
            case Constants.URL_FRAME_TYPE:
                UrlValidator.validate(deviceAddress, serviceData, beacon);
                break;
            default:
                String err = String.format("Invalid frame type byte %02X", serviceData[0]);
                beacon.frameStatus.invalidFrameType = err;
                logDeviceError(deviceAddress, err);
                break;
        }
    }

    private void logDeviceError(String deviceAddress, String err) {
        Log.e(TAG, deviceAddress + ": " + err);
    }
}

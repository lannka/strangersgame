package com.example.dinghongfei.strangersgame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

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

    private BluetoothLeScanner scanner;

    private List<ScanFilter> scanFilters;
    private ScanCallback scanCallback;

    private List<String> instances_to_track;

    private Map<String /* device address */, Beacon> deviceToBeaconMap = new HashMap<>();

    public boolean Init(Context application_context, List<String> ids) {
        BluetoothManager manager = (BluetoothManager) application_context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = manager.getAdapter();
        instances_to_track = new ArrayList<>();
        for (String instance_id : ids) {
            instances_to_track.add(instance_id);
        }
        if (btAdapter != null && btAdapter.isEnabled()) {
            scanner = btAdapter.getBluetoothLeScanner();
            scanFilters = new ArrayList<>();
            scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());
            return true;
        }
        return false;
    }

    public void Start(final ScannerCallback callback) {
        if (scanner != null) {
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
                    Log.v(TAG, deviceAddress + " " + Utils.toHexString(serviceData));
                    validateServiceData(deviceAddress, serviceData);

                    beacon = deviceToBeaconMap.get(deviceAddress);

                    if (beacon.uidStatus != null && beacon.uidStatus.uidValue != null) {
                        String namespace = beacon.uidStatus.uidValue.substring(0, 20);
                        String instance_id = beacon.uidStatus.uidValue.substring(20, 32);
                        if (namespace.toLowerCase().equals(Constants.NAMESPACE.toLowerCase())) {
                            for (String iid : instances_to_track) {
                                if (instance_id.equals(instance_id.toLowerCase())) {
                                    ArrayList<String> iids = new ArrayList<>();
                                    iids.add(instance_id);
                                    callback.onDetected(iids);
                                }
                            }
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

    public void SetInstancesToTrack(ArrayList<String> instance_ids) {
        instances_to_track.clear();
        for (String id : instance_ids) {
            instances_to_track.add(id);
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

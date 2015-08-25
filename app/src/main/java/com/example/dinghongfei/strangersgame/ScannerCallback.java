package com.example.dinghongfei.strangersgame;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingkai on 8/24/15.
 */
public abstract class ScannerCallback {
    /**
     * Callback when there are some scanned instance ids.
     *
     * @param instance_ids List of instance ids that are previously scanned.
     */
    public void onDetected(ArrayList<String> instance_ids) {
    }
}
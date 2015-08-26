package com.example.dinghongfei.strangersgame;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingkai on 8/24/15.
 */
public abstract class ScannerCallback {
    /**
     * Callback when there are matched instance ID.
     */
    public void onDetected(String instance_id) {
    }
}
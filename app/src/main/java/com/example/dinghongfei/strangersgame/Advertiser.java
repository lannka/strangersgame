package com.example.dinghongfei.strangersgame;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class Advertiser {

  private static final String TAG = "EddystoneAdvertiser";
  private static final byte FRAME_TYPE_UID = 0x00;
  private static final ParcelUuid SERVICE_UUID =
      ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

  private BluetoothLeAdvertiser adv;

  public Advertiser(BluetoothLeAdvertiser adv) {
    this.adv = adv;
  }

  public void startAdvertising(String namespace, String instance, AdvertiseCallback advertiseCallback) {
    if (!isValidHex(namespace, 10)) {
      advertiseCallback.onStartFailure(10);
      return;
    }
    if (!isValidHex(instance.toUpperCase(), 6)) {
      advertiseCallback.onStartFailure(10);
      return;
    }
    Log.i("Start advertising: ", instance);
    AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
        .setConnectable(true)
        .build();

    byte[] serviceData = null;
    try {
      serviceData = buildServiceData(
          AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM, namespace, instance);
    } catch (IOException e) {
      Log.e(TAG, e.toString());
    }

    AdvertiseData advertiseData = new AdvertiseData.Builder()
        .addServiceData(SERVICE_UUID, serviceData)
        .addServiceUuid(SERVICE_UUID)
        .setIncludeTxPowerLevel(false)
        .setIncludeDeviceName(false)
        .build();

    adv.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
  }

  public void stopAdvertising(AdvertiseCallback advertiseCallback) {
    Log.i(TAG, "Stopping ADV");
    adv.stopAdvertising(advertiseCallback);
  }

  private byte[] buildServiceData(
      int txPowerLevel, String namespace, String instance) throws IOException {
    byte txPower = txPowerLevelToByteValue(txPowerLevel);
    byte[] namespaceBytes = toByteArray(namespace);
    byte[] instanceBytes = toByteArray(instance);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    os.write(new byte[]{FRAME_TYPE_UID, txPower});
    os.write(namespaceBytes);
    os.write(instanceBytes);
    return os.toByteArray();
  }

  // Converts the current Tx power level value to the byte value for that power
  // in dBm at 0 meters.
  //
  // Note that this will vary by device and the values are only roughly accurate.
  // The measurements were taken with a Nexus 6.
  private byte txPowerLevelToByteValue(int txPowerLevel) {
    switch (txPowerLevel) {
      case AdvertiseSettings.ADVERTISE_TX_POWER_HIGH:
        return (byte) -16;
      case AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM:
        return (byte) -26;
      case AdvertiseSettings.ADVERTISE_TX_POWER_LOW:
        return (byte) -35;
      default:
        return (byte) -59;
    }
  }

  private byte[] toByteArray(String hexString) {
    // hexString guaranteed valid.
    int len = hexString.length();
    byte[] bytes = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
          + Character.digit(hexString.charAt(i + 1), 16));
    }
    return bytes;
  }

  private boolean isValidHex(String s, int len) {
    return !(s == null || s.isEmpty()) && (s.length() / 2) == len && s.matches("[0-9A-F]+");
  }
}

package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

  private static final int REQUEST_ENABLE_BLUETOOTH = 1;

  private Scanner scanner;
  private Advertiser advertiser;
  private Button settings_button;
  private String self_id;
  private String self_base_id;
  private String opponent_id;
  private String opponent_base_id;
  private Vibrator vibrator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    init();
    settings_button = (Button)findViewById(R.id.settingsButton);
    settings_button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(i, 0);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
      if (resultCode == Activity.RESULT_OK) {
        init();
      } else {
        finish();
      }
    } else {
      if (resultCode == Activity.RESULT_OK) {
        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        String text = sharedPref.getString(getString(R.string.self_instance_id), "");
        self_id = Utils.toInstanceId(text);
        text = sharedPref.getString(getString(R.string.self_base_instance_id), "");
        self_base_id = Utils.toInstanceId(text);
        text = sharedPref.getString(getString(R.string.opponent_instance_id), "");
        opponent_id = Utils.toInstanceId(text);
        text = sharedPref.getString(getString(R.string.opponent_base_instance_id), "");
        opponent_base_id = Utils.toInstanceId(text);
        if (scanner != null) {
          ArrayList<String> instances_to_track = new ArrayList<>();
          instances_to_track.add(self_base_id);
          instances_to_track.add(opponent_id);
          instances_to_track.add(opponent_base_id);
          scanner.SetInstancesToTrack(instances_to_track);
        }
        if (advertiser != null) {
          advertiser.stopAdvertising(new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
              super.onStartSuccess(settingsInEffect);
              advertiser.startAdvertising(Constants.NAMESPACE, self_id, new AdvertiseCallback() {
                @Override
                public void onStartFailure(int errorCode) {
                  super.onStartFailure(errorCode);
                  showToast("startAdvertising failed with error " + errorCode);
                }
              });
            }

            @Override
            public void onStartFailure(int errorCode) {
              super.onStartFailure(errorCode);
            }
          });
        }
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    scanner.Start(new ScannerCallback() {
      @Override
      public void onDetected(String instance_id) {
        Log.d("MainActivity", instance_id);
        vibrator.vibrate(200);
      }
    });
  }

  @Override
  public void onPause() {
    super.onPause();
    scanner.Stop();
  }

  // Checks if Bluetooth advertising is supported on the device and requests enabling if necessary.
  private void init() {
    BluetoothManager manager = (BluetoothManager) getApplicationContext().getSystemService(
        Context.BLUETOOTH_SERVICE);
    BluetoothAdapter btAdapter = manager.getAdapter();
    scanner = new Scanner();
    if (btAdapter == null) {
      showFinishingAlertDialog("Bluetooth Error", "Bluetooth not detected on device");
    } else if (!btAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    } else if (!btAdapter.isMultipleAdvertisementSupported()) {
      showFinishingAlertDialog("Not supported", "BLE advertising not supported on this device");
    } else {
      scanner.Init(getApplicationContext(), new ArrayList<String>());
      advertiser = new Advertiser(btAdapter.getBluetoothLeAdvertiser());
      advertiser.startAdvertising(Constants.NAMESPACE, "112233445566", new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
          super.onStartFailure(errorCode);
          showToast("startAdvertising failed with error " + errorCode);
        }
      });
    }
  }

  // Pops an AlertDialog that quits the app on OK.
  private void showFinishingAlertDialog(String title, String message) {
    new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            finish();
          }
        }).show();
  }

  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }
}

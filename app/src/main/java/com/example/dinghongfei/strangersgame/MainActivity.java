package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

  private static final int REQUEST_ENABLE_BLUETOOTH = 1;
  private static final int START_NEW_GAME = 2;

  private Scanner scanner;
  private Advertiser advertiser;
  private Button newGameButton;
  private GameController gameController;
  private AdvertiseCallback advertiseCallback;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    gameController = new GameController(this);
    advertiseCallback = new AdvertiseCallback() {
      @Override
      public void onStartFailure(int errorCode) {
        super.onStartFailure(errorCode);
        Log.e("StartAdvertising", "failed " + errorCode);
        showToast("startAdvertising failed with error " + errorCode);
      }
    };
    init();
    newGameButton = (Button)findViewById(R.id.new_game_button);
    newGameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(MainActivity.this, NewGameActivity.class);
        startActivityForResult(i, START_NEW_GAME);
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
    switch (requestCode) {
      case REQUEST_ENABLE_BLUETOOTH:
        if (resultCode == Activity.RESULT_OK) {
          init();
        } else {
          finish();
        }
        break;
      case START_NEW_GAME:
        if (resultCode == Activity.RESULT_OK) {
          startGame();
        }
        break;
      default:
        break;
    }
  }

  // Checks if Bluetooth advertising is supported on the device and requests enabling if necessary.
  private void init() {
    BluetoothManager manager = (BluetoothManager) getApplicationContext().getSystemService(
        Context.BLUETOOTH_SERVICE);
    BluetoothAdapter btAdapter = manager.getAdapter();
    if (btAdapter == null) {
      showFinishingAlertDialog("Bluetooth Error", "Bluetooth not detected on device");
    } else if (!btAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    } else if (!btAdapter.isMultipleAdvertisementSupported()) {
      showFinishingAlertDialog("Not supported", "BLE advertising not supported on this device");
    } else {
      scanner = new Scanner(this, btAdapter.getBluetoothLeScanner());
      advertiser = new Advertiser(btAdapter.getBluetoothLeAdvertiser());
    }
  }

  private void startGame() {
    Set<String> enemyIds = new HashSet<>();
    enemyIds.add(readInstanceId(R.string.opponent_instance_id));
    gameController.start(
        readInstanceId(R.string.self_base_instance_id),
        readInstanceId(R.string.opponent_base_instance_id),
        enemyIds);
    if (advertiser != null) {
      advertiser.stopAdvertising(advertiseCallback);
      String self_id = readInstanceId(R.string.self_instance_id);
      advertiser.startAdvertising(Constants.NAMESPACE, self_id, advertiseCallback);
    }
    scanner.Stop();
    scanner.Start(new ScannerCallback() {
      @Override
      public void onDetected(String instance_id) {
        gameController.interrupt(instance_id);
      }
    });
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

  private String readInstanceId(int stringId) {
    SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
    return Utils.toInstanceId(sharedPref.getString(getString(stringId), ""));
  }
}

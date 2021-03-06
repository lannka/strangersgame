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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

  private static final int REQUEST_ENABLE_BLUETOOTH = 1;
  private static final int START_NEW_GAME = 2;

  private Scanner scanner;
  private Advertiser advertiser;
  private TextView titleTextView;
  private Button newGameButton;
  private Button helpButton;
  private Button quitGameButton;

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
    Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/PoiretOne-Regular.ttf");
    titleTextView = (TextView) findViewById(R.id.title_text_view);
    titleTextView.setTypeface(typeface);
    newGameButton = (Button)findViewById(R.id.new_game_button);
    newGameButton.setTypeface(typeface);
    newGameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(MainActivity.this, NewGameActivity.class);
        startActivityForResult(i, START_NEW_GAME);
      }
    });
    helpButton = (Button)findViewById(R.id.help_button);
    helpButton.setTypeface(typeface);
    helpButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String url = "http://yoopu.me/blog/strangers-game-rule/?hideNavBar";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
      }
    });
    quitGameButton = (Button)findViewById(R.id.quit_button);
    quitGameButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PoiretOne-Regular.ttf"));
    quitGameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        quitGame();
      }
    });
    quitGameButton.setVisibility(View.GONE);
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
    titleTextView.setVisibility(View.GONE);
    newGameButton.setVisibility(View.GONE);
    helpButton.setVisibility(View.GONE);
    quitGameButton.setVisibility(View.VISIBLE);
  }

  private void quitGame() {
    advertiser.stopAdvertising(advertiseCallback);
    scanner.Stop();
    gameController.stop();
    titleTextView.setVisibility(View.VISIBLE);
    newGameButton.setVisibility(View.VISIBLE);
    helpButton.setVisibility(View.VISIBLE);
    quitGameButton.setVisibility(View.GONE);
    TextView logView = (TextView) (findViewById(R.id.logView));
    logView.setText("");
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

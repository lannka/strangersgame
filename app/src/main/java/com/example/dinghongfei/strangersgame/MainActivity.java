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
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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
  private ProgressBar progressBar;
  private ProgressBar progressBarBackground;
  private int progress;
  private Handler handler;
  private boolean prev_found_self_base = false;
  private TextView found_enemy_text;
  private TextView found_enemy_base_text;
  private TextView count_down_timer;

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
    found_enemy_text = (TextView)findViewById(R.id.found_enemy_label);
    found_enemy_base_text = (TextView)findViewById(R.id.found_enemy_base_label);
    progressBar = (ProgressBar)findViewById(R.id.circle_progress_bar);
    progress = 0;
    progressBar.setProgress(0);
    progressBar.setMax(100);
    progressBar.setVisibility(View.GONE);
    progressBarBackground = (ProgressBar)findViewById(R.id.circle_progress_bar_background);
    progressBarBackground.setVisibility(View.GONE);
    count_down_timer = (TextView)findViewById(R.id.timer);
    count_down_timer.setText("");
    handler = new Handler();
    new Thread(new Runnable() {
      public void run() {
        while (true) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          handler.post(new Runnable() {
            public void run() {
              if (!gameController.game_started) {
                return;
              }
              if (gameController.found_self_base && !prev_found_self_base) {
                count_down_timer.setText("");
                progressBar.setVisibility(View.VISIBLE);
                progressBarBackground.setVisibility(View.VISIBLE);
                progress = 0;
                progressBar.setProgress(0);
                prev_found_self_base = true;
              } else if (gameController.found_self_base && prev_found_self_base) {
                count_down_timer.setText("");
                if (progress < 100) {
                  progress += 1;
                  progressBar.setProgress(progress);
                } else {
                  gameController.ResetLife();
                }
              } else if (!gameController.found_self_base) {
                if (prev_found_self_base) {
                  prev_found_self_base = false;
                  progressBar.setVisibility(View.GONE);
                  progressBarBackground.setVisibility(View.GONE);
                  progressBar.setProgress(0);
                  progress = 0;
                }
                if (gameController.life < 0) {
                  count_down_timer.setText("Game Over! You Lose!");
                } else {
                  int sec = gameController.life % 60;
                  int min = gameController.life / 60;
                  count_down_timer.setText(Integer.toString(min) + ":" + Integer.toString(sec));
                }
              }

              if (gameController.found_enemy) {
                found_enemy_text.setText("Enemy Around!!!");
              } else {
                found_enemy_text.setText("");
              }
              if (gameController.found_enemy_base) {
                found_enemy_base_text.setText("Found Enemy Base!!!");
              } else {
                found_enemy_base_text.setText("");
              }
            }
          });
        }
      }
    }).start();
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

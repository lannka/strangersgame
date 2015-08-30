package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;
import java.util.Set;

public class GameController {

  private static final int RESPONSE_INTERVAL_IN_MS = 500;

  private final Activity context;
  private String myBaseId;
  private String enemyBaseId;
  private Set<String> enemyIds;
  private Vibrator vibrator;
  private boolean found_enemy = false;
  private boolean found_self_base = false;
  private boolean found_enemy_base = false;
  private boolean game_started = false;
  private LifeCharger lifeCharger;
  private LifeTimer lifeTimer;
  private TextView found_enemy_text;
  private TextView found_enemy_base_text;

  private Thread timerThread;

  public GameController(Activity context) {
    this.context = context;
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    lifeCharger = new LifeCharger(context);
    lifeTimer = new LifeTimer(context);
    lifeTimer.setVisible(false);
  }

  public void start(String myBaseId, String enemyBaseId, Set<String> enemyIds) {
    this.myBaseId = myBaseId;
    this.enemyBaseId = enemyBaseId;
    this.enemyIds = enemyIds;
    game_started = true;
    Log.i("Start game", myBaseId + ":" + enemyBaseId + ":" + enemyIds);

    found_enemy_text = (TextView) (context.findViewById(R.id.found_enemy_label));
    found_enemy_base_text = (TextView) (context.findViewById(R.id.found_enemy_base_label));
    lifeTimer.reset();
    lifeTimer.setVisible(true);
    final Handler handler = new Handler();

    if (timerThread != null) {
      timerThread.interrupt();
      timerThread = null;
    }
    timerThread = new Thread(new Runnable() {
      public void run() {
        while (!Thread.currentThread().isInterrupted()) {
          try {
            Thread.sleep(RESPONSE_INTERVAL_IN_MS);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          handler.post(new Runnable() {
            public void run() {
              if (!game_started) {
                return;
              }
              if (found_self_base) {
                found_self_base = false;
                if (lifeCharger.charge(RESPONSE_INTERVAL_IN_MS)) {
                  lifeTimer.reset();
                }
              } else {
                lifeCharger.stop();
                lifeTimer.countDown(RESPONSE_INTERVAL_IN_MS);
              }

              if (found_enemy) {
                found_enemy_text.setText("Enemy Around!!!");
                found_enemy = false;
              } else {
                found_enemy_text.setText("");
              }
              if (found_enemy_base) {
                found_enemy_base_text.setText("Found Enemy Base!!!");
                found_enemy_base = false;
              } else {
                found_enemy_base_text.setText("");
              }
            }
          });
        }
      }
    });
    timerThread.start();
  }

  public void interrupt(String instanceId) {
    Date date = new Date();
    if (instanceId.equals(myBaseId)) {
      found_self_base = true;
      Log.i("Detected", "My base in range: " + myBaseId + "at time: " + date.getTime());
    } else if (instanceId.equals(enemyBaseId)) {
      found_enemy_base = true;
      Log.i("Detected", "Enemy base in range: " + enemyBaseId + "at time: " + date.getTime());
    } else if (enemyIds.contains(instanceId)) {
      found_enemy = true;
      Log.i("Detected", "Enemy in range: " + instanceId + "at time: " + date.getTime());
      vibrator.vibrate(200);
    }
  }
}

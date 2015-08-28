package com.example.dinghongfei.strangersgame;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import java.util.Date;
import java.util.Set;

public class GameController {

  private final Context context;
  private String myBaseId;
  private String enemyBaseId;
  private Set<String> enemyIds;
  private Vibrator vibrator;
  private long last_found_enemy_time = -1;
  private long last_found_self_base_time = -1;
  private long last_found_enemy_base_time = -1;
  public boolean found_enemy = false;
  public boolean found_self_base = false;
  public boolean found_enemy_base = false;
  public int life = 300;  // 10 HP = 1 second

  public GameController(Context context) {
    this.context = context;
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    new Thread(new Runnable() {
      public void run() {
          while (true) {
              Date date = new Date();
              long current_time = date.getTime();
              found_enemy = (current_time - last_found_enemy_time) < 1000;
              found_enemy_base = (current_time - last_found_enemy_base_time) < 1000;
              found_self_base = (current_time - last_found_self_base_time) < 1000;

              if (!found_self_base) {
                  life -= 1;
              }

              try {
                  Thread.sleep(1000);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              Log.i("GameStatus",
                      "found_enemy: " + found_enemy + "  found_enemy_base: " + found_enemy_base +
                              "  found_self_base: " + found_self_base);
          }
      }
    }).start();
  }

  public void start(String myBaseId, String enemyBaseId, Set<String> enemyIds) {
    this.myBaseId = myBaseId;
    this.enemyBaseId = enemyBaseId;
    this.enemyIds = enemyIds;
    Log.i("Start game", myBaseId + ":" + enemyBaseId + ":" + enemyIds);
  }

  public void interrupt(String instanceId) {
    Date date = new Date();
    if (instanceId.equals(myBaseId)) {
      last_found_self_base_time = date.getTime();
      Log.i("Detected", "My base in range: " + myBaseId + "at time: " + date.getTime());
    } else if (instanceId.equals(enemyBaseId)) {
      last_found_enemy_base_time = date.getTime();
      Log.i("Detected", "Enemy base in range: " + enemyBaseId + "at time: " + date.getTime());
    } else if (enemyIds.contains(instanceId)) {
      last_found_enemy_time = date.getTime();
      Log.i("Detected", "Enemy in range: " + instanceId + "at time: " + date.getTime());
      vibrator.vibrate(200);
    }
  }

  public void ResetLife() {
      life = 300;
  }
}

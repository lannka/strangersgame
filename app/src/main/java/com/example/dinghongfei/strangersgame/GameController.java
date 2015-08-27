package com.example.dinghongfei.strangersgame;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import java.util.Set;

public class GameController {

  private final Context context;
  private String myBaseId;
  private String enemyBaseId;
  private Set<String> enemyIds;
  private Vibrator vibrator;

  public GameController(Context context) {
    this.context = context;
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
  }

  public void start(String myBaseId, String enemyBaseId, Set<String> enemyIds) {
    this.myBaseId = myBaseId;
    this.enemyBaseId = enemyBaseId;
    this.enemyIds = enemyIds;
    Log.i("Start game", myBaseId + ":" + enemyBaseId + ":" + enemyIds);
  }

  public void interrupt(String instanceId) {
    if (instanceId.equals(myBaseId)) {
      Log.i("Detected", "My base in range: " + myBaseId);
    } else if (instanceId.equals(enemyBaseId)) {
      Log.i("Detected", "Enemy base in range: " + enemyBaseId);
    } else if (enemyIds.contains(instanceId)) {
      Log.i("Detected", "Enemy in range: " + instanceId);
      vibrator.vibrate(200);
    }
  }
}

package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;
import java.util.Set;

public class GameController {

  private static final int RESPONSE_INTERVAL_IN_MS = 500;
  private static final int FULL_LIFE_IN_SEC = 300;

  private final Activity context;
  private String myBaseId;
  private String enemyBaseId;
  private Set<String> enemyIds;
  private Vibrator vibrator;
  private boolean found_enemy = false;
  private boolean found_self_base = false;
  private boolean found_enemy_base = false;
  private int life;
  private boolean game_started = false;

  private ProgressBar progressBar;
  private ProgressBar progressBarBackground;
  private int progress;
  private Handler handler;
  private boolean prev_found_self_base = false;
  private TextView found_enemy_text;
  private TextView found_enemy_base_text;
  private TextView count_down_timer;

  private Thread timerThread;

  public GameController(Activity context) {
    this.context = context;
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
  }

  public void start(String myBaseId, String enemyBaseId, Set<String> enemyIds) {
    this.myBaseId = myBaseId;
    this.enemyBaseId = enemyBaseId;
    this.enemyIds = enemyIds;
    game_started = true;
    Log.i("Start game", myBaseId + ":" + enemyBaseId + ":" + enemyIds);

    found_enemy_text = (TextView) (context.findViewById(R.id.found_enemy_label));
    found_enemy_base_text = (TextView) (context.findViewById(R.id.found_enemy_base_label));
    progressBar = (ProgressBar) (context.findViewById(R.id.circle_progress_bar));
    life = FULL_LIFE_IN_SEC;
    progress = 0;
    progressBar.setProgress(0);
    progressBar.setMax(100);
    progressBar.setVisibility(View.GONE);
    progressBarBackground = (ProgressBar) (context.findViewById(R.id.circle_progress_bar_background));
    progressBarBackground.setVisibility(View.GONE);
    count_down_timer = (TextView) (context.findViewById(R.id.timer));
    count_down_timer.setText("");
    handler = new Handler();

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
                if (!prev_found_self_base) {
                  count_down_timer.setText("");
                  progressBar.setVisibility(View.VISIBLE);
                  progressBarBackground.setVisibility(View.VISIBLE);
                  progress = 0;
                  progressBar.setProgress(0);
                  prev_found_self_base = true;
                  found_self_base = false;
                } else {
                  count_down_timer.setText("");
                  found_self_base = false;
                  if (progress < 100) {
                    progress += RESPONSE_INTERVAL_IN_MS / 100;
                    progressBar.setProgress(progress);
                  } else {
                    life = FULL_LIFE_IN_SEC;
                  }
                }
              } else {
                if (prev_found_self_base) {
                  prev_found_self_base = false;
                  progressBar.setVisibility(View.GONE);
                  progressBarBackground.setVisibility(View.GONE);
                  progressBar.setProgress(0);
                  progress = 0;
                }
                if (life < 0) {
                  count_down_timer.setText("Game Over! You Lose!");
                } else {
                  int sec = life % 60;
                  int min = life / 60;
                  count_down_timer.setText(Integer.toString(min) + ":" + Integer.toString(sec));
                }
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

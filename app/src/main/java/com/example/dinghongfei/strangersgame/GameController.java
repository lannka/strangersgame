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

  private final Activity context;
  private String myBaseId;
  private String enemyBaseId;
  private Set<String> enemyIds;
  private Vibrator vibrator;
  private long last_found_enemy_time;
  private long last_found_self_base_time;
  private long last_found_enemy_base_time;
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
    new Thread(new Runnable() {
      public void run() {
          while (true) {
              Date date = new Date();
              long current_time = date.getTime();
              found_enemy = (current_time - last_found_enemy_time) < 1000;
              found_enemy_base = (current_time - last_found_enemy_base_time) < 1000;
              found_self_base = (current_time - last_found_self_base_time) < 1000;

              if (!found_self_base && game_started) {
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
    game_started = true;
    Log.i("Start game", myBaseId + ":" + enemyBaseId + ":" + enemyIds);

    found_enemy_text = (TextView) (context.findViewById(R.id.found_enemy_label));
    found_enemy_base_text = (TextView) (context.findViewById(R.id.found_enemy_base_label));
    progressBar = (ProgressBar) (context.findViewById(R.id.circle_progress_bar));
    ResetLife();
    last_found_enemy_time = -1;
    last_found_self_base_time = -1;
    last_found_enemy_time = -1;
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
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          handler.post(new Runnable() {
            public void run() {
              if (!game_started) {
                return;
              }
              if (found_self_base && !prev_found_self_base) {
                count_down_timer.setText("");
                progressBar.setVisibility(View.VISIBLE);
                progressBarBackground.setVisibility(View.VISIBLE);
                progress = 0;
                progressBar.setProgress(0);
                prev_found_self_base = true;
              } else if (found_self_base && prev_found_self_base) {
                count_down_timer.setText("");
                if (progress < 100) {
                  progress += 1;
                  progressBar.setProgress(progress);
                } else {
                  ResetLife();
                }
              } else if (!found_self_base) {
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
              } else {
                found_enemy_text.setText("");
              }
              if (found_enemy_base) {
                found_enemy_base_text.setText("Found Enemy Base!!!");
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

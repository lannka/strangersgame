package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
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
  private volatile boolean game_started = false;
  private volatile boolean isGameOver;
  private LifeCharger lifeCharger;
  private LifeTimer lifeTimer;
  private ImageView imageView;
  private TextView messageLabel;

  private Thread timerThread;

  public GameController(Activity context) {
    this.context = context;
    imageView = (ImageView) (context.findViewById(R.id.main_image_view));
    messageLabel = (TextView) (context.findViewById(R.id.message_label));
    messageLabel.setTypeface(
        Typeface.createFromAsset(context.getAssets(), "fonts/PoiretOne-Regular.ttf"));
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    lifeCharger = new LifeCharger(context);
    lifeTimer = new LifeTimer(context);
    lifeTimer.setVisible(false);
  }

  public void start(String myBaseId, String enemyBaseId, Set<String> enemyIds) {
    if (game_started) throw new RuntimeException("Game already started!");
    this.myBaseId = myBaseId;
    this.enemyBaseId = enemyBaseId;
    this.enemyIds = enemyIds;
    game_started = true;
    isGameOver = false;
    Log.i("Start game", myBaseId + ":" + enemyBaseId + ":" + enemyIds);

    lifeTimer.reset();
    lifeTimer.setVisible(true);
    final Handler handler = new Handler();

    if (timerThread == null) {
      timerThread = new Thread(new Runnable() {
        public void run() {
          while (true) {
            if (!game_started) {
              continue;
            }
            try {
              Thread.sleep(RESPONSE_INTERVAL_IN_MS);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            handler.post(new Runnable() {
              public void run() {
                if (!game_started || isGameOver) {
                  return;
                }

                if (found_enemy_base) {
                  imageView.setImageResource(R.drawable.base);
                } else if (found_enemy) {
                  imageView.setImageResource(R.drawable.enemy);
                } else {
                  imageView.setImageResource(R.drawable.detective);
                }

                if (!found_self_base && !found_enemy && !found_enemy_base) {
                  messageLabel.setText("");
                }

                if (found_self_base) {
                  found_self_base = false;
                  if (lifeCharger.charge(RESPONSE_INTERVAL_IN_MS)) {
                    lifeTimer.reset();
                  }
                  messageLabel.setText("Recovering...");
                } else {
                  lifeCharger.stop();
                  if (lifeTimer.countDown(RESPONSE_INTERVAL_IN_MS)) {
                    imageView.setImageResource(R.drawable.gameover);
                    messageLabel.setText("");
                    isGameOver = true;
                  }
                }

                if (found_enemy) {
                  messageLabel.setText("Enemy Around!!!");
                  found_enemy = false;
                }
                if (found_enemy_base) {
                  messageLabel.setText("Found Enemy Base!!!");
                  found_enemy_base = false;
                }
              }
            });
          }
        }
      });
      timerThread.start();
    }
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
//      vibrator.vibrate(200);
    }
  }

  public void stop() {
    game_started = false;
    lifeTimer.setVisible(false);
    lifeCharger.stop();
    imageView.setImageResource(R.drawable.detective);
    messageLabel.setText("");
  }
}

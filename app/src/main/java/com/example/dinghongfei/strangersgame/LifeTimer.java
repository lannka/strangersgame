package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

/**
 * Created by dinghongfei on 8/29/15.
 */
public class LifeTimer {

  private static final int FULL_LIFE_IN_MS = 300 * 1000;
  private TextView countDownTimerTextView;
  private int lifeInMs;

  public LifeTimer(Activity context) {
    countDownTimerTextView = (TextView) (context.findViewById(R.id.timer));
    countDownTimerTextView.setTypeface(
        Typeface.createFromAsset(context.getAssets(), "fonts/PoiretOne-Regular.ttf"));
    reset();
  }

  public void setVisible(boolean visible) {
    countDownTimerTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  /**
   * @return true if game over.
   */
  public boolean countDown(int timeInMs) {
    lifeInMs -= timeInMs;
    if (lifeInMs <= 0) {
      countDownTimerTextView.setText("Game Over! You Lose!");
      return true;
    } else {
      print();
      return false;
    }
  }

  public void reset() {
    lifeInMs = FULL_LIFE_IN_MS;
    print();
  }

  private void print() {
    int sec = lifeInMs / 1000 % 60;
    int min = lifeInMs / 1000 / 60;
    countDownTimerTextView.setText(Integer.toString(min) + ":" + String.format("%02d", sec));
  }
}

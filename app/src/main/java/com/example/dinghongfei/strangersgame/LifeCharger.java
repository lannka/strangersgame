package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by dinghongfei on 8/29/15.
 */
public class LifeCharger {

  private static final int CHARGE_DURATION_IN_MS = 5 * 1000;
  private static final int PROGRESS_MAX = 100;

  private ProgressBar progressBar;
  private ProgressBar progressBarBackground;
  private int progress;

  public LifeCharger(Activity context) {
    progressBar = (ProgressBar) (context.findViewById(R.id.circle_progress_bar));
    progressBarBackground = (ProgressBar) (context.findViewById(R.id.circle_progress_bar_background));
    progress = 0;
    progressBar.setProgress(0);
    progressBar.setMax(PROGRESS_MAX);
    progressBar.setVisibility(View.GONE);
    progressBarBackground.setVisibility(View.GONE);
  }

  /**
   * @return true if charging is completed.
   */
  public boolean charge(int milliSec) {
    progress += milliSec / (CHARGE_DURATION_IN_MS / PROGRESS_MAX);
    progressBar.setVisibility(View.VISIBLE);
    progressBarBackground.setVisibility(View.VISIBLE);
    progressBar.setProgress(progress);
    return progress >= PROGRESS_MAX;
  }

  public void stop() {
    progress = 0;
    progressBar.setVisibility(View.GONE);
    progressBarBackground.setVisibility(View.GONE);
  }
}

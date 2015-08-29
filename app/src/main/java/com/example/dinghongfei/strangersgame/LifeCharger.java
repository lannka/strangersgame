package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by dinghongfei on 8/29/15.
 */
public class LifeCharger {
  private ProgressBar progressBar;
  private ProgressBar progressBarBackground;
  private int progress;

  public LifeCharger(Activity context) {
    progressBar = (ProgressBar) (context.findViewById(R.id.circle_progress_bar));
    progressBarBackground = (ProgressBar) (context.findViewById(R.id.circle_progress_bar_background));
    progress = 0;
    progressBar.setProgress(0);
    progressBar.setMax(100);
    progressBar.setVisibility(View.GONE);
    progressBarBackground.setVisibility(View.GONE);
  }

  public boolean charge(int milliSec) {
    progress += milliSec / 100;
    progressBar.setVisibility(View.VISIBLE);
    progressBarBackground.setVisibility(View.VISIBLE);
    progressBar.setProgress(progress);
    if (progress >= 100) {
      progress = 0;
      return true;
    }
    return false;
  }

  public void stop() {
    progress = 0;
    progressBar.setVisibility(View.GONE);
    progressBarBackground.setVisibility(View.GONE);
  }
}

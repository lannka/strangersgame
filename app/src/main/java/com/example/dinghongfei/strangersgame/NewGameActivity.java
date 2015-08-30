package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class NewGameActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_new_game);
    Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/PoiretOne-Regular.ttf");
    TextView titleTextView = (TextView) findViewById(R.id.new_game_title);
    titleTextView.setTypeface(typeface);
    final RadioButton radioTeamX = (RadioButton)findViewById(R.id.radio_team_x);
    Button startButton = (Button)findViewById(R.id.finishButton);
    startButton.setTypeface(typeface);

    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.self_instance_id), radioTeamX.isChecked() ? "Tom" : "Jerry");
        editor.putString(getString(R.string.self_base_instance_id), radioTeamX.isChecked() ? "X" : "Y");
        editor.putString(getString(R.string.opponent_instance_id), radioTeamX.isChecked() ? "Jerry" : "Tom");
        editor.putString(getString(R.string.opponent_base_instance_id), radioTeamX.isChecked() ? "Y" : "X");
        editor.commit();

        setResult(RESULT_OK);
        finish();
      }
    });
  }
}

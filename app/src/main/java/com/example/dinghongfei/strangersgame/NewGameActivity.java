package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class NewGameActivity extends Activity {

  private boolean leftSelected = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_game);
    Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/PoiretOne-Regular.ttf");
    TextView titleTextView = (TextView) findViewById(R.id.new_game_title);
    titleTextView.setTypeface(typeface);

    final ImageView leftImage = (ImageView)findViewById(R.id.select_left_image_view);
    final ImageView rightImage = (ImageView)findViewById(R.id.select_right_image_view);
    leftImage.setPadding(1, 1, 1, 1);
    rightImage.setPadding(1, 1, 1, 1);

    leftSelected = true;
    leftImage.setBackgroundColor(getResources().getColor(R.color.red));
    rightImage.setBackgroundColor(getResources().getColor(R.color.transparent));

    leftImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        leftSelected = true;
        leftImage.setBackgroundColor(getResources().getColor(R.color.red));
        rightImage.setBackgroundColor(getResources().getColor(R.color.transparent));
      }
    });
    rightImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        leftSelected = false;
        leftImage.setBackgroundColor(getResources().getColor(R.color.transparent));
        rightImage.setBackgroundColor(getResources().getColor(R.color.blue));
      }
    });

    Button startButton = (Button)findViewById(R.id.finishButton);
    startButton.setTypeface(typeface);

    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.self_instance_id), leftSelected ? "Tom" : "Jerry");
        editor.putString(getString(R.string.self_base_instance_id), leftSelected ? "X" : "Y");
        editor.putString(getString(R.string.opponent_instance_id), leftSelected ? "Jerry" : "Tom");
        editor.putString(getString(R.string.opponent_base_instance_id), leftSelected ? "Y" : "X");
        editor.commit();

        setResult(RESULT_OK);
        finish();
      }
    });
  }
}

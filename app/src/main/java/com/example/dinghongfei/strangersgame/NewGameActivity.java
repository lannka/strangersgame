package com.example.dinghongfei.strangersgame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewGameActivity extends Activity {

    private Button finish_button;
    private EditText self_instance_id_text;
    private EditText self_base_instance_id_text;
    private EditText opponent_instance_id_text;
    private EditText opponent_base_instance_id_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        finish_button = (Button)findViewById(R.id.finishButton);
        self_instance_id_text = (EditText)findViewById(R.id.playerOneText);
        self_base_instance_id_text = (EditText)findViewById(R.id.baseOneText);
        opponent_instance_id_text = (EditText)findViewById(R.id.playTwoText);
        opponent_base_instance_id_text = (EditText)findViewById(R.id.baseTwoText);

        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        String text = sharedPref.getString(getString(R.string.self_instance_id), "");
        self_instance_id_text.setText(text);
        text = sharedPref.getString(getString(R.string.self_base_instance_id), "");
        self_base_instance_id_text.setText(text);
        text = sharedPref.getString(getString(R.string.opponent_instance_id), "");
        opponent_instance_id_text.setText(text);
        text = sharedPref.getString(getString(R.string.opponent_base_instance_id), "");
        opponent_base_instance_id_text.setText(text);

        if (self_instance_id_text.getText().length() == 0) {
          self_instance_id_text.setText("Tom");
        }
        if (self_base_instance_id_text.getText().length() == 0) {
          self_base_instance_id_text.setText("X");
        }
        if (opponent_instance_id_text.getText().length() == 0) {
          opponent_instance_id_text.setText("Jerry");
        }
        if (opponent_base_instance_id_text.getText().length() == 0) {
          opponent_base_instance_id_text.setText("Y");
        }
        finish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString(getString(R.string.self_instance_id), self_instance_id_text.getText().toString());
                editor.putString(getString(R.string.self_base_instance_id), self_base_instance_id_text.getText().toString());
                editor.putString(getString(R.string.opponent_instance_id), opponent_instance_id_text.getText().toString());
                editor.putString(getString(R.string.opponent_base_instance_id), opponent_base_instance_id_text.getText().toString());
                editor.commit();

                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

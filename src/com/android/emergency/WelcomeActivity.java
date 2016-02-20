package com.android.emergency;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

/**
 * First screen the user sees the first time they open the app.
 */
public class WelcomeActivity extends Activity {
    private static final String KEY_SKIP_WELCOME_ACTIVITY = "skip_welcome_activity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(KEY_SKIP_WELCOME_ACTIVITY, false)) {
            launchNextActivity();
            return;
        }
        
        setContentView(R.layout.welcome_layout);

        Button getStartedButton = (Button) findViewById(R.id.get_started_button);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putBoolean(KEY_SKIP_WELCOME_ACTIVITY, true).commit();
                launchNextActivity();
            }
        });
    }

    private void launchNextActivity() {
        Intent intent = new Intent(this, ViewInfoActivity.class);
        startActivity(intent);
        finish();
    }
}

package com.simonits.adalerts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.simonits.adalerts.R;
import com.simonits.adalerts.utils.PreferenceUtils;

public class RoutingActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        // Keep the built-in splash screen visible during this Activity
        splashScreen.setKeepOnScreenCondition(() -> true );

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Fetch and activate succeeded");
                        FirebaseCrashlytics.getInstance().log("Fetch and activate succeeded");
                    } else {
                        Log.i(TAG, "Fetch failed");
                        FirebaseCrashlytics.getInstance().log("Fetch failed");
                    }
                    if (PreferenceUtils.getAppVersionCode(RoutingActivity.this) == 0) {
                        startActivity(new Intent(RoutingActivity.this, WelcomeActivity.class));
                        overridePendingTransition(R.anim.scroll_in_right, R.anim.scroll_out_left);
                    } else {
                        startActivity(new Intent(RoutingActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
                    }
                    finish();
                });
    }
}

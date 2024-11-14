package com.simonits.adalerts.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import com.simonits.adalerts.R;
import com.simonits.adalerts.dialogs.ConfirmDialog;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;

public class WelcomeActivity extends AppCompatActivity implements ConfirmDialog.FrequencyDialogListener {

    private static final String TAG = "WelcomeActivity";
    private CheckBox wifiCheckBox;
    private TextView frequencyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        wifiCheckBox = findViewById(R.id.welcome_wifi_checkbox);
        wifiCheckBox.setChecked(true);

        setupFrequencyPicker();
        confirmButtonSetup();
    }

    private void setupFrequencyPicker() {
        frequencyText = findViewById(R.id.welcome_frequency);
        frequencyText.setText(PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_WIFIFREQUENCY, this));
        frequencyText.setOnClickListener(view -> {
            ConfirmDialog dialog = new ConfirmDialog(WelcomeActivity.this, null, "Frequency", Constants.KEY_SETTING_WIFIFREQUENCY, Constants.DIALOGTYPE_FREQUENCY + "Activity");
            dialog.show(WelcomeActivity.this.getSupportFragmentManager(), "confirm dialog");
        });
    }

    @Override
    public void onConfirmClicked() {
        frequencyText.setText(PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_WIFIFREQUENCY, this));
    }

    private void confirmButtonSetup() {
        findViewById(R.id.welcome_button).setOnClickListener(view -> {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PreferenceUtils.setAppVersionCode(pInfo.getLongVersionCode(), WelcomeActivity.this);
                } else {
                    PreferenceUtils.setAppVersionCode(pInfo.versionCode, WelcomeActivity.this);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.toString());
            }

            String wifiFrequency = PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_WIFIFREQUENCY, WelcomeActivity.this);
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_WIFIFREQUENCY, wifiFrequency, WelcomeActivity.this);
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_CHARGEFREQUENCY, wifiFrequency, WelcomeActivity.this);
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_BATTERYFREQUENCY, wifiFrequency, WelcomeActivity.this);
            if (!wifiCheckBox.isChecked()) {
                PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_MOBILEFREQUENCY, wifiFrequency, WelcomeActivity.this);
            }

            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
            finish();
        });
    }
}

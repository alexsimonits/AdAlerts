package com.simonits.adalerts.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.simonits.adalerts.R;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.services.ForegroundService;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.util.ArrayList;

public class OnUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "OnUpdateReceiver";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnUpdateReceiver Received");
        long previousAppVersionCode = PreferenceUtils.getAppVersionCode(context);

        if (previousAppVersionCode == 0 && !PreferenceUtils.getAppVersion(context).equals("0.00")) {
            previousAppVersionCode = getConvertedAppVersionCode(context);
        }

        codeUpdates(previousAppVersionCode, context);

        if (previousAppVersionCode != 0) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PreferenceUtils.setAppVersionCode(pInfo.getLongVersionCode(), context);
                } else {
                    PreferenceUtils.setAppVersionCode(pInfo.versionCode, context);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.toString());
            }
        }

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate();

        if (PreferenceUtils.optionGetDataFetch(context) && !Utility.isServiceRunningInForeground(context, ForegroundService.class)) {
            Utility.setCheckAlarm(context.getApplicationContext(), false);
        }
        if (!Utility.doesWorkExist(context)) {
            context.sendBroadcast(new Intent("any string").setClass(context, WorkerReceiver.class));
        }
    }

    private void codeUpdates(long previousAppVersionCode, Context context) {
        ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
        int id = 1;
        for (Alert alert : alertList) {
            if (previousAppVersionCode < 76) {
                alert.setIDsHistory();
            }
            if (previousAppVersionCode < 78) {
                alert.setId(id);
                id++;
            }
            if (previousAppVersionCode < 93) {
                alert.setFURL();
            }
            if (previousAppVersionCode < 106) {
                if (alert.hasFacebook()) {
                    alert.removeBrokenFacebookListings();
                }
            }
            if (previousAppVersionCode < 108) {
                alert.setIncludeAny();
            }
            if (previousAppVersionCode < 122) {
                alert.formatKeywords();
                alert.adjustFURL();
                alert.fixNulls();
            }
            if (previousAppVersionCode < 128) {
                alert.fixKURL();
            }
            if (previousAppVersionCode < 130) {
                alert.addKLatLong();
            }
            if (previousAppVersionCode < 133) {
                alert.fixKURL_nextPage();
            }
            if (previousAppVersionCode < 134) {
                alert.adjustFURLAgain();
            }
        }
        PreferenceUtils.saveAlertList(alertList, context);
        if (previousAppVersionCode < 90) {
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_WIFIFREQUENCY, PreferenceUtils.optionGetInterval(context), context);
            String mobileFrequency;
            if (PreferenceUtils.optionGetWifi(context)) {
                mobileFrequency = Constants.FREQUENCY_NEVER;
            } else {
                mobileFrequency = PreferenceUtils.optionGetInterval(context);
            }
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_MOBILEFREQUENCY, mobileFrequency, context);
            String dndFrequency;
            if (PreferenceUtils.optionGetDnd(context)) {
                dndFrequency = Constants.FREQUENCY_NEVER;
            } else {
                dndFrequency = PreferenceUtils.optionGetInterval(context);
            }
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_DNDFREQUENCY, dndFrequency, context);
        }
        if (previousAppVersionCode < 99) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            boolean dataFetch = prefs.getBoolean("dataFetch", true);
            editor.putBoolean(context.getResources().getString(R.string.settings_data_fetch), dataFetch);
            boolean notificationTiming = prefs.getBoolean("notifyAsAlertsAreChecked", false);
            if (notificationTiming) {
                editor.putString(context.getResources().getString(R.string.settings_notification_timing), "1");
            } else {
                editor.putString(context.getResources().getString(R.string.settings_notification_timing), "0");
            }
            editor.apply();
            String wifiFrequency = PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_WIFIFREQUENCY, context);
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_CHARGEFREQUENCY, wifiFrequency, context);
            PreferenceUtils.optionSaveFrequency(Constants.KEY_SETTING_BATTERYFREQUENCY, wifiFrequency, context);
        }
        if (previousAppVersionCode < 103) {
            String currentFrequency = PreferenceUtils.optionGetFrequency(Utility.getFrequencyKey(context), context);
            if (!currentFrequency.equals(Constants.FREQUENCY_NEVER)) {
                long previousFetchTime = PreferenceUtils.getTargetTime(context) - Utility.getMilliseconds(currentFrequency);
                PreferenceUtils.setPreviousFetchTime(previousFetchTime, context);
            }
        }
    }

    private long getConvertedAppVersionCode(Context context) {
        String oldVersionName = PreferenceUtils.getAppVersion(context);
        switch (oldVersionName) {
            case "1.21":
                return 76;
            case "1.19":
                return 74;
            case "1.18":
                return 73;
            case "1.17":
                return 71;
            case "1.16":
                return 67;
            case "1.15":
                return 65;
            case "1.14":
                return 64;
            case "1.13":
                return 60;
            case "1.12":
                return 59;
            case "1.11":
                return 57;
            case "1.10":
                return 53;
            case "1.09":
                return 50;
            case "1.08":
                return 49;
            case "1.07":
                return 46;
            case "1.06":
                return 43;
            case "1.05":
                return 42;
            case "1.04":
                return 40;
            case "1.03":
                return 31;
            case "1.02":
                return 26;
            case "1.01":
                return 24;
            case "1.00":
                return 23;
            default:
                return 0;
        }
    }

}

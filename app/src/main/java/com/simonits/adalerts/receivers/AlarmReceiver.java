package com.simonits.adalerts.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.simonits.adalerts.services.ForegroundService;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "AlarmReceiver Received");
        if (PreferenceUtils.optionGetDataFetch(context)) {
            if (!PreferenceUtils.getAlertList(context).isEmpty()) {
                String frequency = PreferenceUtils.optionGetFrequency(Utility.getFrequencyKey(context), context);
                if (!frequency.equals(Constants.FREQUENCY_NEVER)) {
                    long frequencyInMilliseconds = Utility.getMilliseconds(frequency);
                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    if (currentTime - frequencyInMilliseconds >= PreferenceUtils.getPreviousFetchTime(context)) {
                        if (!Utility.isServiceRunningInForeground(context, ForegroundService.class)) {
                            ContextCompat.startForegroundService(context, new Intent(context, ForegroundService.class));
                            PreferenceUtils.setPreviousFetchTime(currentTime, context);
                        } else {
                            Log.i(TAG, "Data Fetch is already active.");
                        }
                    } else {
                        Log.i(TAG, "Still too soon for a Data Fetch.");
                    }
                } else {
                    Log.i(TAG, "Frequency is Never.");
                }
            } else {
                Log.i(TAG, "There's no alerts set up.");
            }
            Utility.setCheckAlarm(context.getApplicationContext(), true);
        } else {
            Log.i(TAG, "DataFetch is False");
        }
    }
}
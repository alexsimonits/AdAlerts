package com.simonits.adalerts.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

public class RevivalWorker extends Worker {

    private static final String TAG = "RevivalWorker";

    private final Context context;

    public RevivalWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork() STARRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRT");

        if (PreferenceUtils.optionGetDataFetch(context) && !Utility.isServiceRunningInForeground(context, ForegroundService.class)) {
            Utility.setCheckAlarm(context.getApplicationContext(), false);
        }

        return Result.success();
    }
}
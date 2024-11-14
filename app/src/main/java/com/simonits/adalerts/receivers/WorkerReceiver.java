package com.simonits.adalerts.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.simonits.adalerts.services.RevivalWorker;

import java.util.concurrent.TimeUnit;

public class WorkerReceiver extends BroadcastReceiver {

    private static final String TAG = "WorkerReceiver";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BroadcastReceiver Received");
        PeriodicWorkRequest.Builder myWorkBuilder = new PeriodicWorkRequest.Builder(RevivalWorker.class, 15, TimeUnit.MINUTES);
        PeriodicWorkRequest myWork = myWorkBuilder.build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("jobTag", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, myWork);
    }
}
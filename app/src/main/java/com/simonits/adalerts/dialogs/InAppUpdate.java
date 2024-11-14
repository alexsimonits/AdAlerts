package com.simonits.adalerts.dialogs;

import android.content.Context;
import android.content.IntentSender;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.simonits.adalerts.R;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;

public class InAppUpdate {

    private static final String TAG = "InAppUpdate";

    public static void checkInAppUpdate(Context context, FragmentActivity activity) {
        // Creates instance of the manager.
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                Log.i("InAppUpdate", "Update available");
                FirebaseCrashlytics.getInstance().log("Update available");
                if (appUpdateInfo.availableVersionCode() > PreferenceUtils.getUpdateVersionCode(context)) {
                    PreferenceUtils.setUpdateVersionCode(appUpdateInfo.availableVersionCode(), context);

                    // Create a listener to track request state updates.
                    InstallStateUpdatedListener listener = state -> {
                        if (state.installStatus() == InstallStatus.DOWNLOADED) {
                            // After the update is downloaded, show a notification
                            // and request user confirmation to restart the app.
                            popupSnackbarForCompleteUpdate(context, activity);
                        }
                    };

                    // Before starting an update, register a listener for updates.
                    appUpdateManager.registerListener(listener);

                    // Start an update.
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // The current activity making the update request.
                                activity,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                                // Include a request code to later monitor this update request.
                                Constants.INAPPUPDATE_REQUESTCODE);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }

    private static void popupSnackbarForCompleteUpdate(Context context, FragmentActivity activity) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);
        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.main_coordinator), "Update ready to install.", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("INSTALL", view -> appUpdateManager.completeUpdate());
        snackbar.show();
    }

    public static void activityOnResume(Context context, FragmentActivity activity) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate(context, activity);
                    }
                });
    }

}

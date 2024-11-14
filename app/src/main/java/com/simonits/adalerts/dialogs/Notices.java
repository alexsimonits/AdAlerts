package com.simonits.adalerts.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;

public class Notices {

    private static final String TAG = "Notices";

    public static void appLaunched(Context context, FragmentActivity activity) {
        String noticeStr = FirebaseRemoteConfig.getInstance().getString("noticeMessages1_17").replaceAll("\\\\n", "\n");
        String[] notices = noticeStr.split("NEXTNOTICE");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String savedNotices = prefs.getString("noticeStr", "");
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("noticeStr", noticeStr);
        prefsEditor.apply();

        int newNotices = 0;
        for (String notice : notices) {
            if (!savedNotices.contains(notice)) {
                newNotices++;
            }
        }

        int noticeNumber = newNotices;
        ConfirmDialog dialog;
        for (int i = notices.length - 1; i >= 0; i--) {
            if (!savedNotices.contains(notices[i])) {
                if (newNotices > 1) {
                    dialog = new ConfirmDialog(context, notices[i], "Notice! " + noticeNumber-- + "/" + newNotices, Constants.DIALOGTYPE_NOTICE);
                } else {
                    dialog = new ConfirmDialog(context, notices[i], "Notice!", Constants.DIALOGTYPE_NOTICE);
                }
                dialog.show(activity.getSupportFragmentManager(), "confirm dialog");
            }
        }

        long currentAppVersionCode;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentAppVersionCode = pInfo.getLongVersionCode();
            } else {
                currentAppVersionCode = pInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
            currentAppVersionCode = 0;
        }
        long whatsNewAppVersionCode = PreferenceUtils.getWhatsNewAppVersionCode(context);
        if (whatsNewAppVersionCode != 0 && whatsNewAppVersionCode < currentAppVersionCode) {
            String version;
            try {
                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                version = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                version = "???";
                Log.e(TAG, e.toString());
            }
            dialog = new ConfirmDialog(context, "- minor bug fixes", "What's New v" + version, Constants.DIALOGTYPE_NOTICE);
            dialog.show(activity.getSupportFragmentManager(), "confirm dialog");
        }
        PreferenceUtils.setWhatsNewAppVersionCode(currentAppVersionCode, context);
    }
}

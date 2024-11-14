package com.simonits.adalerts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.simonits.adalerts.R;
import com.simonits.adalerts.objects.Alert;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simonits.adalerts.objects.Listing;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class PreferenceUtils {

    private static final String TAG = "PreferenceUtils";

    public PreferenceUtils() {

    }

    public static void saveAlertList(ArrayList<Alert> alertList, Context context) {
        Gson gson = new Gson();
        String json = gson.toJson(alertList);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(Constants.KEY_ALERTLIST, json);
        prefsEditor.apply();
    }

    public static ArrayList<Alert> getAlertList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.KEY_ALERTLIST, null);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Alert>>() {
        }.getType();
        ArrayList<Alert> alertList = gson.fromJson(json, type);
        if (alertList == null) {
            alertList = new ArrayList<>();
        }
        return alertList;
    }

    public static void saveFavsList(ArrayList<Listing> favourites, Context context) {
        if (favourites.size() > 100) {
            favourites.remove(favourites.size() - 1);
        } // makes sure FavsList does not get too long to where the app lags hard in the recView of FavouritesActivity

        Gson gson = new Gson();
        String json = gson.toJson(favourites);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(Constants.KEY_FAVSLIST, json);
        prefsEditor.apply();
    }

    public static ArrayList<Listing> getFavsList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.KEY_FAVSLIST, null);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Listing>>() {
        }.getType();
        ArrayList<Listing> favourites = gson.fromJson(json, type);
        if (favourites == null) {
            favourites = new ArrayList<>();
        }
        return favourites;
    }

    public static void saveSpams(ArrayList<String> spamList, Context context) {
        String spams = "";
        for (String spam : spamList) {
            spams += spam;
            spams += "  ";
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(Constants.KEY_SPAM, spams);
        prefsEditor.apply();
    }

    public static ArrayList<String> getSpams(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String spams = prefs.getString(Constants.KEY_SPAM, null);
        if (Objects.equals(spams, "")) {
            throw new NullPointerException("String spams == \"\"");
        }

        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(spams).split("  ")));
    }

    public static boolean spamCheck(String check, Context context) {
        ArrayList<String> spams;
        try {
            spams = getSpams(context);
        } catch (NullPointerException e) {
            return false;
        }

        for (String spam : spams) {
            if (spam.equals(check)) {
                return true;
            }
        }

        return false;
    }

    public static void addSpam(String spam, Context context) {
        ArrayList<String> spams;
        try {
            spams = getSpams(context);
        } catch (NullPointerException e) {
            spams = new ArrayList<>();
        }

        Log.i(TAG, "Adding spam: " + spam);
        spams.add(spam);
        if (spams.size() > 200) {
            spams.remove(0);
        }
        saveSpams(spams, context);
    }

    public static boolean optionGetDataFetch(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getResources().getString(R.string.settings_data_fetch), true);
    }

    public static boolean optionGetWifi(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Constants.KEY_OPTION_WIFI, true);
    }

    public static String getAppVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Constants.KEY_APPVERSION, "0.00");
    }

    public static String optionGetInterval(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Constants.KEY_OPTION_INTERVAL, "15 Minutes");
    }

    public static void saveFacebookLogin(boolean login, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(Constants.KEY_FACEBOOK_LOGIN, login);
        prefsEditor.apply();
    }

    public static boolean getFacebookLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Constants.KEY_FACEBOOK_LOGIN, false);
    }

    public static long getTargetTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Constants.KEY_TARGETTIME, 0);
    } // old. Last used in 103

    public static void setFacebookResumeTime(String addedTimeKey, Context context, int facebookAlertsNum) {
        Calendar calendar = Calendar.getInstance();
        try {
            if (addedTimeKey.equals("pauseTimeMinutes")) {
                if (facebookAlertsNum < 3) {
                    calendar.add(Calendar.MINUTE, 3);
                } else if (facebookAlertsNum < 5) {
                    calendar.add(Calendar.MINUTE, 5);
                } else {
                    calendar.add(Calendar.MINUTE, Integer.parseInt(Objects.requireNonNull(Utility.getScrapeMap(Constants.TABSITE_FACEBOOK, "listings").get(addedTimeKey))));
                }
            } else { // "timeoutTimeHours"
                calendar.add(Calendar.HOUR, Integer.parseInt(Objects.requireNonNull(Utility.getScrapeMap(Constants.TABSITE_FACEBOOK, "listings").get(addedTimeKey))));
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            if (addedTimeKey.equals("pauseTimeMinutes")) {
                calendar.add(Calendar.MINUTE, 10);
            } else { // "timeoutTimeHours"
                calendar.add(Calendar.HOUR, 2);
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(Constants.KEY_FACEBOOKRESUMETIME, calendar.getTimeInMillis());
        prefsEditor.apply();

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm dd/M/yyyy", Locale.getDefault());
        Log.i(TAG, "Facebook Resume Time set to " + sdf.format(calendar.getTime()));
    }

    public static long getFacebookResumeTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Constants.KEY_FACEBOOKRESUMETIME, 0);
    }

    public static void setFacebookTimeoutCount(int count, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(Constants.KEY_FACEBOOKTIMEOUTCOUNT, count);
        prefsEditor.apply();
    }

    public static int getFacebookTimeoutCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(Constants.KEY_FACEBOOKTIMEOUTCOUNT, 0);
    }

    public static void setAppVersionCode(long appVersionCode, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(Constants.KEY_APPVERSIONCODE, appVersionCode);
        prefsEditor.apply();
    }

    public static long getAppVersionCode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Constants.KEY_APPVERSIONCODE, 0);
    }

    public static void setUpdateVersionCode(long updateVersionCode, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(Constants.KEY_UPDATEPVERSIONCODE, updateVersionCode);
        prefsEditor.apply();
    }

    public static long getUpdateVersionCode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Constants.KEY_UPDATEPVERSIONCODE, 0);
    }

    public static boolean optionGetNotifyAsAlertsAreChecked(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(context.getResources().getString(R.string.settings_notification_timing), "0");
        return value.equals("1");
    }

    public static boolean optionGetNotificationToNewListingsActivity(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(context.getResources().getString(R.string.settings_notification_action), "0");
        return value.equals("1");
    }

    public static boolean optionGetDnd(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Constants.KEY_OPTION_DND, true);
    }

    public static void optionSaveFrequency(String key, String frequency, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(key, frequency);
        prefsEditor.apply();
    }

    public static String optionGetFrequency(String key, Context context) {
        if (key.equals(Constants.NO_FREQUENCY_KEY)) {
            return Constants.FREQUENCY_NEVER;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (key.equals(Constants.KEY_SETTING_WIFIFREQUENCY) || key.equals(Constants.KEY_SETTING_CHARGEFREQUENCY) || key.equals(Constants.KEY_SETTING_BATTERYFREQUENCY)) {
            return prefs.getString(key, "15 Minutes");
        } else {
            return prefs.getString(key, Constants.FREQUENCY_NEVER);
        }
    }

    public static void setCheckTime(long checkTime, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(Constants.KEY_CHECKTIME, checkTime);
        prefsEditor.apply();
    }

    public static long getCheckTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Constants.KEY_CHECKTIME, 0);
    }

    public static void setWhatsNewAppVersionCode(long whatsNewAppVersionCode, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(Constants.KEY_WHATSNEWAPPVERSIONCODE, whatsNewAppVersionCode);
        prefsEditor.apply();
    }

    public static long getWhatsNewAppVersionCode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Constants.KEY_WHATSNEWAPPVERSIONCODE, 0);
    }

    public static void setFrequencyWarningOk(boolean frequencyWarningOk, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(Constants.KEY_FREQUENCYWARNINGOK, frequencyWarningOk);
        prefsEditor.apply();
    }

    public static boolean getFrequencyWarningOk(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Constants.KEY_FREQUENCYWARNINGOK, false);
    }

    public static void setPreviousFetchTime(long previousFetchTime, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(Constants.KEY_PREVIOUSFETCHTIME, previousFetchTime);
        prefsEditor.apply();
    }

    public static long getPreviousFetchTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long previousFetchTime = prefs.getLong(Constants.KEY_PREVIOUSFETCHTIME, 0);
        if (previousFetchTime == 0) {
            previousFetchTime = Calendar.getInstance().getTimeInMillis();
            setPreviousFetchTime(previousFetchTime, context);
        }
        return previousFetchTime;
    }

    public static void saveNewListingsList(ArrayList<Listing> newListings, Context context) {
        Gson gson = new Gson();
        String json = gson.toJson(newListings);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(Constants.KEY_NEWLISTINGSLIST, json);
        prefsEditor.apply();
    }

    public static ArrayList<Listing> getNewListingsList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.KEY_NEWLISTINGSLIST, null);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Listing>>() {
        }.getType();
        ArrayList<Listing> newListings = gson.fromJson(json, type);
        if (newListings == null) {
            newListings = new ArrayList<>();
        }
        return newListings;
    }

}

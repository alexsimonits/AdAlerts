package com.simonits.adalerts.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.simonits.adalerts.R;
import com.simonits.adalerts.activities.MainActivity;
import com.simonits.adalerts.activities.NewListingsActivity;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    public static final String CHANNELFOREGROUND_ID = "ForegroundServiceChannel";
    public static final String CHANNELALERT_ID = "AlertChannel";

    private NotificationManager notificationManager;
    private ArrayList<Alert> alertList;
    private volatile ArrayList<Listing> freshFacebookListings;
    private Alert fAlert;
    private int fRngNum;
    private volatile String fUrl;
    private volatile int pass;
    private volatile boolean jsInjected;
    private volatile boolean cleanedUp;
    private volatile boolean fFiltered;
    private boolean cancelled;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
        updateProgress(0, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        new Thread(() -> {
            fRngNum = new Random().nextInt(1000 - 1) + 1;
            Log.i(TAG, ">>>>DATA FETCH START<<<<");
            FirebaseCrashlytics.getInstance().log(">>>>DATA FETCH START<<<<");
            alertList = PreferenceUtils.getAlertList(ForegroundService.this);
            if (!alertList.isEmpty()) {
                backgroundWork();
            } else {
                Log.i(TAG, ">>>>ALERT LIST EMPTY<<<<");
                FirebaseCrashlytics.getInstance().log(">>>>ALERT LIST EMPTY<<<<");
            }
            Log.i(TAG, ">>>>DATA FETCH END<<<<");
            FirebaseCrashlytics.getInstance().log(">>>>DATA FETCH END<<<<");
            ForegroundService.this.stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        wakeLock.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channelForegroundService = new NotificationChannel(
                    CHANNELFOREGROUND_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_NONE
            );
            NotificationChannel channelAlert = new NotificationChannel(
                    CHANNELALERT_ID,
                    "Alert Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannels(new ArrayList<NotificationChannel>() {{
                add(channelForegroundService);
                add(channelAlert);
            }});
        }
    }

    private void updateProgress(int currentAlert, int totalAlerts) {
        Intent notificationIntent = new Intent(ForegroundService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ForegroundService.this,
                2, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(ForegroundService.this, CHANNELFOREGROUND_ID)
                .setContentTitle("Checking for new listings...")
                .setSmallIcon(R.drawable.datafetch_icon)
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setOngoing(true);
        if (currentAlert != 0 && totalAlerts != 0) {
            notification.setContentText("Alert " + currentAlert + " of " + totalAlerts);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundService.this.startForeground(1, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            ForegroundService.this.startForeground(1, notification.build());
        }
    }

    private boolean isCancelled() {
        return !PreferenceUtils.optionGetDataFetch(ForegroundService.this) || PreferenceUtils.optionGetFrequency(Utility.getFrequencyKey(ForegroundService.this), ForegroundService.this).equals(Constants.FREQUENCY_NEVER);
    }

    private void backgroundWork() {
        boolean facebookPaused = PreferenceUtils.getFacebookResumeTime(ForegroundService.this) > Calendar.getInstance().getTimeInMillis();
        boolean facebookOfficialTimeout = false;
        boolean facebookTimeout = true;
        int facebookAlertsNum = 0;
        boolean alertListContainsNewListings = false;
        for (int i = 0; i < alertList.size(); i++) {
            updateProgress(i + 1, alertList.size());
            Alert alert = alertList.get(i);
            ArrayList<Listing> freshListings;
            if (alert.hasKijiji()) {
                String kLatLong = alert.getKLatLong();
                freshListings = getAlertData(alert, alert.getKURL(1, ForegroundService.this), Constants.TABSITE_KIJIJI);
                if (!kLatLong.equals(alert.getKLatLong())) {
                    Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_KIJIJI, 8, null, alert.getId(), alert);
                }
                if (freshListings.isEmpty()) {
                    Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_KIJIJI, 1, new ArrayList<>(), alert.getId(), null);
                } else if (freshListings.get(0).getID().equals("error")) {
                    Log.i(TAG, "KIJIJI DATA FETCH ERROR!!!");
                    FirebaseCrashlytics.getInstance().log("KIJIJI DATA FETCH ERROR!!!");
                } else {
                    Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_KIJIJI, 1, freshListings, alert.getId(), null);
                }
            }
            if (alert.hasEbay()) {
                freshListings = getAlertData(alert, alert.getEURL(1), Constants.TABSITE_EBAY);
                if (freshListings.isEmpty()) {
                    Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_EBAY, 1, new ArrayList<>(), alert.getId(), null);
                } else if (freshListings.get(0).getID().equals("error")) {
                    Log.i(TAG, "EBAY DATA FETCH ERROR!!!");
                    FirebaseCrashlytics.getInstance().log("EBAY DATA FETCH ERROR!!!");
                } else {
                    Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_EBAY, 1, freshListings, alert.getId(), null);
                }
            }
            if (alert.hasCraigslist()) {
                freshListings = getAlertData(alert, alert.getCURL(1, ""), Constants.TABSITE_CRAIGSLIST);
                if (freshListings.isEmpty()) {
                    Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_CRAIGSLIST, 1, new ArrayList<>(), alert.getId(), null);
                } else if (freshListings.get(0).getID().equals("error")) {
                    Log.i(TAG, "CRAIGSLIST DATA FETCH ERROR!!!");
                    FirebaseCrashlytics.getInstance().log("CRAIGSLIST DATA FETCH ERROR!!!");
                } else {
                    Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_CRAIGSLIST, 1, freshListings, alert.getId(), null);
                }
            }
            if (alert.hasFacebook()) {
                facebookAlertsNum++;
                if (!facebookPaused && !facebookOfficialTimeout) {
                    Log.i(TAG, " ");
                    fFiltered = false;
                    freshListings = getFacebookAlertData(alert);
                    if (!freshListings.isEmpty() && freshListings.get(0).getID().equals("accountTimeOut")) {
                        Log.i(TAG, "FACEBOOK ACCOUNT OFFICIALLY TIMED OUT");
                        FirebaseCrashlytics.getInstance().log("FACEBOOK ACCOUNT OFFICIALLY TIMED OUT");
                        facebookOfficialTimeout = true;
                    } else if ((freshListings.isEmpty() && !fFiltered)) {
                        Log.i(TAG, "FACEBOOK ACCOUNT TIMEOUT???");
                        FirebaseCrashlytics.getInstance().log("FACEBOOK ACCOUNT TIMEOUT???");
                    } else {
                        facebookTimeout = false;
                        if (!freshListings.isEmpty() && freshListings.get(0).getID().equals("error")) {
                            Log.i(TAG, "FACEBOOK DATA FETCH ERROR!!!");
                            FirebaseCrashlytics.getInstance().log("FACEBOOK DATA FETCH ERROR!!!");
                        } else if ((freshListings.isEmpty() && fFiltered) || (!freshListings.isEmpty() && freshListings.get(0).getID().equals("no listings found"))) {
                            Log.i(TAG, "NO FACEBOOK LISTINGS FOUND FOR ALERT: " + alert.getName());
                            FirebaseCrashlytics.getInstance().log("NO FACEBOOK LISTINGS FOUND FOR ALERT: " + alert.getName());
                            Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_FACEBOOK, 1, null, alert.getId(), null);
                        } else if (freshListings.get(0).getID().equals("stopped")) {
                            Log.i(TAG, "FACEBOOK DATA FETCH STOPPED!!!");
                            FirebaseCrashlytics.getInstance().log("FACEBOOK DATA FETCH STOPPED!!!");
                        } else {
                            Utility.updateSavedPrefs(ForegroundService.this, 0, Constants.TABSITE_FACEBOOK, 1, freshListings, alert.getId(), null);
                        }
                    }
                } else {
                    Log.i(TAG, "Facebook fetches currently paused");
                }
            }

            Log.i(TAG, " ");
            int alertID = alert.getId();
            alertList = PreferenceUtils.getAlertList(ForegroundService.this);
            if (cancelled || isCancelled()) {
                cancelled = true;
                if (PreferenceUtils.optionGetNotifyAsAlertsAreChecked(ForegroundService.this)) {
                    updateNotification(false);
                } else {
                    updateNotification(alertListContainsNewListings);
                }
                break;
            } else {
                alert = Utility.getAlertFromList(alertList, alertID);
                boolean alertContainsNewListings = false;
                if (alert != null) {
                    removeAnyOnlyNewBgListingsThatAreGone(alert);
                    alertContainsNewListings = addAnyNewListingsToOnlyNewBgListings(alert);
                    if (alertContainsNewListings && !PreferenceUtils.optionGetNotifyAsAlertsAreChecked(ForegroundService.this)) {
                        alertListContainsNewListings = true;
                    }
                }
                for (int j = i; j >= 0; j--) {
                    if (j >= alertList.size() || alertID != alertList.get(j).getId()) {
                        i--;
                    } else {
                        break;
                    }
                }
                if (PreferenceUtils.optionGetNotifyAsAlertsAreChecked(ForegroundService.this)) {
                    if (alertListContainsNewListings) {
                        updateNotification(true);
                        alertListContainsNewListings = false;
                    } else {
                        updateNotification(alertContainsNewListings);
                    }
                } else if (i == alertList.size() - 1) {
                    updateNotification(alertListContainsNewListings);
                }
            }
        }

        if (!cancelled && !isCancelled() && !facebookPaused) {
            facebookTimeStuff(facebookAlertsNum, facebookTimeout, facebookOfficialTimeout);
        }
    }

    private void facebookTimeStuff(int facebookAlertsNum, boolean facebookTimeout, boolean facebookOfficialTimeout) {
        String addedTimeKey = "pauseTimeMinutes";
        if (facebookAlertsNum != 0 && (facebookTimeout || facebookOfficialTimeout)) {
            int count = PreferenceUtils.getFacebookTimeoutCount(ForegroundService.this) + 1;
            if (count >= 3 || facebookOfficialTimeout) {
                FirebaseCrashlytics.getInstance().log("Facebook Marketplace account is temporarily timed out");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                addedTimeKey = "timeoutTimeHours";
                PreferenceUtils.setFacebookTimeoutCount(0, ForegroundService.this);
            } else {
                PreferenceUtils.setFacebookTimeoutCount(count, ForegroundService.this);
            }
        } else {
            PreferenceUtils.setFacebookTimeoutCount(0, ForegroundService.this);
        }
        PreferenceUtils.setFacebookResumeTime(addedTimeKey, ForegroundService.this, facebookAlertsNum);
    }

    private ArrayList<Listing> getAlertData(Alert alert, String url, String site) {
        Log.i(TAG, " ");
        Log.i(TAG, "~~~ForegroundService " + url + " ~~~");
        FirebaseCrashlytics.getInstance().log("~~~ForegroundService " + url + " ~~~");
        OkHttpClient client;
        if (site.equals(Constants.TABSITE_CRAIGSLIST)) {
            client = new OkHttpClient().newBuilder()
                    .addInterceptor(chain -> {
                        final Request original = chain.request();
                        final Request authorized = original.newBuilder()
                                // don't think i need cookies to get consistent data from Craigslist. Craigslist is just that easy boi
                                .build();
                        return chain.proceed(authorized);
                    })
                    .build();
        } else {
            client = Utility.getClient(site);
        }
        String htmlStr = Utility.getHtml(client, url);
        if (htmlStr == null) {
            return new ArrayList<Listing>() {{
                add(new Listing("error"));
            }};
        }

        if (site.equals(Constants.TABSITE_KIJIJI)) {
            return Utility.scrapeKijiji(ForegroundService.this, htmlStr, alert);
        } else if (site.equals(Constants.TABSITE_EBAY)) {
            return Utility.scrapeEbay(ForegroundService.this, htmlStr, alert);
        } else {
            return Utility.scrapeCraigslist(ForegroundService.this, htmlStr, alert);
        }
    }

    private ArrayList<Listing> getFacebookAlertData(Alert alert) {
        fAlert = alert;
        pass = 0;
        jsInjected = false;
        cleanedUp = false;
        freshFacebookListings = null;
        String url = alert.getFURL();
        url = url + "&" + fRngNum;
        fUrl = url;

        Log.i(TAG, "~~~ForegroundService " + fUrl + " ~~~");
        FirebaseCrashlytics.getInstance().log("~~~ForegroundService " + fUrl + " ~~~");

        Handler moreHandler = new Handler(ForegroundService.this.getMainLooper());
        Handler mainHandler = new Handler(ForegroundService.this.getMainLooper());
        @SuppressLint("SetJavaScriptEnabled") Runnable myRunnable = () -> {
            WebView webView = new WebView(ForegroundService.this);
            webView.setVisibility(View.INVISIBLE);
            webView.getSettings().setUserAgentString(Utility.getFacebookUA());
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url1) {
                    if (jsInjected) {
                        Log.i(TAG, "jsInjected is TRUEEEEEEEEEEEEEE");
                    } else {
                        Log.i(TAG, "jsInjected is FAAAAAAAAAAAAALSE");
                    }
                    Log.i(TAG, "onPageFinished. pass: " + pass);
                    Log.i(TAG, "webview URL: " + webView.getOriginalUrl());
                    if (fUrl.equals(webView.getOriginalUrl())) {
                        if (pass >= 2) {
                            cleanedUp = true;
                        } else {
                            if (!jsInjected) {
                                jsInjected = true;
                                webView.loadUrl("javascript:window.HtmlViewer.showHTML" +
                                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                            }
                        }
                    } else {
                        Log.i(TAG, "URL is different than WebView URL, so not loading JS!");
                        // should I instead loadUrl(url) again here...?
                    }
                }
            });
            webView.loadUrl(fUrl);
            moreHandler.postDelayed(() -> {
                pass = 2;
                webView.stopLoading();
                Log.i(TAG, "stopped loading webview");
            }, 10000);
        };
        mainHandler.post(myRunnable);

        int count = 0;
        while (!cleanedUp && count < 20 && !cancelled && !isCancelled()) {
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
                count++;
                Log.i(TAG, "Asleeeeeeeeep: " + count);
            } catch (InterruptedException e) {
                Log.i(TAG, "SLEEP INTERRUPTED!!! yo dawg");
            }
        }
        moreHandler.removeCallbacksAndMessages(null);
        mainHandler.removeCallbacksAndMessages(null);
        if (freshFacebookListings == null && (count == 20 || pass >= 2)) {
            Log.i(TAG, "~error: " + fUrl);
            FirebaseCrashlytics.getInstance().log("~error: " + fUrl);
            return new ArrayList<Listing>() {{
                add(new Listing("error"));
            }};
        }

        if (cancelled || isCancelled()) {
            cancelled = true;
            Log.i(TAG, "~stopped: " + fUrl);
            FirebaseCrashlytics.getInstance().log("~stopped: " + fUrl);
            return new ArrayList<Listing>() {{
                add(new Listing("stopped"));
            }};
        }

        return freshFacebookListings;
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void showHTML(String html) {
            ArrayList<Listing> freshListings = Utility.scrapeFacebook(ForegroundService.this, html, fAlert);
            if (!freshListings.isEmpty() && !freshListings.get(0).getID().equals("error") && !freshListings.get(0).getID().equals("accountTimeOut")) {
                int size = freshListings.size();
                Utility.removeSpam(ForegroundService.this, Utility.removeFiltered(freshListings));
                if (freshListings.size() < size) {
                    fFiltered = true;
                }
                Utility.updateFavourites(ForegroundService.this, freshListings);
            }
            freshFacebookListings = freshListings;
            if (freshListings.isEmpty() && !fFiltered) {
                Log.i(TAG, "FACEBOOK DATA FETCH ERROR!!! TRYING AGAIN");
                FirebaseCrashlytics.getInstance().log("FACEBOOK DATA FETCH ERROR!!! TRYING AGAIN");
                jsInjected = false;
            } else {
                cleanedUp = true;
            }
        }
    }

    private void updateNotification(boolean newListingsExist) {
        int alertsWithNewListingsCount = 0;
        String alertStr = "";
        boolean noOnlyNewBgListingsExist = true;
        for (Alert alert : alertList) {
            if (!alert.getOnlyNewBgListings().isEmpty()) {
                noOnlyNewBgListingsExist = false;
                Log.i(TAG, "onlyNewBgListings IDs from alert:" + alert.getName() + " is: " + Utility.getListingsIDs(alert.getOnlyNewBgListings()));
                alertsWithNewListingsCount++;
                if (alert.getCustomName() != null && !alert.getCustomName().isEmpty()) { // temp. 122 update
                    alertStr = alert.getCustomName();
                } else {
                    alertStr = alert.getName();
                }
            }
        }
        if (noOnlyNewBgListingsExist) {
            notificationManager.cancel(2);
        } else {
            if (newListingsExist || isNotificationVisible()) {
                if (alertsWithNewListingsCount > 1) {
                    sendNotification("Alerts notified!", alertsWithNewListingsCount + " alerts notified!", newListingsExist);
                } else {
                    sendNotification("Alert notified!", alertStr, newListingsExist);
                }
            }
        }
    }

    private boolean isNotificationVisible() {
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == 2) {
                return true;
            }
        }
        return false;
    }

    private void removeAnyOnlyNewBgListingsThatAreGone(Alert alert) {
        if (!alert.getOnlyNewBgListings().isEmpty()) {
            ArrayList<Listing> listings = new ArrayList<>(alert.getOnlyNewBgListings());
            for (Listing listing : listings) {
                if ((listing.getID().startsWith("k") && !Utility.isInListings(alert.getKListings(), listing.getID())) || (listing.getID().startsWith("e") && !Utility.isInListings(alert.getEListings(), listing.getID())) || (listing.getID().startsWith("c") && !Utility.isInListings(alert.getCListings(), listing.getID()))) {
                    alert.removeOnlyNewBgListing(listing);
                    Utility.updateSavedPrefs(ForegroundService.this, 0, null, 3, new ArrayList<Listing>() {{
                        add(listing);
                    }}, alert.getId(), null);
                }
            }
        }
    }

    private boolean addAnyNewListingsToOnlyNewBgListings(Alert alert) {
        boolean newListingsExist = false;
        Log.i(TAG, "~~~" + alert.getName() + "~~~");
        if (alert.hasKijiji()) {
            Log.i(TAG, "KIJIJI: " + Utility.getListingsIDs(alert.getKListings()));
        }
        if (alert.hasEbay()) {
            Log.i(TAG, "EBAY: " + Utility.getListingsIDs(alert.getEListings()));
        }
        if (alert.hasCraigslist()) {
            Log.i(TAG, "CRAIGSLIST: " + Utility.getListingsIDs(alert.getCListings()));
        }
        if (alert.hasFacebook()) {
            Log.i(TAG, "FACEBOOK: " + Utility.getListingsIDs(alert.getFListingHistory()));
        }
        Log.i(TAG, " ");
        for (int i = 1; i <= 4; i++) {
            boolean skip = false;
            ArrayList<Listing> listings = new ArrayList<>();
            if (i == 1 && alert.hasKijiji()) {
                listings = alert.getKListings();
            } else if (i == 2 && alert.hasEbay()) {
                listings = alert.getEListings();
            } else if (i == 3 && alert.hasCraigslist()) {
                listings = alert.getCListings();
            } else if (i == 4 && alert.hasFacebook()) {
                listings = alert.getFListingHistory();
            } else {
                skip = true;
            }
            if (!skip) {
                for (Listing listing : listings) {
                    if (listing.isNewListing()) {
                        int oldSize = alert.getOnlyNewBgListings().size();
                        alert.addOnlyNewBgListing(listing);
                        if (alert.getOnlyNewBgListings().size() > oldSize) {
                            newListingsExist = true;
                            Utility.updateSavedPrefs(ForegroundService.this, 0, null, 2, new ArrayList<Listing>() {{
                                add(listing);
                            }}, alert.getId(), null);
                        }
                    }
                }
            }
        }
        return newListingsExist;
    }

    public void sendNotification(String title, String message, boolean newListingsExist) {
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(ForegroundService.this);
        if (PreferenceUtils.optionGetNotificationToNewListingsActivity(ForegroundService.this)) {
            taskStackBuilder.addNextIntentWithParentStack(new Intent(ForegroundService.this, NewListingsActivity.class));
        } else {
            taskStackBuilder.addNextIntent(new Intent(ForegroundService.this, MainActivity.class));
        }

        PendingIntent pendingIntent = taskStackBuilder.
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNELALERT_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        if (!newListingsExist) {
            notification.setOnlyAlertOnce(true);
        }

        notificationManager.notify(2, notification.build());
    }
}
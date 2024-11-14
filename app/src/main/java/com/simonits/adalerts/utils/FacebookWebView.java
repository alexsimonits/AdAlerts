package com.simonits.adalerts.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.simonits.adalerts.R;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.Listing;

import java.util.ArrayList;

public class FacebookWebView {

    private static final String TAG = "FacebookWebView";

    private final Context context;
    private final FragmentActivity activity;
    private final Alert alert;

    private WebView webView;
    private boolean fFiltered;
    private volatile boolean jsInjected;

    public FacebookWebView(Context context, FragmentActivity activity, Alert alert) {
        this.context = context;
        this.activity = activity;
        this.alert = alert;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void loadInitialFacebookListings() {
        webView = activity.findViewById(R.id.main_webView);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setUserAgentString(Utility.getFacebookUA());
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //String cookies = CookieManager.getInstance().getCookie(url);
                if (!jsInjected) {
                    jsInjected = true;
                    webView.loadUrl("javascript:window.HtmlViewer.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                }
            }
        });

        webView.loadUrl(alert.getFURL());
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void showHTML(String html) {
            activity.runOnUiThread(() -> {
                fFiltered = false;
                ArrayList<Listing> freshListingList = extractFacebookData(html);
                if (freshListingList.isEmpty() && !fFiltered) {
                    Log.i(TAG, "FACEBOOK DATA FETCH ERROR!!! TRYING AGAIN");
                    FirebaseCrashlytics.getInstance().log("FACEBOOK DATA FETCH ERROR!!! TRYING AGAIN");
                    jsInjected = false;
                    return;
                } else if (freshListingList.isEmpty() || freshListingList.get(0).getID().equals("no listings found")) {
                    Log.i(TAG, "NO FACEBOOK LISTINGS FOUND");
                    FirebaseCrashlytics.getInstance().log("NO FACEBOOK LISTINGS FOUND");
                    if (!(!alert.getFListingHistory().isEmpty() && !alert.getFListingHistory().get(0).getID().equals("no listings found"))) {
                        Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, Constants.TABSITE_FACEBOOK, 3, null, alert.getId(), null);
                    }
                } else {
                    Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, Constants.TABSITE_FACEBOOK, 4, freshListingList, alert.getId(), null);
                }
                webView.setVisibility(View.GONE);
                Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, Constants.TABSITE_FACEBOOK, 1, null, alert.getId(), null);
            });
        }
    }

    private ArrayList<Listing> extractFacebookData(String htmlStr) {
        Log.i(TAG, "~~~TabFragment " + alert.getFURL() + " ~~~");
        FirebaseCrashlytics.getInstance().log("~~~TabFragment " + alert.getFURL() + " ~~~");
        ArrayList<Listing> freshListingList = Utility.scrapeFacebook(context, htmlStr, alert);
        if (!freshListingList.isEmpty() && freshListingList.get(0).getID().equals("error")) {
            freshListingList.clear();
        } else {
            int size = freshListingList.size();
            Utility.removeSpam(context, Utility.removeFiltered(freshListingList));
            if (freshListingList.size() < size) {
                fFiltered = true;
            }
            Utility.updateFavourites(context, freshListingList);
        }
        return freshListingList;
    }

}

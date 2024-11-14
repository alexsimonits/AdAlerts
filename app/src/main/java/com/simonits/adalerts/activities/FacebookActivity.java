package com.simonits.adalerts.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simonits.adalerts.R;

import java.util.HashMap;
import java.util.Objects;

public class FacebookActivity extends AppCompatActivity {

    private static final String TAG = "FacebookActivity";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ProgressBar progressBar = findViewById(R.id.fProgressBar);
        RelativeLayout relativeLayout = findViewById(R.id.fRelativeLayout);

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        HashMap<String, String> map = new Gson().fromJson(
                mFirebaseRemoteConfig.getString("fNetworkStuff1_07"), new TypeToken<HashMap<String, String>>() {}.getType()
        );

        WebView webView = findViewById(R.id.fWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String cookies = CookieManager.getInstance().getCookie(url);
                Log.i(TAG, "All the cookies in a string: " + cookies);
                if (cookies != null && cookies.contains(Objects.requireNonNull(map.get("loggedInCookie")))) {
                    setResult(RESULT_OK);
                    finish();
                    overridePendingTransition(R.anim.nothing, R.anim.scale_out);
                } else {
                    progressBar.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                relativeLayout.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
        webView.loadUrl("https://m.facebook.com/");

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.scale_in, R.anim.slide_out_left);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.scale_in, R.anim.slide_out_left);
        }
        return super.onOptionsItemSelected(item);
    }

}

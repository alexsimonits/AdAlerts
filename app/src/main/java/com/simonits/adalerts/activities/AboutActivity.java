package com.simonits.adalerts.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simonits.adalerts.R;

import java.util.HashMap;
import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView versionName = findViewById(R.id.about_versionName);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionName.setText(getString(R.string.version_name, version));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        HashMap<String, String> map = new Gson().fromJson(
                mFirebaseRemoteConfig.getString("AboutStrings0_64"), new TypeToken<HashMap<String, String>>() {}.getType()
        );
        ((TextView)findViewById(R.id.about_description)).setText(map.get("description"));
        ((TextView)findViewById(R.id.about_emailLink)).setText(map.get("email"));
        ((TextView)findViewById(R.id.about_discordLink)).setText(map.get("discord"));

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.scroll_in_left, R.anim.scroll_out_right);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.scroll_in_left, R.anim.scroll_out_right);
        }

        return super.onOptionsItemSelected(item);
    }

}

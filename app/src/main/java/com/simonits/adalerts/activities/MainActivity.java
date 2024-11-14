package com.simonits.adalerts.activities;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.simonits.adalerts.R;
import com.simonits.adalerts.adapters.Main_RecAdapter;
import com.simonits.adalerts.dialogs.ConfirmDialog;
import com.simonits.adalerts.dialogs.InAppUpdate;
import com.simonits.adalerts.dialogs.Notices;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.receivers.AlarmReceiver;
import com.simonits.adalerts.receivers.WorkerReceiver;
import com.simonits.adalerts.services.ForegroundService;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.FacebookWebView;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ConfirmDialog.DeleteDialogListener, PopupMenu.OnMenuItemClickListener, Main_RecAdapter.OnAlertListener {

    private static final String TAG = "MainActivity";
    private static AlarmManager alarmManager;

    private Button newListingsButton;
    private Main_RecAdapter recAdapter;
    private TextView guideText;

    private ArrayList<Alert> alertList;
    private int alertID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.drawable.appicon_nobg_small);

        newListingsButton = findViewById(R.id.main_newListingsButton);
        RecyclerView recView = findViewById(R.id.main_recView);
        guideText = findViewById(R.id.guideText);

        if (PreferenceUtils.optionGetDataFetch(this) && !Utility.isServiceRunningInForeground(this, ForegroundService.class)) {
            Utility.setCheckAlarm(getApplicationContext(), false);
        }
        if (!Utility.doesWorkExist(this)) {
            sendBroadcast(new Intent("any string").setClass(this, WorkerReceiver.class));
        }

        Utility.updateSavedPrefs(this, Constants.UTILITY_MAINACTIVITY, null, 1, null, 0, null);
        alertList = PreferenceUtils.getAlertList(this);
        if (newListingsExist()) {
            newListingsButton.setVisibility(View.VISIBLE);
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recAdapter = new Main_RecAdapter(this, alertList, this);
        recView.setLayoutManager(gridLayoutManager);
        recView.setAdapter(recAdapter);
        if (alertList.isEmpty()) {
            guideText.setText(R.string.createNewAlert);
        } else {
            guideText.setText(R.string.alertOptions);
        }

        newListingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewListingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.scroll_in_right, R.anim.scroll_out_left);
        });

        FloatingActionButton fab = findViewById(R.id.main_fabCreate);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AlertActivity.class);
            createAlertResultLauncher.launch(intent);
            overridePendingTransition(R.anim.scale_in, R.anim.nothing);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            }).launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:com.simonits.adalerts"));
                startActivity(intent);
            }
        }

        Notices.appLaunched(this, this);
    }

    @Override
    public void onAlertClick(int position) {
        alertID = alertList.get(position).getId();

        Intent intent = new Intent(MainActivity.this, TabbedActivity.class);
        if (!alertList.get(position).getCustomName().isEmpty()) {
            intent.putExtra("name", alertList.get(position).getCustomName());
        } else {
            intent.putExtra("name", alertList.get(position).getName());
        }
        intent.putExtra("alertId", alertID);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.scale_out);
    }

    @Override
    public void onAlertHold(View view, int position) {
        alertID = alertList.get(position).getId();
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.setOnMenuItemClickListener(MainActivity.this);
        popupMenu.inflate(R.menu.menu_popup);
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        int itemId = item.getItemId();
        if (itemId == R.id.action_listings) {
            intent = new Intent(MainActivity.this, NewListingsActivity.class);
        } else if (itemId == R.id.action_favourites) {
            intent = new Intent(MainActivity.this, FavouritesActivity.class);
        } else if (itemId == R.id.action_settings) {
            intent = new Intent(MainActivity.this, SettingsActivity.class);
        } else if (itemId == R.id.action_faq) {
            intent = new Intent(MainActivity.this, FaqActivity.class);
        } else if (itemId == R.id.action_about) {
            intent = new Intent(MainActivity.this, AboutActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.scroll_in_right, R.anim.scroll_out_left);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        String dialogType = null;
        int itemId = menuItem.getItemId();
        if (itemId == R.id.item_edit) {
            Intent intent = new Intent(this, AlertActivity.class);
            intent.putExtra("alertId", alertID);
            //this.startActivityForResult(intent, 2);
            editAlertResultLauncher.launch(intent);
            overridePendingTransition(R.anim.scale_in, R.anim.nothing);
            return true;
        } else if (itemId == R.id.item_delete) {
            dialogType = Constants.DIALOGTYPE_DELETE;
        } else if (itemId == R.id.item_name) {
            dialogType = Constants.DIALOGTYPE_NAME;
        }
        Alert alert = Utility.getAlertFromList(alertList, alertID);
        ConfirmDialog dialog;
        if (!Objects.requireNonNull(alert).getCustomName().isEmpty()) {
            dialog = new ConfirmDialog(this, alert.getCustomName(), dialogType);
        } else {
            dialog = new ConfirmDialog(this, alert.getName(), dialogType);
        }
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "confirm dialog");
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INAPPUPDATE_REQUESTCODE && resultCode != RESULT_OK) {
            Log.i(TAG, "Update flow failed! Result code: " + resultCode);
            FirebaseCrashlytics.getInstance().log("Update flow failed! Result code: " + resultCode);
            // If the update is cancelled or fails,
            // you can request to start the update again.
        }
    }

    ActivityResultLauncher<Intent> createAlertResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        String strEditText = Objects.requireNonNull(Objects.requireNonNull(data).getStringExtra("search_text"));
                        Log.i(TAG, "SEARCH TEXT: " + strEditText);

                        refreshLocalAlertList();

                        alertID = generateAlertId();
                        Alert alert = new Alert(alertID, strEditText, data.getStringExtra("custom_name"), data.getBooleanExtra("kSwitch", false), data.getBooleanExtra("eSwitch", false), data.getBooleanExtra("cSwitch", false), data.getBooleanExtra("fSwitch", false), data.getStringExtra("kLoc"), data.getStringExtra("kKm"), data.getStringExtra("kPostalZip"), data.getStringExtra("kCat"), data.getStringExtra("kPrice"), data.getStringExtra("kFrom"), data.getStringExtra("kTo"), data.getStringExtra("kType"), data.getStringExtra("kInclude"), data.getStringExtra("kIncludeAny"), data.getStringExtra("kExclude"), data.getStringExtra("eLoc"), data.getStringExtra("eKm"), data.getStringExtra("ePostalZip"), data.getStringExtra("eCat"), data.getStringExtra("ePrice"), data.getStringExtra("eFrom"), data.getStringExtra("eTo"), data.getStringExtra("eType"), data.getStringExtra("eInclude"), data.getStringExtra("eIncludeAny"), data.getStringExtra("eExclude"), data.getStringExtra("cLoc"), data.getStringExtra("cPrice"), data.getStringExtra("cFrom"), data.getStringExtra("cTo"), data.getStringExtra("cCat"), data.getStringExtra("cInclude"), data.getStringExtra("cIncludeAny"), data.getStringExtra("cExclude"), data.getStringExtra("fCat"), data.getStringExtra("fPrice"), data.getStringExtra("fFrom"), data.getStringExtra("fTo"), data.getStringExtra("fInclude"), data.getStringExtra("fIncludeAny"), data.getStringExtra("fExclude"));
                        alertList.add(alert);
                        Utility.updateSavedPrefs(MainActivity.this, Constants.UTILITY_MAINACTIVITY, null, 5, null, 0, alert);
                        Snackbar.make(findViewById(R.id.main_coordinator), "Alert created", Snackbar.LENGTH_SHORT).show();
                        guideText.setText(R.string.alertOptions);
                        recAdapter.notifyItemInserted(recAdapter.getItemCount());

                        if (alert.hasFacebook() && !PreferenceUtils.optionGetFrequency(Utility.getFrequencyKey(MainActivity.this), MainActivity.this).equals(Constants.FREQUENCY_NEVER)) {
                            new FacebookWebView(MainActivity.this, MainActivity.this, alert).loadInitialFacebookListings();
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> editAlertResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        String strEditText = Objects.requireNonNull(Objects.requireNonNull(data).getStringExtra("search_text"));
                        Log.i(TAG, "SEARCH TEXT: " + strEditText);

                        refreshLocalAlertList();

                        Alert alert = new Alert(alertID, strEditText, data.getStringExtra("custom_name"), data.getBooleanExtra("kSwitch", false), data.getBooleanExtra("eSwitch", false), data.getBooleanExtra("cSwitch", false), data.getBooleanExtra("fSwitch", false), data.getStringExtra("kLoc"), data.getStringExtra("kKm"), data.getStringExtra("kPostalZip"), data.getStringExtra("kCat"), data.getStringExtra("kPrice"), data.getStringExtra("kFrom"), data.getStringExtra("kTo"), data.getStringExtra("kType"), data.getStringExtra("kInclude"), data.getStringExtra("kIncludeAny"), data.getStringExtra("kExclude"), data.getStringExtra("eLoc"), data.getStringExtra("eKm"), data.getStringExtra("ePostalZip"), data.getStringExtra("eCat"), data.getStringExtra("ePrice"), data.getStringExtra("eFrom"), data.getStringExtra("eTo"), data.getStringExtra("eType"), data.getStringExtra("eInclude"), data.getStringExtra("eIncludeAny"), data.getStringExtra("eExclude"), data.getStringExtra("cLoc"), data.getStringExtra("cPrice"), data.getStringExtra("cFrom"), data.getStringExtra("cTo"), data.getStringExtra("cCat"), data.getStringExtra("cInclude"), data.getStringExtra("cIncludeAny"), data.getStringExtra("cExclude"), data.getStringExtra("fCat"), data.getStringExtra("fPrice"), data.getStringExtra("fFrom"), data.getStringExtra("fTo"), data.getStringExtra("fInclude"), data.getStringExtra("fIncludeAny"), data.getStringExtra("fExclude"));
                        int alertPosition = Utility.getAlertPosition(alertList, alertID);
                        alertList.remove(alertPosition);
                        alertList.add(alertPosition, alert);
                        Utility.updateSavedPrefs(MainActivity.this, Constants.UTILITY_MAINACTIVITY, null, 6, null, alertID, alert);
                        Snackbar.make(findViewById(R.id.main_coordinator), "Alert edited", Snackbar.LENGTH_SHORT).show();
                        recAdapter.notifyItemChanged(alertPosition);

                        if (alert.hasFacebook() && !PreferenceUtils.optionGetFrequency(Utility.getFrequencyKey(MainActivity.this), MainActivity.this).equals(Constants.FREQUENCY_NEVER)) {
                            new FacebookWebView(MainActivity.this, MainActivity.this, alert).loadInitialFacebookListings();
                        }
                    }
                }
            });

    private int generateAlertId() {
        int id = 1;
        for (Alert alert : alertList) {
            if (alert.getId() >= id) {
                id = alert.getId() + 1;
            }
        }
        return id;
    }

    @Override
    public void onYesClicked() {
        // remove alert
        Utility.updateSavedPrefs(this, Constants.UTILITY_MAINACTIVITY, null, 3, null, alertID, null);
        alertList.remove(Utility.getAlertPosition(alertList, alertID));
        if (alertList.isEmpty()) {
            guideText.setText(R.string.createNewAlert);
        }

        // re-assign alertIds and refresh local alert list
        Utility.updateSavedPrefs(this, Constants.UTILITY_MAINACTIVITY, null, 2, null, alertID, null);
        refreshLocalAlertList();
        Snackbar.make(findViewById(R.id.main_coordinator), "Alert deleted", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConfirmClicked(String customName) {
        Alert alert = Utility.getAlertFromList(alertList, alertID);
        if (customName.isEmpty()) {
            if (!Objects.requireNonNull(alert).getCustomName().isEmpty()) {
                Snackbar.make(findViewById(R.id.main_coordinator), "Custom name removed", Snackbar.LENGTH_SHORT).show();
            }
            alert.setCustomName("");
            Utility.updateSavedPrefs(this, Constants.UTILITY_MAINACTIVITY, "", 4, null, alertID, null);
        } else {
            if (!customName.equals(Objects.requireNonNull(alert).getName())) {
                alert.setCustomName(customName);
                Utility.updateSavedPrefs(this, Constants.UTILITY_MAINACTIVITY, customName, 4, null, alertID, null);
                Snackbar.make(findViewById(R.id.main_coordinator), "Custom name set", Snackbar.LENGTH_SHORT).show();
            } else {
                if (!alert.getCustomName().isEmpty()) {
                    Snackbar.make(findViewById(R.id.main_coordinator), "Custom name removed", Snackbar.LENGTH_SHORT).show();
                }
                alert.setCustomName("");
                Utility.updateSavedPrefs(this, Constants.UTILITY_MAINACTIVITY, "", 4, null, alertID, null);
            }
        }
        refreshLocalAlertList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshLocalAlertList() {
        if (!PreferenceUtils.getFacebookLogin(this)) {
            for (int i = 0; i < alertList.size(); i++) {
                if (!alertList.get(i).hasKijiji() && !alertList.get(i).hasEbay() && !alertList.get(i).hasCraigslist()) {
                    alertList.remove(i);
                    if (alertList.isEmpty()) {
                        guideText.setText(R.string.createNewAlert);
                    }
                    i--;
                }
            }
        }
        ArrayList<Alert> freshList = PreferenceUtils.getAlertList(this);
        alertList.clear();
        alertList.addAll(freshList);
        recAdapter.notifyDataSetChanged();
        if (newListingsExist()) {
            newListingsButton.setVisibility(View.VISIBLE);
        } else {
            newListingsButton.setVisibility(View.GONE);
        }
        ((NotificationManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))).cancel(2);
    }

    private boolean newListingsExist() {
        for (Alert alert : alertList) {
            if (!alert.getOnlyNewBgListings().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        InAppUpdate.checkInAppUpdate(this, this);
        Utility.updateSavedPrefs(this, Constants.UTILITY_MAINACTIVITY, null, 1, null, 0, null);
        refreshLocalAlertList();
        InAppUpdate.activityOnResume(this, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                if (PendingIntent.getBroadcast(this, 1, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) == null) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, PreferenceUtils.getCheckTime(this), pendingIntent);
                }
            }
        }
    }

}

package com.simonits.adalerts.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.CookieManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.material.snackbar.Snackbar;
import com.simonits.adalerts.R;
import com.simonits.adalerts.dialogs.ConfirmDialog;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.scroll_in_left, R.anim.scroll_out_right);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SwitchPreference dataFetch;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            dataFetch = Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_data_fetch)));
            dataFetch.setOnPreferenceChangeListener((preference, newVal) -> {
                final boolean value = (Boolean) newVal;
                if (value) {
                    Utility.setCheckAlarm(requireActivity().getApplicationContext(), false);
                } else {
                    Utility.cancelDataFetchAlarm(requireActivity().getApplicationContext());
                }
                refreshFrequencySettings(value);
                return true;
            });

            if (!PreferenceUtils.getFacebookLogin(getActivity())) {
                Preference facebookLabel = Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_facebook_label)));
                facebookLabel.setVisible(false);
            }
            Preference facebookButton = Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_facebook_button)));
            facebookButton.setOnPreferenceClickListener(preference -> {
                ConfirmDialog dialog = new ConfirmDialog(getActivity(), null, "FacebookLogOut fragment");
                requireActivity().getSupportFragmentManager().setFragmentResultListener(Constants.KEY_FRAGMENTREQUEST, dialog, (requestKey, result) -> {
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                    Utility.removeAllFacebook(getActivity());
                    PreferenceUtils.saveFacebookLogin(false, getActivity());
                    Preference facebookLabel = Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_facebook_label)));
                    facebookLabel.setVisible(false);
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), "Logged out of Facebook", Snackbar.LENGTH_SHORT).show();
                });
                dialog.show(requireActivity().getSupportFragmentManager(), "confirm dialog");
                return true;
            });

            setUpFrequencyPreference(Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_wifi_preference))), Constants.KEY_SETTING_WIFIFREQUENCY, "Frequency on WiFi");
            setUpFrequencyPreference(Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_mobile_preference))), Constants.KEY_SETTING_MOBILEFREQUENCY, "Frequency on Mobile Data");
            setUpFrequencyPreference(Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_dnd_preference))), Constants.KEY_SETTING_DNDFREQUENCY, "Frequency during Do Not Disturb");
            setUpFrequencyPreference(Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_charging_preference))), Constants.KEY_SETTING_CHARGEFREQUENCY, "Frequency when Charging");
            setUpFrequencyPreference(Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_battery_preference))), Constants.KEY_SETTING_BATTERYFREQUENCY, "Frequency during Battery Saver");

            Preference alertConfigButton = Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_alertconfig_button)));
            alertConfigButton.setOnPreferenceClickListener(preference -> {
                ConfirmDialog dialog = new ConfirmDialog(getActivity(), null, Constants.DIALOGTYPE_IMPORTEXPORT_ALERTCONFIG);
                requireActivity().getSupportFragmentManager().setFragmentResultListener(Constants.KEY_FRAGMENTREQUEST, dialog, (requestKey, result) -> Snackbar.make(requireActivity().findViewById(android.R.id.content), "Alert config saved", Snackbar.LENGTH_SHORT).show());
                dialog.show(requireActivity().getSupportFragmentManager(), "confirm dialog");
                return true;
            });

            Preference favouritesButton = Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_favourites_button)));
            favouritesButton.setOnPreferenceClickListener(preference -> {
                ConfirmDialog dialog = new ConfirmDialog(getActivity(), null, Constants.DIALOGTYPE_IMPORTEXPORT_FAVOURITES);
                requireActivity().getSupportFragmentManager().setFragmentResultListener(Constants.KEY_FRAGMENTREQUEST, dialog, (requestKey, result) -> Snackbar.make(requireActivity().findViewById(android.R.id.content), "Favourites saved", Snackbar.LENGTH_SHORT).show());
                dialog.show(requireActivity().getSupportFragmentManager(), "confirm dialog");
                return true;
            });
        }

        private void setUpFrequencyPreference(Preference preference, String frequencyKey, String title) {
            preference.setOnPreferenceClickListener(preference1 -> {
                ConfirmDialog dialog = new ConfirmDialog(getActivity(), null, title, frequencyKey, Constants.DIALOGTYPE_FREQUENCY);
                requireActivity().getSupportFragmentManager().setFragmentResultListener(Constants.KEY_FRAGMENTREQUEST, dialog, (requestKey, result) -> refreshFrequencySettings(true));
                dialog.show(requireActivity().getSupportFragmentManager(), "confirm dialog");
                return true;
            });
        }

        private void refreshFrequencySettings(boolean dataFetch) {
            ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_wifi_preference)))).setSummary(PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_WIFIFREQUENCY, getActivity()));
            ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_mobile_preference)))).setSummary(PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_MOBILEFREQUENCY, getActivity()));
            ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_dnd_preference)))).setSummary(PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_DNDFREQUENCY, getActivity()));
            ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_charging_preference)))).setSummary(PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_CHARGEFREQUENCY, getActivity()));
            ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_battery_preference)))).setSummary(PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_BATTERYFREQUENCY, getActivity()));

            String frequencyKey = Utility.getFrequencyKey(requireActivity()); // which one do I use?
            if (dataFetch && !frequencyKey.equals(Constants.NO_FREQUENCY_KEY)) {
                String targetTime = " @ ";
                String frequency = PreferenceUtils.optionGetFrequency(frequencyKey, getActivity());
                if (frequency.equals(Constants.FREQUENCY_NEVER)) {
                    targetTime = targetTime + Constants.FREQUENCY_NEVER;
                } else {
                    Calendar calendar = Calendar.getInstance();
                    long currentTime = calendar.getTimeInMillis();
                    calendar.setTimeInMillis(PreferenceUtils.getPreviousFetchTime(getActivity()) + Utility.getMilliseconds(frequency));
                    if (calendar.getTimeInMillis() <= currentTime) {
                        targetTime = targetTime + "ASAP";
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a dd/M/yyyy", Locale.getDefault());
                        targetTime = targetTime + sdf.format(calendar.getTime());
                    }
                }
                switch (frequencyKey) {
                    case Constants.KEY_SETTING_WIFIFREQUENCY:
                        ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_wifi_preference)))).setSummary(frequency + targetTime);
                        break;
                    case Constants.KEY_SETTING_MOBILEFREQUENCY:
                        ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_mobile_preference)))).setSummary(frequency + targetTime);
                        break;
                    case Constants.KEY_SETTING_DNDFREQUENCY:
                        ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_dnd_preference)))).setSummary(frequency + targetTime);
                        break;
                    case Constants.KEY_SETTING_CHARGEFREQUENCY:
                        ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_charging_preference)))).setSummary(frequency + targetTime);
                        break;
                    case Constants.KEY_SETTING_BATTERYFREQUENCY:
                        ((Preference) Objects.requireNonNull(findPreference(requireActivity().getResources().getString(R.string.settings_battery_preference)))).setSummary(frequency + targetTime);
                        break;
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            refreshFrequencySettings(dataFetch.isChecked());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.scroll_in_left, R.anim.scroll_out_right);
        }

        return super.onOptionsItemSelected(item);
    }

}
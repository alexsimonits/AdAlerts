package com.simonits.adalerts.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.simonits.adalerts.R;
import com.simonits.adalerts.dialogs.ConfirmDialog;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.SiteParam;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.util.Objects;
import java.util.Stack;

public class AlertActivity extends AppCompatActivity {

    private Boolean spinnerTouched;
    private EditText searchText;
    private String customName;

    private SwitchCompat kSwitch;
    private TextView kLocationLabel;
    private Spinner[] kLocSpinners;
    private EditText kLocKm, kLocPostalZip;
    private TextView kKmLabel;
    private TextView kCatLabel;
    private Spinner[] kCatSpinners;
    private TextView kPriceLabel;
    private Spinner kPriceSpinner;
    private EditText kPriceFrom, kPriceTo;
    private TextView kDash;
    private TextView kTypeLabel;
    private Spinner kTypeSpinner;
    private TextView kNameLabel, kIncludeLabel, kIncludeAnyLabel, kExcludeLabel;
    private SwitchCompat kIncludeSwitch, kIncludeAnySwitch, kExcludeSwitch;
    private EditText kIncludeText, kIncludeAnyText, kExcludeText;
    private View[] kDividers;

    private SwitchCompat eSwitch;
    private TextView eLocationLabel;
    private Spinner eLocSpinner;
    private EditText eLocKm, eLocPostalZip;
    private TextView eKmLabel;
    private TextView eCatLabel;
    private Spinner[] eCatSpinners;
    private TextView ePriceLabel;
    private Spinner ePriceSpinner;
    private EditText ePriceFrom, ePriceTo;
    private TextView eDash;
    private TextView eTypeLabel;
    private Spinner eTypeSpinner;
    private TextView eNameLabel, eIncludeLabel, eIncludeAnyLabel, eExcludeLabel;
    private SwitchCompat eIncludeSwitch, eIncludeAnySwitch, eExcludeSwitch;
    private EditText eIncludeText, eIncludeAnyText, eExcludeText;
    private View[] eDividers;

    private SwitchCompat cSwitch;
    private TextView cLocationLabel;
    private Spinner[] cLocSpinners;
    private TextView cPriceLabel;
    private Spinner cPriceSpinner;
    private EditText cPriceFrom, cPriceTo;
    private TextView cDash;
    private TextView cCatLabel;
    private Spinner[] cCatSpinners;
    private TextView cNameLabel, cIncludeLabel, cIncludeAnyLabel, cExcludeLabel;
    private SwitchCompat cIncludeSwitch, cIncludeAnySwitch, cExcludeSwitch;
    private EditText cIncludeText, cIncludeAnyText, cExcludeText;
    private View[] cDividers;

    private SwitchCompat fSwitch;
    private Button fLoginButton;
    private TextView fLoginLabel;
    private TextView fLocationLabel;
    private TextView fLocationText;
    private TextView fCatLabel;
    private Spinner[] fCatSpinners;
    private TextView fPriceLabel;
    private Spinner fPriceSpinner;
    private EditText fPriceFrom, fPriceTo;
    private TextView fDash;
    private TextView fNameLabel, fIncludeLabel, fIncludeAnyLabel, fExcludeLabel;
    private SwitchCompat fIncludeSwitch, fIncludeAnySwitch, fExcludeSwitch;
    private EditText fIncludeText, fIncludeAnyText, fExcludeText;
    private View[] fDividers;

    private String kLoc, kCat, kPrice, kType;
    private SiteParam[] kLocObjArray, kCatObjArray;
    private String eLoc, eCat, ePrice, eType;
    private SiteParam[] eCatObjArray;
    private String cLoc, cPrice, cCat;
    private SiteParam[] cLocObjArray, cCatObjArray;
    private String fCat, fPrice;
    private SiteParam[] fCatObjArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        Gson gson = new Gson();
        kLocObjArray = gson.fromJson(mFirebaseRemoteConfig.getString("kLocations1_17"), SiteParam[].class);
        cLocObjArray = gson.fromJson(mFirebaseRemoteConfig.getString("cLocations"), SiteParam[].class);
        kCatObjArray = gson.fromJson(mFirebaseRemoteConfig.getString("kCategories0_64"), SiteParam[].class);
        eCatObjArray = gson.fromJson(mFirebaseRemoteConfig.getString("eCategories"), SiteParam[].class);
        fCatObjArray = gson.fromJson(mFirebaseRemoteConfig.getString("fCategories1_3_4"), SiteParam[].class);
        cCatObjArray = gson.fromJson(mFirebaseRemoteConfig.getString("cCategories0_64"), SiteParam[].class);

        searchText = findViewById(R.id.search_text);

        spinnerTouched = false;

        kSwitch = findViewById(R.id.switch_kijiji);
        kLocationLabel = findViewById(R.id.kLocationLabel);
        kLocSpinners = new Spinner[]{findViewById(R.id.kSpinner9), findViewById(R.id.kSpinner1), findViewById(R.id.kSpinner2), findViewById(R.id.kSpinner3)};
        kLocKm = findViewById(R.id.kKm);
        kLocPostalZip = findViewById(R.id.kPostalZip);
        kKmLabel = findViewById(R.id.kKmLabel);
        kCatLabel = findViewById(R.id.kCatLabel);
        kCatSpinners = new Spinner[]{findViewById(R.id.kSpinner5), findViewById(R.id.kSpinner6), findViewById(R.id.kSpinner7)};
        kPriceLabel = findViewById(R.id.kPriceLabel);
        kPriceSpinner = findViewById(R.id.kSpinner4);
        kPriceFrom = findViewById(R.id.kFrom);
        kPriceTo = findViewById(R.id.kTo);
        kDash = findViewById(R.id.kDash);
        kTypeLabel = findViewById(R.id.kTypeLabel);
        kTypeSpinner = findViewById(R.id.kSpinner8);
        kNameLabel = findViewById(R.id.kNameLabel);
        kIncludeLabel = findViewById(R.id.kIncludeLabel);
        kIncludeAnyLabel = findViewById(R.id.kIncludeAnyLabel);
        kExcludeLabel = findViewById(R.id.kExcludeLabel);
        kIncludeSwitch = findViewById(R.id.kIncludeSwitch);
        kIncludeAnySwitch = findViewById(R.id.kIncludeAnySwitch);
        kExcludeSwitch = findViewById(R.id.kExcludeSwitch);
        kIncludeText = findViewById(R.id.kIncludeText);
        kIncludeAnyText = findViewById(R.id.kIncludeAnyText);
        kExcludeText = findViewById(R.id.kExcludeText);
        kDividers = new View[]{findViewById(R.id.kDivider1), findViewById(R.id.kDivider2), findViewById(R.id.kDivider3), findViewById(R.id.kDivider4), findViewById(R.id.kDivider5), findViewById(R.id.kDivider6), findViewById(R.id.kDivider7), findViewById(R.id.kDivider8), findViewById(R.id.kDivider9), findViewById(R.id.kDivider10)};

        eSwitch = findViewById(R.id.switch_ebay);
        eLocationLabel = findViewById(R.id.eLocationLabel);
        eLocSpinner = findViewById(R.id.eSpinner1);
        eLocKm = findViewById(R.id.eKm);
        eLocPostalZip = findViewById(R.id.ePostalZip);
        eKmLabel = findViewById(R.id.eKmLabel);
        eCatLabel = findViewById(R.id.eCatLabel);
        eCatSpinners = new Spinner[]{findViewById(R.id.eSpinner4), findViewById(R.id.eSpinner5), findViewById(R.id.eSpinner6)};
        ePriceLabel = findViewById(R.id.ePriceLabel);
        ePriceSpinner = findViewById(R.id.eSpinner2);
        ePriceFrom = findViewById(R.id.eFrom);
        ePriceTo = findViewById(R.id.eTo);
        eDash = findViewById(R.id.eDash);
        eTypeLabel = findViewById(R.id.eTypeLabel);
        eTypeSpinner = findViewById(R.id.eSpinner3);
        eNameLabel = findViewById(R.id.eNameLabel);
        eIncludeLabel = findViewById(R.id.eIncludeLabel);
        eIncludeAnyLabel = findViewById(R.id.eIncludeAnyLabel);
        eExcludeLabel = findViewById(R.id.eExcludeLabel);
        eIncludeSwitch = findViewById(R.id.eIncludeSwitch);
        eIncludeAnySwitch = findViewById(R.id.eIncludeAnySwitch);
        eExcludeSwitch = findViewById(R.id.eExcludeSwitch);
        eIncludeText = findViewById(R.id.eIncludeText);
        eIncludeAnyText = findViewById(R.id.eIncludeAnyText);
        eExcludeText = findViewById(R.id.eExcludeText);
        eDividers = new View[]{findViewById(R.id.eDivider1), findViewById(R.id.eDivider2), findViewById(R.id.eDivider3), findViewById(R.id.eDivider4), findViewById(R.id.eDivider5), findViewById(R.id.eDivider6), findViewById(R.id.eDivider7), findViewById(R.id.eDivider8), findViewById(R.id.eDivider9), findViewById(R.id.eDivider10)};

        cSwitch = findViewById(R.id.switch_craigslist);
        cLocationLabel = findViewById(R.id.cLocationLabel);
        cLocSpinners = new Spinner[]{findViewById(R.id.cSpinner1), findViewById(R.id.cSpinner2)};
        cPriceLabel = findViewById(R.id.cPriceLabel);
        cPriceSpinner = findViewById(R.id.cSpinner3);
        cPriceFrom = findViewById(R.id.cFrom);
        cPriceTo = findViewById(R.id.cTo);
        cDash = findViewById(R.id.cDash);
        cCatLabel = findViewById(R.id.cCatLabel);
        cCatSpinners = new Spinner[]{findViewById(R.id.cSpinner4), findViewById(R.id.cSpinner5)};
        cNameLabel = findViewById(R.id.cNameLabel);
        cIncludeLabel = findViewById(R.id.cIncludeLabel);
        cIncludeAnyLabel = findViewById(R.id.cIncludeAnyLabel);
        cExcludeLabel = findViewById(R.id.cExcludeLabel);
        cIncludeSwitch = findViewById(R.id.cIncludeSwitch);
        cIncludeAnySwitch = findViewById(R.id.cIncludeAnySwitch);
        cExcludeSwitch = findViewById(R.id.cExcludeSwitch);
        cIncludeText = findViewById(R.id.cIncludeText);
        cIncludeAnyText = findViewById(R.id.cIncludeAnyText);
        cExcludeText = findViewById(R.id.cExcludeText);
        cDividers = new View[]{findViewById(R.id.cDivider1), findViewById(R.id.cDivider2), findViewById(R.id.cDivider3), findViewById(R.id.cDivider4), findViewById(R.id.cDivider5), findViewById(R.id.cDivider6), findViewById(R.id.cDivider7), findViewById(R.id.cDivider8)};

        fSwitch = findViewById(R.id.switch_facebook);
        fLoginButton = findViewById(R.id.fLogInButton);
        fLoginLabel = findViewById(R.id.fLogInLabel);
        fLocationLabel = findViewById(R.id.fLocationLabel);
        fLocationText = findViewById(R.id.fLocationText);
        fCatLabel = findViewById(R.id.fCatLabel);
        fCatSpinners = new Spinner[]{findViewById(R.id.fSpinner3), findViewById(R.id.fSpinner4)};
        fPriceLabel = findViewById(R.id.fPriceLabel);
        fPriceSpinner = findViewById(R.id.fSpinner2);
        fPriceFrom = findViewById(R.id.fFrom);
        fPriceTo = findViewById(R.id.fTo);
        fDash = findViewById(R.id.fDash);
        fNameLabel = findViewById(R.id.fNameLabel);
        fIncludeLabel = findViewById(R.id.fIncludeLabel);
        fIncludeAnyLabel = findViewById(R.id.fIncludeAnyLabel);
        fExcludeLabel = findViewById(R.id.fExcludeLabel);
        fIncludeSwitch = findViewById(R.id.fIncludeSwitch);
        fIncludeAnySwitch = findViewById(R.id.fIncludeAnySwitch);
        fExcludeSwitch = findViewById(R.id.fExcludeSwitch);
        fIncludeText = findViewById(R.id.fIncludeText);
        fIncludeAnyText = findViewById(R.id.fIncludeAnyText);
        fExcludeText = findViewById(R.id.fExcludeText);
        fDividers = new View[]{findViewById(R.id.fDivider1), findViewById(R.id.fDivider2), findViewById(R.id.fDivider3), findViewById(R.id.fDivider4), findViewById(R.id.fDivider5), findViewById(R.id.fDivider6), findViewById(R.id.fDivider7), findViewById(R.id.fDivider8)};

        int alertID = getIntent().getIntExtra("alertId", -1);
        if (alertID == -1) {
            setTitle("Create Alert");
            searchText.requestFocus();
            kLoc = kLocObjArray[0].getName();
            kCat = kCatObjArray[0].getName();
            kPrice = "Any";
            kType = "All Listings";
            eLoc = "Canada Only";
            eCat = eCatObjArray[0].getName();
            ePrice = "Any";
            eType = "All Listings";
            cPrice = "Any";
            cLoc = cLocObjArray[0].getName();
            cCat = cCatObjArray[0].getName();
            fCat = fCatObjArray[0].getName();
            fPrice = "Any";
        } else {
            Alert alert = Utility.getAlertFromList(PreferenceUtils.getAlertList(this), alertID);
            if (Objects.requireNonNull(alert).hasFacebook() && alert.getName().equals(alert.getFCat()) || alert.hasKijiji() && alert.getName().equals(alert.getKCat()) || alert.hasCraigslist() && alert.getName().equals(alert.getCCat())) {
                searchText.setText("");
            } else {
                searchText.setText(alert.getName());
            }
            customName = alert.getCustomName();
            if (customName != null) {
                setTitle(customName);
            } else {
                setTitle("Edit Alert");
            }

            kSwitch.setChecked(alert.hasKijiji());
            kLoc = alert.getKLoc();
            kLocKm.setText(alert.getKKm());
            kLocPostalZip.setText(alert.getKPostalZip());
            kCat = alert.getKCat();
            kPrice = alert.getKPrice();
            kPriceFrom.setText(alert.getKFrom());
            kPriceTo.setText(alert.getKTo());
            kType = alert.getKType();
            kIncludeText.setText(alert.getKInclude());
            kIncludeAnyText.setText(alert.getKIncludeAny());
            kExcludeText.setText(alert.getKExclude());

            eSwitch.setChecked(alert.hasEbay());
            eLoc = alert.getELoc();
            eLocKm.setText(alert.getEKm());
            eLocPostalZip.setText(alert.getEPostalZip());
            eCat = alert.getECat();
            ePrice = alert.getEPrice();
            ePriceFrom.setText(alert.getEFrom());
            ePriceTo.setText(alert.getETo());
            eType = alert.getEType();
            eIncludeText.setText(alert.getEInclude());
            eIncludeAnyText.setText(alert.getEIncludeAny());
            eExcludeText.setText(alert.getEExclude());

            cSwitch.setChecked(alert.hasCraigslist());
            cLoc = alert.getCLoc();
            cPrice = alert.getCPrice();
            cPriceFrom.setText(alert.getCFrom());
            cPriceTo.setText(alert.getCTo());
            cCat = alert.getCCat();
            cIncludeText.setText(alert.getCInclude());
            cIncludeAnyText.setText(alert.getCIncludeAny());
            cExcludeText.setText(alert.getCExclude());

            fSwitch.setChecked(alert.hasFacebook());
            fCat = alert.getFCat();
            fPrice = alert.getFPrice();
            fPriceFrom.setText(alert.getFFrom());
            fPriceTo.setText(alert.getFTo());
            fIncludeText.setText(alert.getFInclude());
            fIncludeAnyText.setText(alert.getFIncludeAny());
            fExcludeText.setText(alert.getFExclude());

            if (kSwitch.isChecked()) {
                onKSwitchChecked();
            }
            if (eSwitch.isChecked()) {
                onESwitchChecked();
            }
            if (cSwitch.isChecked()) {
                onCSwitchChecked();
            }
            if (fSwitch.isChecked()) {
                onFSwitchChecked();
            }
        }

        kSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (kSwitch.isChecked()) {
                onKSwitchChecked();
            } else {
                kLocationLabel.setVisibility(View.GONE);
                for (Spinner spinner : kLocSpinners) {
                    spinner.setVisibility(View.GONE);
                }
                kLocKm.setVisibility(View.GONE);
                kLocPostalZip.setVisibility(View.GONE);
                kKmLabel.setVisibility(View.GONE);
                kCatLabel.setVisibility(View.GONE);
                for (Spinner spinner : kCatSpinners) {
                    spinner.setVisibility(View.GONE);
                }
                kPriceLabel.setVisibility(View.GONE);
                kPriceSpinner.setVisibility(View.GONE);
                kPriceFrom.setVisibility(View.GONE);
                kPriceTo.setVisibility(View.GONE);
                kDash.setVisibility(View.GONE);
                kTypeLabel.setVisibility(View.GONE);
                kTypeSpinner.setVisibility(View.GONE);
                kNameLabel.setVisibility(View.GONE);
                kIncludeLabel.setVisibility(View.GONE);
                kIncludeAnyLabel.setVisibility(View.GONE);
                kExcludeLabel.setVisibility(View.GONE);
                kIncludeSwitch.setVisibility(View.GONE);
                kIncludeAnySwitch.setVisibility(View.GONE);
                kExcludeSwitch.setVisibility(View.GONE);
                kIncludeText.setVisibility(View.GONE);
                kIncludeAnyText.setVisibility(View.GONE);
                kExcludeText.setVisibility(View.GONE);
                for (View view : kDividers) {
                    view.setVisibility(View.GONE);
                }
            }
        });

        eSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (eSwitch.isChecked()) {
                onESwitchChecked();
            } else {
                eLocationLabel.setVisibility(View.GONE);
                eLocSpinner.setVisibility(View.GONE);
                eLocKm.setVisibility(View.GONE);
                eLocPostalZip.setVisibility(View.GONE);
                eKmLabel.setVisibility(View.GONE);
                eCatLabel.setVisibility(View.GONE);
                for (Spinner spinner : eCatSpinners) {
                    spinner.setVisibility(View.GONE);
                }
                ePriceLabel.setVisibility(View.GONE);
                ePriceSpinner.setVisibility(View.GONE);
                ePriceFrom.setVisibility(View.GONE);
                ePriceTo.setVisibility(View.GONE);
                eDash.setVisibility(View.GONE);
                eTypeLabel.setVisibility(View.GONE);
                eTypeSpinner.setVisibility(View.GONE);
                eNameLabel.setVisibility(View.GONE);
                eIncludeLabel.setVisibility(View.GONE);
                eIncludeAnyLabel.setVisibility(View.GONE);
                eExcludeLabel.setVisibility(View.GONE);
                eIncludeSwitch.setVisibility(View.GONE);
                eIncludeAnySwitch.setVisibility(View.GONE);
                eExcludeSwitch.setVisibility(View.GONE);
                eIncludeText.setVisibility(View.GONE);
                eIncludeAnyText.setVisibility(View.GONE);
                eExcludeText.setVisibility(View.GONE);
                for (View view : eDividers) {
                    view.setVisibility(View.GONE);
                }
            }
        });

        cSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cSwitch.isChecked()) {
                onCSwitchChecked();
            } else {
                cLocationLabel.setVisibility(View.GONE);
                for (Spinner spinner : cLocSpinners) {
                    spinner.setVisibility(View.GONE);
                }
                cPriceLabel.setVisibility(View.GONE);
                cPriceSpinner.setVisibility(View.GONE);
                cPriceFrom.setVisibility(View.GONE);
                cPriceTo.setVisibility(View.GONE);
                cDash.setVisibility(View.GONE);
                cCatLabel.setVisibility(View.GONE);
                for (Spinner spinner : cCatSpinners) {
                    spinner.setVisibility(View.GONE);
                }
                cNameLabel.setVisibility(View.GONE);
                cIncludeLabel.setVisibility(View.GONE);
                cIncludeAnyLabel.setVisibility(View.GONE);
                cExcludeLabel.setVisibility(View.GONE);
                cIncludeSwitch.setVisibility(View.GONE);
                cIncludeAnySwitch.setVisibility(View.GONE);
                cExcludeSwitch.setVisibility(View.GONE);
                cIncludeText.setVisibility(View.GONE);
                cIncludeAnyText.setVisibility(View.GONE);
                cExcludeText.setVisibility(View.GONE);
                for (View view : cDividers) {
                    view.setVisibility(View.GONE);
                }
            }
        });

        fSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (fSwitch.isChecked()) {
                if (Utility.getFacebookAlertsNum(AlertActivity.this) >= 8) {
                    ConfirmDialog dialog = new ConfirmDialog(AlertActivity.this, "You have too many Facebook alerts. The more Facebook alerts you have, the worse this app performs. Please remove an existing Facebook alert before adding another.", "Too Many Facebook Alerts", Constants.DIALOGTYPE_NOTICE);
                    dialog.show(getSupportFragmentManager(), "confirm dialog");
                    fSwitch.setChecked(false);
                } else {
                    onFSwitchChecked();
                }
            } else {
                fLoginButton.setVisibility(View.GONE);
                fLoginLabel.setVisibility(View.GONE);
                fLocationLabel.setVisibility(View.GONE);
                fLocationText.setVisibility(View.GONE);
                fCatLabel.setVisibility(View.GONE);
                for (Spinner spinner : fCatSpinners) {
                    spinner.setVisibility(View.GONE);
                }
                fPriceLabel.setVisibility(View.GONE);
                fPriceSpinner.setVisibility(View.GONE);
                fPriceFrom.setVisibility(View.GONE);
                fPriceTo.setVisibility(View.GONE);
                fDash.setVisibility(View.GONE);
                fNameLabel.setVisibility(View.GONE);
                fIncludeLabel.setVisibility(View.GONE);
                fIncludeAnyLabel.setVisibility(View.GONE);
                fExcludeLabel.setVisibility(View.GONE);
                fIncludeSwitch.setVisibility(View.GONE);
                fIncludeAnySwitch.setVisibility(View.GONE);
                fExcludeSwitch.setVisibility(View.GONE);
                fIncludeText.setVisibility(View.GONE);
                fIncludeAnyText.setVisibility(View.GONE);
                fExcludeText.setVisibility(View.GONE);
                for (View view : fDividers) {
                    view.setVisibility(View.GONE);
                }
            }
        });

        for (Spinner spinner : kLocSpinners) {
            setupSpinner("kLoc", spinner);
        }
        for (Spinner spinner : cLocSpinners) {
            setupSpinner("cLoc", spinner);
        }
        for (Spinner spinner : kCatSpinners) {
            setupSpinner("kCat", spinner);
        }
        for (Spinner spinner : eCatSpinners) {
            setupSpinner("eCat", spinner);
        }
        for (Spinner spinner : fCatSpinners) {
            setupSpinner("fCat", spinner);
        }
        for (Spinner spinner : cCatSpinners) {
            setupSpinner("cCat", spinner);
        }
        setupSpinner("kPrice", kPriceSpinner);
        setupSpinner("ePrice", ePriceSpinner);
        setupSpinner("cPrice", cPriceSpinner);
        setupSpinner("fPrice", fPriceSpinner);
        setupSpinner("eType", eTypeSpinner);
        setupSpinner("kType", kTypeSpinner);

        setupNameSwitch(kIncludeSwitch, kIncludeText);
        setupNameSwitch(kIncludeAnySwitch, kIncludeAnyText);
        setupNameSwitch(kExcludeSwitch, kExcludeText);
        setupNameSwitch(eIncludeSwitch, eIncludeText);
        setupNameSwitch(eIncludeAnySwitch, eIncludeAnyText);
        setupNameSwitch(eExcludeSwitch, eExcludeText);
        setupNameSwitch(cIncludeSwitch, cIncludeText);
        setupNameSwitch(cIncludeAnySwitch, cIncludeAnyText);
        setupNameSwitch(cExcludeSwitch, cExcludeText);
        setupNameSwitch(fIncludeSwitch, fIncludeText);
        setupNameSwitch(fIncludeAnySwitch, fIncludeAnyText);
        setupNameSwitch(fExcludeSwitch, fExcludeText);

        spinnerSetOnItemSelectedListener(eLocSpinner);
        eLocSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerTouched) {
                    spinnerTouched = false;
                    eLoc = parent.getItemAtPosition(position).toString();
                    refreshEbayLocation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        fLoginButton.setOnClickListener(view -> {
            Intent intent = new Intent(AlertActivity.this, FacebookActivity.class);
            facebookLoginResultLauncher.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.scale_out);
        });

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.nothing, R.anim.scale_out);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    ActivityResultLauncher<Intent> facebookLoginResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    PreferenceUtils.saveFacebookLogin(true, AlertActivity.this);
                    Snackbar.make(findViewById(android.R.id.content), "Logged in to Facebook", Snackbar.LENGTH_SHORT).show();
                    onFSwitchChecked();
                }
            });

    private void setupNameSwitch(SwitchCompat includeSwitch, EditText includeText) {
        includeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (includeSwitch.isChecked()) {
                includeText.setVisibility(View.VISIBLE);
            } else {
                includeText.setVisibility(View.GONE);
                includeText.setText("");
            }
        });
    }

    private void setupSpinner(String param, Spinner spinner) {
        spinnerSetOnItemSelectedListener(spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerTouched) {
                    spinnerTouched = false;
                    switch (param) {
                        case "kLoc":
                            kLoc = refreshSpinners(parent.getItemAtPosition(position).toString(), kLocObjArray, kLocSpinners);
                            refreshSpecificLocation(kLoc, kLocKm, kLocPostalZip, kKmLabel);
                            break;
                        case "cLoc":
                            cLoc = refreshSpinners(parent.getItemAtPosition(position).toString(), cLocObjArray, cLocSpinners);
                            break;
                        case "kCat":
                            kCat = refreshSpinners(parent.getItemAtPosition(position).toString(), kCatObjArray, kCatSpinners);
                            break;
                        case "eCat":
                            eCat = refreshSpinners(parent.getItemAtPosition(position).toString(), eCatObjArray, eCatSpinners);
                            break;
                        case "fCat":
                            fCat = refreshSpinners(parent.getItemAtPosition(position).toString(), fCatObjArray, fCatSpinners);
                            break;
                        case "cCat":
                            cCat = refreshSpinners(parent.getItemAtPosition(position).toString(), cCatObjArray, cCatSpinners);
                            break;
                        case "kPrice":
                            kPrice = parent.getItemAtPosition(position).toString();
                            refreshPrice(kPriceSpinner, kPrice, kPriceFrom, kPriceTo, kDash);
                            break;
                        case "ePrice":
                            ePrice = parent.getItemAtPosition(position).toString();
                            refreshPrice(ePriceSpinner, ePrice, ePriceFrom, ePriceTo, eDash);
                            break;
                        case "cPrice":
                            cPrice = parent.getItemAtPosition(position).toString();
                            refreshPrice(cPriceSpinner, cPrice, cPriceFrom, cPriceTo, cDash);
                            break;
                        case "fPrice":
                            fPrice = parent.getItemAtPosition(position).toString();
                            refreshPrice(fPriceSpinner, fPrice, fPriceFrom, fPriceTo, fDash);
                            break;
                        case "eType":
                            eType = parent.getItemAtPosition(position).toString();
                            refreshType(eTypeSpinner, eType);
                            break;
                        case "kType":
                            kType = parent.getItemAtPosition(position).toString();
                            refreshType(kTypeSpinner, kType);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initializeSpinner(Stack<SiteParam> aStack, SiteParam[] objArray, Spinner spinner) {
        String[] strArray = new String[objArray.length];
        for (int i = 0; i < strArray.length; i++) {
            strArray[i] = objArray[i].getName();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(arrayAdapter);
        SiteParam obj = aStack.peek();
        spinner.setSelection(arrayAdapter.getPosition(obj.getName()));
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_alert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_confirm) {
            if (!searchText.getText().toString().isEmpty() && (kSwitch.isChecked() || eSwitch.isChecked() || cSwitch.isChecked() || fSwitch.isChecked())
                    || searchText.getText().toString().isEmpty() && fSwitch.isChecked() && !fCat.equals("All Categories") && !kSwitch.isChecked() && !eSwitch.isChecked() && !cSwitch.isChecked()
                    || searchText.getText().toString().isEmpty() && kSwitch.isChecked() && !kCat.equals("Auto-Detect") && !fSwitch.isChecked() && !eSwitch.isChecked() && !cSwitch.isChecked()
                    || searchText.getText().toString().isEmpty() && cSwitch.isChecked() && !kSwitch.isChecked() && !eSwitch.isChecked() && !fSwitch.isChecked()) {
                String kKm = kLocKm.getText().toString();
                String kPostalZip = kLocPostalZip.getText().toString().trim();
                kPostalZip = kPostalZip.replace(" ", "");
                String kFrom = kPriceFrom.getText().toString();
                String kTo = kPriceTo.getText().toString();
                String eKm = eLocKm.getText().toString();
                String ePostalZip = eLocPostalZip.getText().toString().trim();
                ePostalZip = ePostalZip.replace(" ", "");
                String eFrom = ePriceFrom.getText().toString();
                String eTo = ePriceTo.getText().toString();
                String cFrom = cPriceFrom.getText().toString();
                String cTo = cPriceTo.getText().toString();
                String fFrom = fPriceFrom.getText().toString();
                String fTo = fPriceTo.getText().toString();
                if (kSwitch.isChecked() && kLoc.equals("Specific Location") && (kKm.isEmpty() || Integer.parseInt(kKm) == 0 || Integer.parseInt(kKm) > 1000 || kPostalZip.length() < 3 || kPostalZip.length() > 6)) {
                    if (kKm.isEmpty()) {
                        kLocKm.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Enter a km amount for Kijiji", Snackbar.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(kKm) == 0) {
                        kLocKm.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Kijiji km can't be 0", Snackbar.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(kKm) > 1000) {
                        kLocKm.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Kijiji km can't be larger than 1000", Snackbar.LENGTH_SHORT).show();
                    } else if (kPostalZip.length() < 3) {
                        kLocPostalZip.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Kijiji Postal/Zip is too short", Snackbar.LENGTH_SHORT).show();
                    } else if (kPostalZip.length() > 6) {
                        kLocPostalZip.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Kijiji Postal/Zip is too long", Snackbar.LENGTH_SHORT).show();
                    }
                } else if (kSwitch.isChecked() && kPrice.equals("Specific Price") && kTo.isEmpty()) {
                    kPriceTo.setBackgroundColor(Color.parseColor("red"));
                    Snackbar.make(findViewById(android.R.id.content), "Enter a Max Price for Kijiji", Snackbar.LENGTH_SHORT).show();
                } else if (eSwitch.isChecked() && eLoc.equals("Specific Location") && (eKm.isEmpty() || ePostalZip.length() < 5 || ePostalZip.length() > 6)) {
                    if (eKm.isEmpty()) {
                        eLocKm.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Enter a km amount for Ebay", Snackbar.LENGTH_SHORT).show();
                    } else if (ePostalZip.length() < 5) {
                        eLocPostalZip.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Ebay Postal/Zip is too short", Snackbar.LENGTH_SHORT).show();
                    } else { //ePostalZip.length() > 6
                        eLocPostalZip.setBackgroundColor(Color.parseColor("red"));
                        Snackbar.make(findViewById(android.R.id.content), "Ebay Postal/Zip is too long", Snackbar.LENGTH_SHORT).show();
                    }
                } else if (eSwitch.isChecked() && ePrice.equals("Specific Price") && eTo.isEmpty()) {
                    ePriceTo.setBackgroundColor(Color.parseColor("red"));
                    Snackbar.make(findViewById(android.R.id.content), "Enter a Max Price for Ebay", Snackbar.LENGTH_SHORT).show();
                } else if (cSwitch.isChecked() && cPrice.equals("Specific Price") && cTo.isEmpty()) {
                    cPriceTo.setBackgroundColor(Color.parseColor("red"));
                    Snackbar.make(findViewById(android.R.id.content), "Enter a Max Price for Craigslist", Snackbar.LENGTH_SHORT).show();
                } else if (fSwitch.isChecked() && !PreferenceUtils.getFacebookLogin(this)) {
                    fLoginLabel.setTextColor(Color.parseColor("red"));
                    Snackbar.make(findViewById(android.R.id.content), "Log In required for Facebook", Snackbar.LENGTH_SHORT).show();
                } else if (fSwitch.isChecked() && fPrice.equals("Specific Price") && fTo.isEmpty()) {
                    fPriceTo.setBackgroundColor(Color.parseColor("red"));
                    Snackbar.make(findViewById(android.R.id.content), "Enter a Max Price for Facebook", Snackbar.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("search_text", searchText.getText().toString().trim().replaceAll(" +", " "));
                    intent.putExtra("custom_name", customName);
                    intent.putExtra("kSwitch", kSwitch.isChecked());
                    intent.putExtra("eSwitch", eSwitch.isChecked());
                    intent.putExtra("cSwitch", cSwitch.isChecked());
                    intent.putExtra("fSwitch", fSwitch.isChecked());
                    intent.putExtra("kLoc", kLoc);
                    intent.putExtra("kKm", kKm);
                    intent.putExtra("kPostalZip", kPostalZip);
                    intent.putExtra("kCat", kCat);
                    intent.putExtra("kPrice", kPrice);
                    if (!kFrom.isEmpty() || !kTo.isEmpty()) {
                        if (kFrom.isEmpty()) {
                            kFrom = "0";
                        } else if (kTo.isEmpty()) {
                            kTo = "0";
                        }
                        if (Integer.parseInt(kFrom) > Integer.parseInt(kTo)) {
                            String temp = kTo;
                            kTo = kFrom;
                            kFrom = temp;
                        }
                    }
                    intent.putExtra("kFrom", kFrom);
                    intent.putExtra("kTo", kTo);
                    intent.putExtra("kType", kType);
                    intent.putExtra("kInclude", keywordsFormatted(kIncludeText));
                    intent.putExtra("kIncludeAny", keywordsFormatted(kIncludeAnyText));
                    intent.putExtra("kExclude", keywordsFormatted(kExcludeText));
                    intent.putExtra("eLoc", eLoc);
                    intent.putExtra("eKm", eKm);
                    intent.putExtra("ePostalZip", ePostalZip);
                    intent.putExtra("eCat", eCat);
                    intent.putExtra("ePrice", ePrice);
                    if (!eFrom.isEmpty() || !eTo.isEmpty()) {
                        if (eFrom.isEmpty()) {
                            eFrom = "0";
                        } else if (eTo.isEmpty()) {
                            eTo = "0";
                        }
                        if (Integer.parseInt(eFrom) > Integer.parseInt(eTo)) {
                            String temp = eTo;
                            eTo = eFrom;
                            eFrom = temp;
                        }
                    }
                    intent.putExtra("eFrom", eFrom);
                    intent.putExtra("eTo", eTo);
                    intent.putExtra("eType", eType);
                    intent.putExtra("eInclude", keywordsFormatted(eIncludeText));
                    intent.putExtra("eIncludeAny", keywordsFormatted(eIncludeAnyText));
                    intent.putExtra("eExclude", keywordsFormatted(eExcludeText));
                    intent.putExtra("cLoc", cLoc);
                    intent.putExtra("cPrice", cPrice);
                    if (!cFrom.isEmpty() || !cTo.isEmpty()) {
                        if (cFrom.isEmpty()) {
                            cFrom = "0";
                        } else if (cTo.isEmpty()) {
                            cTo = "0";
                        }
                        if (Integer.parseInt(cFrom) > Integer.parseInt(cTo)) {
                            String temp = cTo;
                            cTo = cFrom;
                            cFrom = temp;
                        }
                    }
                    intent.putExtra("cFrom", cFrom);
                    intent.putExtra("cTo", cTo);
                    intent.putExtra("cCat", cCat);
                    intent.putExtra("cInclude", keywordsFormatted(cIncludeText));
                    intent.putExtra("cIncludeAny", keywordsFormatted(cIncludeAnyText));
                    intent.putExtra("cExclude", keywordsFormatted(cExcludeText));
                    intent.putExtra("fCat", fCat);
                    intent.putExtra("fPrice", fPrice);
                    if (!fFrom.isEmpty() || !fTo.isEmpty()) {
                        if (fFrom.isEmpty()) {
                            fFrom = "0";
                        } else if (fTo.isEmpty()) {
                            fTo = "0";
                        }
                        if (Integer.parseInt(fFrom) > Integer.parseInt(fTo)) {
                            String temp = fTo;
                            fTo = fFrom;
                            fFrom = temp;
                        }
                    }
                    intent.putExtra("fFrom", fFrom);
                    intent.putExtra("fTo", fTo);
                    intent.putExtra("fInclude", keywordsFormatted(fIncludeText));
                    intent.putExtra("fIncludeAny", keywordsFormatted(fIncludeAnyText));
                    intent.putExtra("fExclude", keywordsFormatted(fExcludeText));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else if (searchText.getText().toString().isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Search Term is empty", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "No search site picked", Snackbar.LENGTH_SHORT).show();
            }
            overridePendingTransition(R.anim.nothing, R.anim.scale_out);
            return true;
        }
        if (id == R.id.action_cancel) {
            finish();
            overridePendingTransition(R.anim.nothing, R.anim.scale_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static String keywordsFormatted(EditText text) {
        return text.getText().toString().trim().replaceAll(" +", " ").replaceAll(" ,", ",").replaceAll(", ", ",").replaceAll(",+", ",").replaceAll(",$|^,", "").replaceAll(",", ", ");
    }

    private void onKSwitchChecked() {
        kLocationLabel.setVisibility(View.VISIBLE);
        kLoc = refreshSpinners(kLoc, kLocObjArray, kLocSpinners);
        refreshSpecificLocation(kLoc, kLocKm, kLocPostalZip, kKmLabel);
        kCatLabel.setVisibility(View.VISIBLE);
        kCat = refreshSpinners(kCat, kCatObjArray, kCatSpinners);
        kPriceLabel.setVisibility(View.VISIBLE);
        refreshPrice(kPriceSpinner, kPrice, kPriceFrom, kPriceTo, kDash);
        kTypeLabel.setVisibility(View.VISIBLE);
        refreshType(kTypeSpinner, kType);
        kNameLabel.setVisibility(View.VISIBLE);
        kIncludeLabel.setVisibility(View.VISIBLE);
        kIncludeAnyLabel.setVisibility(View.VISIBLE);
        kExcludeLabel.setVisibility(View.VISIBLE);
        kIncludeSwitch.setVisibility(View.VISIBLE);
        kIncludeAnySwitch.setVisibility(View.VISIBLE);
        kExcludeSwitch.setVisibility(View.VISIBLE);
        refreshName(kIncludeSwitch, kIncludeText, kIncludeAnySwitch, kIncludeAnyText, kExcludeSwitch, kExcludeText);
        for (View view : kDividers) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void onESwitchChecked() {
        eLocationLabel.setVisibility(View.VISIBLE);
        refreshEbayLocation();
        eCatLabel.setVisibility(View.VISIBLE);
        eCat = refreshSpinners(eCat, eCatObjArray, eCatSpinners);
        ePriceLabel.setVisibility(View.VISIBLE);
        refreshPrice(ePriceSpinner, ePrice, ePriceFrom, ePriceTo, eDash);
        eTypeLabel.setVisibility(View.VISIBLE);
        refreshType(eTypeSpinner, eType);
        eNameLabel.setVisibility(View.VISIBLE);
        eIncludeLabel.setVisibility(View.VISIBLE);
        eIncludeAnyLabel.setVisibility(View.VISIBLE);
        eExcludeLabel.setVisibility(View.VISIBLE);
        eIncludeSwitch.setVisibility(View.VISIBLE);
        eIncludeAnySwitch.setVisibility(View.VISIBLE);
        eExcludeSwitch.setVisibility(View.VISIBLE);
        refreshName(eIncludeSwitch, eIncludeText, eIncludeAnySwitch, eIncludeAnyText, eExcludeSwitch, eExcludeText);
        for (View view : eDividers) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void onCSwitchChecked() {
        cLocationLabel.setVisibility(View.VISIBLE);
        cLoc = refreshSpinners(cLoc, cLocObjArray, cLocSpinners);
        cPriceLabel.setVisibility(View.VISIBLE);
        refreshPrice(cPriceSpinner, cPrice, cPriceFrom, cPriceTo, cDash);
        cCatLabel.setVisibility(View.VISIBLE);
        cCat = refreshSpinners(cCat, cCatObjArray, cCatSpinners);
        cNameLabel.setVisibility(View.VISIBLE);
        cIncludeLabel.setVisibility(View.VISIBLE);
        cIncludeAnyLabel.setVisibility(View.VISIBLE);
        cExcludeLabel.setVisibility(View.VISIBLE);
        cIncludeSwitch.setVisibility(View.VISIBLE);
        cIncludeAnySwitch.setVisibility(View.VISIBLE);
        cExcludeSwitch.setVisibility(View.VISIBLE);
        refreshName(cIncludeSwitch, cIncludeText, cIncludeAnySwitch, cIncludeAnyText, cExcludeSwitch, cExcludeText);
        for (View view : cDividers) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void onFSwitchChecked() {
        if (PreferenceUtils.getFacebookLogin(AlertActivity.this)) {
            fLoginButton.setVisibility(View.GONE);
            fLoginLabel.setVisibility(View.GONE);
            fLocationLabel.setVisibility(View.VISIBLE);
            fLocationText.setVisibility(View.VISIBLE);
            fCatLabel.setVisibility(View.VISIBLE);
            fCat = refreshSpinners(fCat, fCatObjArray, fCatSpinners);
            fPriceLabel.setVisibility(View.VISIBLE);
            refreshPrice(fPriceSpinner, fPrice, fPriceFrom, fPriceTo, fDash);
            fNameLabel.setVisibility(View.VISIBLE);
            fIncludeLabel.setVisibility(View.VISIBLE);
            fIncludeAnyLabel.setVisibility(View.VISIBLE);
            fExcludeLabel.setVisibility(View.VISIBLE);
            fIncludeSwitch.setVisibility(View.VISIBLE);
            fIncludeAnySwitch.setVisibility(View.VISIBLE);
            fExcludeSwitch.setVisibility(View.VISIBLE);
            refreshName(fIncludeSwitch, fIncludeText, fIncludeAnySwitch, fIncludeAnyText, fExcludeSwitch, fExcludeText);
            for (View view : fDividers) {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            fLoginButton.setVisibility(View.VISIBLE);
            fLoginLabel.setVisibility(View.VISIBLE);
        }
    }

    private void refreshName(SwitchCompat includeSwitch, EditText includeText, SwitchCompat includeAnySwitch, EditText includeAnyText, SwitchCompat excludeSwitch, EditText excludeText) {
        if (!includeText.getText().toString().trim().isEmpty()) {
            includeSwitch.setChecked(true);
            includeText.setVisibility(View.VISIBLE);
        } else {
            includeSwitch.setChecked(false);
            includeText.setVisibility(View.GONE);
        }
        if (!includeAnyText.getText().toString().trim().isEmpty()) {
            includeAnySwitch.setChecked(true);
            includeAnyText.setVisibility(View.VISIBLE);
        } else {
            includeAnySwitch.setChecked(false);
            includeAnyText.setVisibility(View.GONE);
        }
        if (!excludeText.getText().toString().trim().isEmpty()) {
            excludeSwitch.setChecked(true);
            excludeText.setVisibility(View.VISIBLE);
        } else {
            excludeSwitch.setChecked(false);
            excludeText.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void refreshType(Spinner typeSpinner, String type) {
        ArrayAdapter adapter = (ArrayAdapter) typeSpinner.getAdapter(); //cast to an ArrayAdapter
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        typeSpinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(type);
        typeSpinner.setSelection(spinnerPosition);//set the default according to value
        typeSpinner.setVisibility(View.VISIBLE);
    }

    private void refreshSpecificLocation(String loc, EditText locKm, EditText locPostalZip, TextView kmLabel) {
        if (loc.equals("Specific Location")) {
            locKm.setVisibility(View.VISIBLE);
            locPostalZip.setVisibility(View.VISIBLE);
            kmLabel.setVisibility(View.VISIBLE);
        } else {
            locKm.setVisibility(View.GONE);
            locPostalZip.setVisibility(View.GONE);
            kmLabel.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void refreshEbayLocation() {
        ArrayAdapter adapter = (ArrayAdapter) eLocSpinner.getAdapter(); //cast to an ArrayAdapter
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        eLocSpinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(eLoc);
        eLocSpinner.setSelection(spinnerPosition);//set the default according to value
        eLocSpinner.setVisibility(View.VISIBLE);
        refreshSpecificLocation(eLoc, eLocKm, eLocPostalZip, eKmLabel);
    }

    private void spinnerSetOnItemSelectedListener(Spinner spinner) {
        spinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                spinnerTouched = true;
                v.performClick();
            }
            return true;
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void refreshPrice(Spinner priceSpinner, String price, EditText priceFrom, EditText priceTo, TextView dash) {
        ArrayAdapter adapter = (ArrayAdapter) priceSpinner.getAdapter(); //cast to an ArrayAdapter
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        priceSpinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(price);
        priceSpinner.setSelection(spinnerPosition);//set the default according to value
        priceSpinner.setVisibility(View.VISIBLE);
        if (price.equals("Any") || price.equals("Free")) {
            priceFrom.setVisibility(View.GONE);
            priceTo.setVisibility(View.GONE);
            dash.setVisibility(View.GONE);
        } else {
            priceFrom.setVisibility(View.VISIBLE);
            priceTo.setVisibility(View.VISIBLE);
            dash.setVisibility(View.VISIBLE);
        }
    }

    private String refreshSpinners(String name, SiteParam[] tree, Spinner[] spinners) {
        SiteParam obj = Utility.getObjFromTree(tree, name);
        while (Objects.requireNonNull(obj).hasChildren()) {
            obj = obj.getChildren()[0];
        }

        for (Spinner spinner : spinners) {
            spinner.setVisibility(View.GONE);
        }

        Stack<SiteParam> aStack = new Stack<>();
        aStack.push(obj);
        while (Objects.requireNonNull(obj).hasParent()) {
            obj = Utility.getObjFromTree(tree, obj.getParent());
            aStack.push(obj);
        }

        obj = new SiteParam();
        int size = aStack.size();
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                initializeSpinner(aStack, tree, spinners[0]);
            } else {
                initializeSpinner(aStack, obj.getChildren(), spinners[i]);
            }
            obj = aStack.pop();
            name = obj.getName();
        }
        return name;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}

package com.simonits.adalerts.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simonits.adalerts.R;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ConfirmDialog extends AppCompatDialogFragment {

    private static final String TAG = "ConfirmDialog";
    private DeleteDialogListener deleteDialogListener;
    private SpamDialogListener spamDialogListener;
    private LogoutDialogListener logoutDialogListener;
    private FrequencyDialogListener frequencyDialogListener;
    private final Context context;
    private String text, title, frequencyKey;
    private final String dialogType;
    private boolean keyboardShown;
    private NumberPicker valuePicker, typePicker;
    private String exportJson = "";
    private EditText editText;
    private AlertDialog previousDialog;
    private Fragment targetFragment;
    private String previousDialogType;
    private int timeValue;
    private String timeType;
    private ArrayList<Alert> alertList;

    public ConfirmDialog(Context context, String text, String dialogType) {
        this.context = context;
        this.text = text;
        this.dialogType = dialogType;
    }

    public ConfirmDialog(Context context, String dialogType, AlertDialog previousDialog, Fragment f, FrequencyDialogListener frequencyDialogListener, String previousDialogType, String frequencyKey, int timeValue, String timeType) {
        this.context = context;
        this.dialogType = dialogType;
        this.previousDialog = previousDialog;
        targetFragment = f;
        this.frequencyDialogListener = frequencyDialogListener;
        this.previousDialogType = previousDialogType;
        this.frequencyKey = frequencyKey;
        this.timeValue = timeValue;
        this.timeType = timeType;
    }

    public ConfirmDialog(Context context, String text, String title, String dialogType) {
        this.context = context;
        this.text = text;
        this.title = title;
        this.dialogType = dialogType;
    }

    public ConfirmDialog(Context context, String text, String title, String frequencyKey, String dialogType) {
        this.context = context;
        this.text = text;
        this.title = title;
        this.frequencyKey = frequencyKey;
        this.dialogType = dialogType;
    }

    public ConfirmDialog(Context context, String dialogType, AlertDialog previousDialog, String previousDialogType, ArrayList<Alert> alertList) {
        this.context = context;
        this.dialogType = dialogType;
        this.previousDialog = previousDialog;
        this.previousDialogType = previousDialogType;
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String negText = "No";
        String posText = "Yes";
        final EditText input = new EditText(context);
        if (dialogType.equals(Constants.DIALOGTYPE_DELETE)) {
            builder.setTitle("Delete?");
            builder.setMessage("Delete \"" + text + "\"?");
        } else if (dialogType.contains(Constants.DIALOGTYPE_SPAM)) {
            builder.setTitle("Mark As Spam?");
            builder.setMessage("Mark \"" + text + "\" as spam?");
        } else if (dialogType.contains(Constants.DIALOGTYPE_FACEBOOK)) {
            builder.setTitle("Remove Facebook Account?");
            builder.setMessage("Logging out of Facebook will remove the account from this app as well as any Facebook Marketplace alerts you have set up.");
        } else if (dialogType.equals(Constants.DIALOGTYPE_NAME)) {
            keyboardShown = false;
            final View activityRootView = requireActivity().findViewById(R.id.main_coordinator);
            activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                keyboardShown = heightDiff > Utility.dpToPx(context, 200); // if more than 200 dp, it's probably a keyboard... else false
            });

            builder.setTitle("Custom Name");
            negText = "Cancel";
            posText = "Confirm";
            LinearLayout container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(60, 30, 60, 30);
            input.setLayoutParams(lp);
            input.setText(text);
            input.requestFocus();
            showKeyboard();
            container.addView(input);
            builder.setView(container);
        } else if (dialogType.contains(Constants.DIALOGTYPE_FREQUENCY)) {
            builder.setTitle(title);
            negText = "Cancel";
            posText = "Confirm";
            LinearLayout container = new LinearLayout(context);
            container.setOrientation(LinearLayout.HORIZONTAL);
            setupPickers();
            container.addView(valuePicker);
            container.addView(typePicker);
            container.setGravity(Gravity.CENTER);
            builder.setView(container);
        } else if (dialogType.equals(Constants.DIALOGTYPE_FREQUENCYWARNING)) {
            builder.setTitle("Are you sure?");
            builder.setMessage("With a frequency this high, your device will constantly be checking for new listings - meaning its battery life will suffer and it may potentially become hot from working so often. Are you sure about this frequency?");
        } else if (dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_ALERTCONFIG)) {
            builder.setTitle("Import/Export Alert Config");
            negText = "Cancel";
            posText = "Save";
            View view = requireActivity().getLayoutInflater().inflate(R.layout.textbox_importexport, null);
            editText = view.findViewById(R.id.edittext_importexport);
            ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
            if (!alertList.isEmpty()) {
                for (Alert alert : alertList) {
                    alert.getKListings().clear();
                    alert.getIDsHistory(Constants.TABSITE_KIJIJI).clear();
                    alert.getEListings().clear();
                    alert.getIDsHistory(Constants.TABSITE_EBAY).clear();
                    alert.getCListings().clear();
                    alert.getIDsHistory(Constants.TABSITE_CRAIGSLIST).clear();
                    alert.getFListingHistory().clear();
                }
                exportJson = new Gson().toJson(alertList);
                editText.setText(exportJson);
            }
            builder.setView(view);
        } else if (dialogType.equals(Constants.DIALOGTYPE_IMPORTFACEBOOKWARNING)) {
            builder.setTitle("Warning!");
            builder.setMessage("Facebook Alerts can't be added because you aren't logged in to Facebook. Proceed anyways?");
        } else if (dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_FAVOURITES)) {
            builder.setTitle("Import/Export Favourites");
            negText = "Cancel";
            posText = "Save";
            View view = requireActivity().getLayoutInflater().inflate(R.layout.textbox_importexport, null);
            editText = view.findViewById(R.id.edittext_importexport);
            editText.setHint("Paste listing data here");
            ArrayList<Listing> favourites = PreferenceUtils.getFavsList(context);
            if (!favourites.isEmpty()) {
                exportJson = new Gson().toJson(favourites);
                editText.setText(exportJson);
            }
            builder.setView(view);
        }
        if (!dialogType.equals(Constants.DIALOGTYPE_NOTICE)) {
            builder.setNegativeButton(negText, (dialogInterface, i) -> {
                        if (dialogType.equals(Constants.DIALOGTYPE_NAME)) {
                            closeKeyboard();
                        }
                    })
                    .setPositiveButton(posText, (dialogInterface, i) -> {
                        if (dialogType.equals(Constants.DIALOGTYPE_DELETE)) {
                            deleteDialogListener.onYesClicked();
                        } else if (dialogType.contains(Constants.DIALOGTYPE_SPAM)) {
                            if (dialogType.equals(Constants.DIALOGTYPE_SPAM)) {
                                requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                            } else {
                                spamDialogListener.onYesClicked();
                            }
                        } else if (dialogType.equals(Constants.DIALOGTYPE_NAME)) {
                            deleteDialogListener.onConfirmClicked(input.getText().toString().trim());
                            closeKeyboard();
                        } else if (dialogType.contains(Constants.DIALOGTYPE_FACEBOOK)) {
                            if (dialogType.equals(Constants.DIALOGTYPE_FACEBOOK)) {
                                logoutDialogListener.onYesClicked();
                            } else {
                                requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                            }
                        } else if (dialogType.equals(Constants.DIALOGTYPE_FREQUENCYWARNING)) {
                            PreferenceUtils.setFrequencyWarningOk(true, context);
                            PreferenceUtils.optionSaveFrequency(frequencyKey, timeValue + " " + timeType, context);
                            if (previousDialogType.equals(Constants.DIALOGTYPE_FREQUENCY)) {
                                requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                            } else {
                                frequencyDialogListener.onConfirmClicked();
                            }
                            previousDialog.dismiss();
                        } else if (dialogType.equals(Constants.DIALOGTYPE_IMPORTFACEBOOKWARNING)) {
                            PreferenceUtils.saveAlertList(alertList, context);
                            requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                            previousDialog.dismiss();
                        }
                    });
        } else {
            builder.setTitle(title);
            builder.setMessage(text);
            builder.setNegativeButton("Dismiss", (dialogInterface, i) -> {
            });
        }
        if (dialogType.contains(Constants.DIALOGTYPE_FREQUENCY)) {
            builder.setNeutralButton(Constants.FREQUENCY_NEVER, (dialogInterface, i) -> {
                PreferenceUtils.optionSaveFrequency(frequencyKey, Constants.FREQUENCY_NEVER, context);
                if (dialogType.equals(Constants.DIALOGTYPE_FREQUENCY)) {
                    requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                } else {
                    frequencyDialogListener.onConfirmClicked();
                }
            });
        }

        AlertDialog dialog = builder.create();

        if (dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_ALERTCONFIG) || dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_FAVOURITES)) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button positiveButton = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (exportJson.equals(editText.getText().toString())) {
                                // no changes to the data
                                dialog.dismiss();
                            } else {
                                // a change to the data
                                try {
                                    if (dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_ALERTCONFIG)) {
                                        // tries to save the new alert data as an ArrayList<Alert>
                                        Type type = new TypeToken<ArrayList<Alert>>() {
                                        }.getType();
                                        ArrayList<Alert> alertList = new Gson().fromJson(editText.getText().toString(), type);
                                        if (alertList == null) {
                                            alertList = new ArrayList<>();
                                        }
                                        boolean fbLogIn = PreferenceUtils.getFacebookLogin(context);
                                        boolean fbDisabled = false;
                                        ArrayList<Alert> fbOnlyAlerts = new ArrayList<>();
                                        for (Alert alert : alertList) {
                                            Field[] fields = alert.getClass().getDeclaredFields();
                                            for (Field field : fields) {
                                                try {
                                                    field.setAccessible(true);
                                                    Object value = field.get(alert);
                                                    if (value == null) {
                                                        throw new IllegalArgumentException("JSON string does not include all fields of the custom object");
                                                    }
                                                } catch (IllegalAccessException e) {
                                                    Log.e(TAG, e.toString());
                                                }
                                            }
                                            if (!fbLogIn && alert.hasFacebook()) {
                                                fbDisabled = true;
                                                if (alert.hasKijiji() || alert.hasEbay() || alert.hasCraigslist()) {
                                                    alert.disableFacebook();
                                                } else {
                                                    fbOnlyAlerts.add(alert);
                                                }
                                            }
                                        }
                                        if (fbDisabled) {
                                            alertList.removeAll(fbOnlyAlerts);
                                            ConfirmDialog dialog = new ConfirmDialog(context, Constants.DIALOGTYPE_IMPORTFACEBOOKWARNING, (AlertDialog) getDialog(), dialogType, alertList);
                                            dialog.show(requireActivity().getSupportFragmentManager(), "confirm dialog");
                                        } else {
                                            PreferenceUtils.saveAlertList(alertList, context);
                                            dialog.dismiss();
                                            requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                                        }
                                    } else if (dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_FAVOURITES)) { // else if for readability
                                        // tries to save the new listing data as an ArrayList<Listing>
                                        Type type = new TypeToken<ArrayList<Listing>>() {
                                        }.getType();
                                        ArrayList<Listing> favourites = new Gson().fromJson(editText.getText().toString(), type);
                                        if (favourites == null) {
                                            favourites = new ArrayList<>();
                                        }
                                        for (Listing favourite : favourites) {
                                            Field[] fields = favourite.getClass().getDeclaredFields();
                                            for (Field field : fields) {
                                                try {
                                                    field.setAccessible(true);
                                                    Object value = field.get(favourite);
                                                    if (value == null) {
                                                        throw new IllegalArgumentException("JSON string does not include all fields of the custom object");
                                                    }
                                                } catch (IllegalAccessException e) {
                                                    Log.e(TAG, e.toString());
                                                }
                                            }
                                        }
                                        PreferenceUtils.saveFavsList(favourites, context);
                                        dialog.dismiss();
                                        requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                                    }
                                } catch (Exception e) {
                                    // invalid data json (checks the json formatting and field structure, but not the field values)
                                    if (dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_ALERTCONFIG)) {
                                        Snackbar.make(view, "Invalid Alert Data", Snackbar.LENGTH_SHORT).show();
                                    } else if (dialogType.equals(Constants.DIALOGTYPE_IMPORTEXPORT_FAVOURITES)) { // else if for readability
                                        Snackbar.make(view, "Invalid Listing Data", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null && dialogType.contains(Constants.DIALOGTYPE_FREQUENCY)) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                boolean wantToCloseDialog;
                int frequencyTimeValue = valuePicker.getValue();
                String frequencyTimeType = Utility.extractFrequencyTimeTypeFromArray(context, typePicker.getValue(), frequencyTimeValue);
                if (frequencyTimeValue < 5 && frequencyTimeType.contains("Minute") && !PreferenceUtils.getFrequencyWarningOk(context)) { // && !frequencyWarningOk
                    wantToCloseDialog = false;
                    ConfirmDialog dialog = new ConfirmDialog(context, Constants.DIALOGTYPE_FREQUENCYWARNING, d, targetFragment, frequencyDialogListener, dialogType, frequencyKey, frequencyTimeValue, frequencyTimeType);
                    dialog.show(requireActivity().getSupportFragmentManager(), "confirm dialog");
                } else {
                    wantToCloseDialog = true;
                    PreferenceUtils.optionSaveFrequency(frequencyKey, frequencyTimeValue + " " + frequencyTimeType, context);
                    if (dialogType.equals(Constants.DIALOGTYPE_FREQUENCY)) {
                        requireActivity().getSupportFragmentManager().setFragmentResult(Constants.KEY_FRAGMENTREQUEST, new Bundle());
                    } else {
                        frequencyDialogListener.onConfirmClicked();
                    }
                }
                if (wantToCloseDialog) {
                    d.dismiss();
                }
            });
        }
    }

    public interface DeleteDialogListener {
        void onYesClicked();

        void onConfirmClicked(String customName);
    }

    public interface SpamDialogListener {
        void onYesClicked();
    }

    public interface LogoutDialogListener {
        void onYesClicked();
    }

    public interface FrequencyDialogListener {
        void onConfirmClicked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (dialogType.equals(Constants.DIALOGTYPE_DELETE) || dialogType.equals(Constants.DIALOGTYPE_NAME)) {
            try {
                deleteDialogListener = (DeleteDialogListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context
                        + "must implement DeleteDialogListener");
            }
        } else if (dialogType.equals("Spam_NewListingsActivity")) {
            try {
                spamDialogListener = (SpamDialogListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context
                        + "must implement SpamDialogListener");
            }
        } else if (dialogType.equals(Constants.DIALOGTYPE_FACEBOOK)) {
            try {
                logoutDialogListener = (LogoutDialogListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context
                        + "must implement LogoutDialogListener");
            }
        } else if (dialogType.contains(Constants.DIALOGTYPE_FREQUENCY) && !dialogType.equals(Constants.DIALOGTYPE_FREQUENCY)) {
            try {
                frequencyDialogListener = (FrequencyDialogListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context
                        + "must implement FrequencyDialogListener");
            }
        }
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void closeKeyboard() {
        if (keyboardShown) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    private void setupPickers() {
        String[] frequencyTimeTypes = context.getResources().getStringArray(R.array.frequency_time_types);
        String frequency = PreferenceUtils.optionGetFrequency(frequencyKey, context);
        String[] splitFrequency = frequency.split(" ");

        valuePicker = new NumberPicker(context);
        valuePicker.setVerticalScrollBarEnabled(false);
        valuePicker.setMinValue(1);
        valuePicker.setMaxValue(60);

        int value;
        if (!frequency.equals(Constants.FREQUENCY_NEVER)) {
            value = Integer.parseInt(splitFrequency[0]);
        } else {
            value = 15;
        }
        valuePicker.setValue(value);

        typePicker = new NumberPicker(context);
        typePicker.setVerticalScrollBarEnabled(false);
        typePicker.setMinValue(0);
        typePicker.setMaxValue(frequencyTimeTypes.length - 1);
        typePicker.setDisplayedValues(frequencyTimeTypes);

        if (!frequency.equals(Constants.FREQUENCY_NEVER)) {
            String type = splitFrequency[1];
            int index = 0;
            for (int i = 0; i < frequencyTimeTypes.length; i++) {
                if (frequencyTimeTypes[i].contains(type)) {
                    index = i;
                }
            }
            typePicker.setValue(index);
        }
    }
}

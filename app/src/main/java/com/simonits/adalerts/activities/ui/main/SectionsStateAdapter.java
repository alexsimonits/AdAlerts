package com.simonits.adalerts.activities.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

public class SectionsStateAdapter extends FragmentStateAdapter {

    private static final String TAG = "SectionsStateAdapter";

    private final Alert alert;
    private int kPagePos, ePagePos, cPagePos, fPagePos;
    private int count;

    public SectionsStateAdapter(FragmentManager fm, Lifecycle lifecycle, Context context, int alertID) {
        super(fm, lifecycle);
        alert = Utility.getAlertFromList(PreferenceUtils.getAlertList(context), alertID);
        setCount();
    }

    public String getTabTitle(int position) {
        if ((position == kPagePos && !alert.getOnlyNewBgListings(Constants.TABSITE_KIJIJI).isEmpty()) || (position == ePagePos && !alert.getOnlyNewBgListings(Constants.TABSITE_EBAY).isEmpty()) || (position == cPagePos && !alert.getOnlyNewBgListings(Constants.TABSITE_CRAIGSLIST).isEmpty()) || (position == fPagePos && !alert.getOnlyNewBgListings(Constants.TABSITE_FACEBOOK).isEmpty())) {
            if (position == kPagePos) {
                return "(!) " + " Kijiji";
            } else if (position == ePagePos) {
                return "(!) " + " eBay";
            } else if (position == cPagePos) {
                return "(!) " + " Craigslist";
            } else {
                return "(!) " + " Facebook";
            }
        } else {
            if (position == kPagePos) {
                return "Kijiji";
            } else if (position == ePagePos) {
                return "eBay";
            } else if (position == cPagePos) {
                return "Craigslist";
            } else {
                return "Facebook";
            }
        }
    }

    private void setCount() {
        kPagePos = -1;
        ePagePos = -1;
        cPagePos = -1;
        fPagePos = -1;
        count = 0;
        if (alert.hasKijiji()) {
            kPagePos = count;
            count++;
        }
        if (alert.hasEbay()) {
            ePagePos = count;
            count++;
        }
        if (alert.hasCraigslist()) {
            cPagePos = count;
            count++;
        }
        if (alert.hasFacebook()) {
            fPagePos = count;
            count++;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // createFragment is called to instantiate the fragment for the given page.
        // Return a TabFragment (defined as a static inner class below).

        if (position == kPagePos) { // kijiji tab
            return TabFragment.newInstance(alert.getId(), Constants.TABSITE_KIJIJI, position);
        } else if (position == ePagePos) { // ebay tab
            return TabFragment.newInstance(alert.getId(), Constants.TABSITE_EBAY, position);
        } else if (position == cPagePos) { // craigslist tab
            return TabFragment.newInstance(alert.getId(), Constants.TABSITE_CRAIGSLIST, position);
        } else if (position == fPagePos) { // facebook tab
            return TabFragment.newInstance(alert.getId(), Constants.TABSITE_FACEBOOK, position);
        } else {
            Log.i(TAG, "position is neither 0 nor 1 nor 2..???????");
            return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }
}
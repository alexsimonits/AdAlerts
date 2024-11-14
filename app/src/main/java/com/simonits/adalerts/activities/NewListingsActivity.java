package com.simonits.adalerts.activities;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.simonits.adalerts.R;
import com.simonits.adalerts.adapters.Listings_RecAdapter;
import com.simonits.adalerts.dialogs.ConfirmDialog;
import com.simonits.adalerts.dialogs.InAppReview;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.util.ArrayList;
import java.util.Objects;

public class NewListingsActivity extends AppCompatActivity implements ConfirmDialog.SpamDialogListener, PopupMenu.OnMenuItemClickListener, Listings_RecAdapter.OnListingListener {

    private Listings_RecAdapter adapter;
    private ArrayList<Listing> newListings;
    private int listingPosition;
    private boolean detailsClicked;
    private TextView guideText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_listings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ((NotificationManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))).cancel(2);



        /*// Get the TaskStackBuilder object that created the synthetic back stack for this activity
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        // Add the parent activities to the builder
        taskStackBuilder.addParentStack(this);
        // Get the PendingIntent object that contains the parent stack information
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);
        // Log the PendingIntent object using a tag and a message
        Log.d("MyActivity", "PendingIntent: " + pendingIntent);*/

        //If the Up button is ready to go back to MainActivity, the log will look something like this:
        //D/MyActivity: PendingIntent: PendingIntent{1234567: android.os.BinderProxy@abcdef launchParam=MultiScreenLaunchParams { mDisplayId=0 mFlags=0 } IntentSender{1234567: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.example.myapp/.MainActivity }}}
        //You can see that the PendingIntent object contains an Intent object that has the action, category, flags, and component of the MainActivity. This means that the parent stack has the MainActivity as the root activity and the Up button will navigate to it.



        RecyclerView recView = findViewById(R.id.newListings_recView);
        guideText = findViewById(R.id.newListings_guideText);

        newListings = new ArrayList<>();
        getNewListings();
        Utility.updateSavedPrefs(this, 0, null, 0, null, 0, null);

        if (newListings.isEmpty()) {
            guideText.setVisibility(View.VISIBLE);
        } else {
            adapter = new Listings_RecAdapter(this, newListings, this);
            recView.setLayoutManager(new GridLayoutManager(this, 1));
            recView.setAdapter(adapter);
        }

        InAppReview.activityCreated(this, this);

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
    public void onListingClick(int position) {
        detailsClicked = true;
        listingPosition = position;
        Intent intent = new Intent(NewListingsActivity.this, DetailsActivity.class);
        if (newListings.get(position).getID().contains("k")) {
            intent.putExtra("tabSite", Constants.TABSITE_KIJIJI);
        } else if (newListings.get(position).getID().contains("e")) {
            intent.putExtra("tabSite", Constants.TABSITE_EBAY);
        } else if (newListings.get(position).getID().contains("c")) {
            intent.putExtra("tabSite", Constants.TABSITE_CRAIGSLIST);
        } else if (newListings.get(position).getID().contains("f")) {
            intent.putExtra("tabSite", Constants.TABSITE_FACEBOOK);
        }
        intent.putExtra("listing", newListings.get(position));
        intent.putExtra("name", newListings.get(position).getName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.scale_out);
    }

    @Override
    public void onListingHold(View view, int position) {
        listingPosition = position;
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        popupMenu.setOnMenuItemClickListener(NewListingsActivity.this);
        popupMenu.inflate(R.menu.menu_tabbed_popup);
        Menu menu = popupMenu.getMenu();
        if (newListings.get(position).getID().startsWith("k")) {
            menu.findItem(R.id.fragment_item_kijiji).setVisible(true);
        } else if (newListings.get(position).getID().startsWith("e")) {
            menu.findItem(R.id.fragment_item_ebay).setVisible(true);
        } else if (newListings.get(position).getID().startsWith("c")) {
            menu.findItem(R.id.fragment_item_craigslist).setVisible(true);
        } else if (newListings.get(position).getID().startsWith("f")) {
            menu.findItem(R.id.fragment_item_facebook).setVisible(true);
        }
        if (Utility.isInListings(PreferenceUtils.getFavsList(NewListingsActivity.this), newListings.get(position).getID())) {
            menu.findItem(R.id.fragment_item_removeFav).setVisible(true);
        } else {
            menu.findItem(R.id.fragment_item_addFav).setVisible(true);
        }
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
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

    private void getNewListings() {
        ArrayList<Alert> alertList = PreferenceUtils.getAlertList(this);
        for (Alert alert : alertList) {
            if (!alert.getOnlyNewBgListings().isEmpty()) {
                addOrderlyToNewListings(alert.getOnlyNewBgListings());
            }
        }
        ArrayList<Listing> savedNewListings = PreferenceUtils.getNewListingsList(this);
        for (int i = 0; i < savedNewListings.size(); i++) {
            if (Utility.isInListings(newListings, savedNewListings.get(i).getID())) {
                savedNewListings.remove(i);
                i--;
            } else {
                savedNewListings.get(i).setNewListing(false);
            }
        }
        newListings.addAll(savedNewListings);
        while (newListings.size() > 100) {
            newListings.remove(newListings.size() - 1);
        } // makes sure newListings does not get too long to where the app lags hard in this recView
        PreferenceUtils.saveNewListingsList(newListings, this);
    }

    private void addOrderlyToNewListings(ArrayList<Listing> onlyNewBgListings) {
        for (Listing listing : onlyNewBgListings) {
            if (newListings.isEmpty()) {
                newListings.add(listing);
            } else {
                if (!Utility.isInListings(newListings, listing.getID())) {
                    for (int i = 0; i < newListings.size(); i++) {
                        if (listing.getDate().after(newListings.get(i).getDate())) {
                            newListings.add(i, listing);
                            break;
                        }
                        if (i == newListings.size() - 1) {
                            newListings.add(listing);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (detailsClicked) {
            newListings.get(listingPosition).setNewListing(false);
            adapter.notifyItemChanged(listingPosition);
            detailsClicked = false;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        ConfirmDialog dialog;
        ArrayList<Listing> favourites;
        int itemId = menuItem.getItemId();
        if (itemId == R.id.fragment_item_kijiji || itemId == R.id.fragment_item_ebay || itemId == R.id.fragment_item_craigslist || itemId == R.id.fragment_item_facebook) {
            detailsClicked = true;
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(newListings.get(listingPosition).getUrl())));
        } else if (itemId == R.id.fragment_item_addFav) {
            favourites = PreferenceUtils.getFavsList(this);
            favourites.add(0, newListings.get(listingPosition));
            PreferenceUtils.saveFavsList(favourites, this);
            Snackbar.make(this.findViewById(android.R.id.content), "Added to favourites", Snackbar.LENGTH_SHORT).show();
        } else if (itemId == R.id.fragment_item_removeFav) {
            favourites = PreferenceUtils.getFavsList(this);
            int i = 0;
            for (Listing listing : favourites) {
                if (listing.getID().equals(newListings.get(listingPosition).getID())) {
                    favourites.remove(i);
                    PreferenceUtils.saveFavsList(favourites, this);
                    Snackbar.make(this.findViewById(android.R.id.content), "Removed from favourites", Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                i++;
            }
        } else if (itemId == R.id.fragment_item_spam) {
            dialog = new ConfirmDialog(this, newListings.get(listingPosition).getName(), "Spam_NewListingsActivity");
            dialog.show(getSupportFragmentManager(), "confirm dialog");
        } else {
            return false;
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onYesClicked() {
        if (newListings.get(listingPosition).getID().startsWith("f")) {
            Utility.updateSavedPrefs(this, 0, null, -1, null, 0, null);
        }
        markSpam(listingPosition);
        Utility.removeSpam(this, newListings);
        adapter.notifyDataSetChanged();
        PreferenceUtils.saveNewListingsList(newListings, this);
        if (newListings.isEmpty()) {
            guideText.setVisibility(View.VISIBLE);
        }
    }

    private void markSpam(int position) {
        if (!newListings.get(position).getImage().equals("no image") && !newListings.get(position).getImage().equals("-IMAGE ERROR-")) {
            if (!PreferenceUtils.spamCheck(newListings.get(position).getImage(), this)) {
                PreferenceUtils.addSpam(newListings.get(position).getImage(), this);
            }
        }
        if (!newListings.get(position).getName().equals("-TITLE ERROR-")) {
            if (!PreferenceUtils.spamCheck(newListings.get(position).getName(), this)) {
                PreferenceUtils.addSpam(newListings.get(position).getName(), this);
            }
        }
        Snackbar.make(findViewById(android.R.id.content), "Marked as spam", Snackbar.LENGTH_SHORT).show();
    }
}
package com.simonits.adalerts.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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

import com.google.android.material.snackbar.Snackbar;
import com.simonits.adalerts.R;
import com.simonits.adalerts.adapters.Listings_RecAdapter;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Objects;

public class FavouritesActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, Listings_RecAdapter.OnListingListener {

    private Listings_RecAdapter adapter;
    private TextView guideText;
    private ArrayList<Listing> favourites;
    private int listingPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        RecyclerView recView = findViewById(R.id.fav_recView);
        guideText = findViewById(R.id.fav_guideText);

        favourites = PreferenceUtils.getFavsList(this);
        adapter = new Listings_RecAdapter(this, favourites, this);
        recView.setLayoutManager(new GridLayoutManager(this, 1));
        recView.setAdapter(adapter);
        if (favourites.isEmpty()) {
            guideText.setVisibility(View.VISIBLE);
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

    @Override
    public void onListingClick(int position) {
        Intent intent = new Intent(FavouritesActivity.this, DetailsActivity.class);
        if (favourites.get(position).getID().contains("k")) {
            intent.putExtra("tabSite", Constants.TABSITE_KIJIJI);
        } else if (favourites.get(position).getID().contains("e")) {
            intent.putExtra("tabSite", Constants.TABSITE_EBAY);
        } else if (favourites.get(position).getID().contains("c")) {
            intent.putExtra("tabSite", Constants.TABSITE_CRAIGSLIST);
        } else if (favourites.get(position).getID().contains("f")) {
            intent.putExtra("tabSite", Constants.TABSITE_FACEBOOK);
        }
        intent.putExtra("listing", favourites.get(position));
        intent.putExtra("name", favourites.get(position).getName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.scale_out);
    }

    @Override
    public void onListingHold(View view, int position) {
        listingPosition = position;
        PopupMenu popupMenu = new PopupMenu(FavouritesActivity.this, view);
        popupMenu.setOnMenuItemClickListener(FavouritesActivity.this);
        popupMenu.inflate(R.menu.menu_favs_popup);
        Menu menu = popupMenu.getMenu();
        if (favourites.get(position).getID().startsWith("k")) {
            menu.findItem(R.id.favourites_item_kijiji).setVisible(true);
        } else if (favourites.get(position).getID().startsWith("e")) {
            menu.findItem(R.id.favourites_item_ebay).setVisible(true);
        } else if (favourites.get(position).getID().startsWith("c")) {
            menu.findItem(R.id.favourites_item_craigslist).setVisible(true);
        } else if (favourites.get(position).getID().startsWith("f")) {
            menu.findItem(R.id.favourites_item_facebook).setVisible(true);
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

    @SuppressLint("NotifyDataSetChanged")
    private void refreshLocalFavourites() {
        ArrayList<Listing> freshList = PreferenceUtils.getFavsList(this);
        for (int i = 0; i < favourites.size(); i++) {
            String id = favourites.get(i).getID();
            favourites.remove(i);
            if (freshList.size() - 1 >= i && id.equals(freshList.get(i).getID())) {
                favourites.add(i, freshList.get(i));
            } else if (freshList.size() - 1 >= i) {
                i--;
            }
        }
        adapter.notifyDataSetChanged();
        if (favourites.isEmpty()) {
            guideText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLocalFavourites();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.favourites_item_kijiji || itemId == R.id.favourites_item_ebay || itemId == R.id.favourites_item_craigslist || itemId == R.id.favourites_item_facebook) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(favourites.get(listingPosition).getUrl())));
        } else if (itemId == R.id.favourites_item_removeFav) {
            favourites.remove(listingPosition);
            PreferenceUtils.saveFavsList(favourites, this);
            adapter.notifyItemRemoved(listingPosition);
            Snackbar.make(findViewById(android.R.id.content), "Removed from favourites", Snackbar.LENGTH_SHORT).show();
            if (favourites.isEmpty()) {
                guideText.setVisibility(View.VISIBLE);
            }
        } else {
            return false;
        }
        return true;
    }

}

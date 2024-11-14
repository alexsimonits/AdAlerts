package com.simonits.adalerts.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simonits.adalerts.R;
import com.simonits.adalerts.activities.NewListingsActivity;
import com.simonits.adalerts.activities.TabbedActivity;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Listings_RecAdapter extends RecyclerView.Adapter<Listings_RecAdapter.Listings_RecViewHolder> {

    private final Context context;
    private final ArrayList<Listing> listings;
    private ArrayList<Listing> onlyNewListings;
    private final OnListingListener onListingListener;

    public Listings_RecAdapter(Context context, ArrayList<Listing> listings, ArrayList<Listing> onlyNewListings, OnListingListener onListingListener) { // from TabbedActivity
        this.context = context;
        this.listings = listings;
        this.onlyNewListings = onlyNewListings;
        this.onListingListener = onListingListener;
    }

    public Listings_RecAdapter(Context context, ArrayList<Listing> listings, OnListingListener onListingListener) { // from FavouritesActivity or NewListingsActivity
        this.context = context;
        this.listings = listings;
        this.onListingListener = onListingListener;
    }

    @NonNull
    @Override
    public Listings_RecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listing_row, parent, false);
        return new Listings_RecViewHolder(v, onListingListener);
    }

    @Override
    public void onBindViewHolder(@NonNull Listings_RecViewHolder holder, final int position) {
        if (listings.get(position).getImage().contains("placeholder") || listings.get(position).getImage().equals("no image") || listings.get(position).getImage().equals("https://ir.ebaystatic.com/pictures/aw/pics/s_1x2.gif")) {
            holder.noImageText.setVisibility(View.VISIBLE);
            Picasso.get().load("blahblahblah").into(holder.image);
        } else if (listings.get(position).getImage().equals("-IMAGE ERROR-")) {
            holder.imageErrorText.setVisibility(View.VISIBLE);
            Picasso.get().load("blahblahblah").into(holder.image);
        } else {
            holder.noImageText.setVisibility(View.INVISIBLE);
            holder.imageErrorText.setVisibility(View.INVISIBLE);
            Picasso.get().load(listings.get(position).getImage()).into(holder.image);
        }
        holder.image.setTag(position);

        holder.name.setText(listings.get(position).getName());
        holder.price.setText(listings.get(position).getPrice());

        String location = listings.get(position).getLocation();
        if (location == null) {
            location = "";
        }
        if (location.contains("/")) {
            location = location.substring(0, location.indexOf("/")).trim();
        }

        String dateStr;
        boolean isNewListing = false;
        if (context instanceof TabbedActivity) {
            isNewListing = Utility.isInListings(onlyNewListings, listings.get(position).getID());
        } else if (context instanceof NewListingsActivity) {
            isNewListing = listings.get(position).isNewListing();
        }
        if (listings.get(position).getID().startsWith("f")) {
            dateStr = "";
        } else {
            if (!location.isEmpty() && !listings.get(position).getPermaDateStr().isEmpty()) {
                location = location + " @ ";
            }
            if (isNewListing) {
                dateStr = getDateStr(listings.get(position).getDate());
            } else {
                dateStr = listings.get(position).getPermaDateStr();
            }
        }
        if (isNewListing) {
            setNewListingStyle(holder);
        } else {
            setOldListingStyle(holder);
        }
        String locPosted = location + dateStr;
        holder.loc_posted.setText(locPosted);
    }

    private String getDateStr(Date date) {
        String dateStr;
        int number;
        long difference = Calendar.getInstance().getTimeInMillis() - date.getTime();
        if (difference > 60000 * 60 * 24 * 7) { // more than a week
            number = (int) (difference / (60000 * 60 * 24 * 7));
            dateStr = number + " week";
        } else if (difference > 60000 * 60 * 24) { // more than a day
            number = (int) (difference / (60000 * 60 * 24));
            dateStr = number + " day";
        } else if (difference > 60000 * 60) { // more than an hour
            number = (int) (difference / (60000 * 60));
            dateStr = number + " hour";
        } else if (difference > 60000) { // more than a minute
            number = (int) (difference / 60000);
            dateStr = number + " minute";
        } else {
            number = 1;
            dateStr = number + " minute";
        }
        if (number != 1) {
            dateStr = dateStr + "s";
        }
        return dateStr + " ago";
    }

    private void setNewListingStyle(Listings_RecViewHolder holder) {
        holder.newListingIcon.setVisibility(View.VISIBLE);
        holder.name.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.price.setTypeface(null, Typeface.BOLD);
        holder.loc_posted.setTypeface(null, Typeface.BOLD);
        holder.image.setBackground(ContextCompat.getDrawable(context, R.drawable.border2));
    }

    private void setOldListingStyle(Listings_RecViewHolder holder) {
        holder.newListingIcon.setVisibility(View.GONE);
        holder.name.setTextColor(ContextCompat.getColor(context, R.color.grey));
        holder.price.setTypeface(null, Typeface.NORMAL);
        holder.loc_posted.setTypeface(null, Typeface.NORMAL);
        holder.image.setBackground(ContextCompat.getDrawable(context, R.drawable.border));
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    public static class Listings_RecViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView name;
        public ImageView newListingIcon;
        public TextView noImageText, imageErrorText;
        public ImageView image;
        public TextView price;
        public TextView loc_posted;
        public ConstraintLayout listing_constraint;
        OnListingListener onListingListener;

        public Listings_RecViewHolder(View itemView, OnListingListener onListingListener) {
            super(itemView);
            name = itemView.findViewById(R.id.listing_name);
            newListingIcon = itemView.findViewById(R.id.listing_newListingIcon);
            noImageText = itemView.findViewById(R.id.listing_noImageText);
            imageErrorText = itemView.findViewById(R.id.listing_imageErrorText);
            image = itemView.findViewById(R.id.listing_image);
            price = itemView.findViewById(R.id.listing_price);
            loc_posted = itemView.findViewById(R.id.listing_location_posted);
            listing_constraint = itemView.findViewById(R.id.listing_constraint);
            this.onListingListener = onListingListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onListingListener.onListingClick(getBindingAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            onListingListener.onListingHold(view, getBindingAdapterPosition());
            return false;
        }
    }

    public interface OnListingListener {
        void onListingClick(int position);
        void onListingHold(View view, int position);
    }
}

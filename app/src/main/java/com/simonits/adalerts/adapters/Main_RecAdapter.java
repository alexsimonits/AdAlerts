package com.simonits.adalerts.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Main_RecAdapter extends RecyclerView.Adapter<Main_RecAdapter.Main_RecViewHolder> {

    private final Context context;
    private final ArrayList<Alert> alertList;
    private ColorStateList oldColors;
    private final OnAlertListener onAlertListener;

    public Main_RecAdapter(Context context, ArrayList<Alert> alertList, OnAlertListener onAlertListener) {
        this.context = context;
        this.alertList = alertList;
        this.onAlertListener = onAlertListener;
    }

    @NonNull
    @Override
    public Main_RecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.alert_row, parent, false);
        //Main_RecViewHolder.setIsRecyclable(false);
        return new Main_RecViewHolder(v, onAlertListener);
    }

    @Override
    public void onBindViewHolder(@NonNull Main_RecViewHolder holder, final int position) {
        if (alertList.get(position).getCustomName() == null) {
            alertList.get(position).fixNulls();
            Utility.updateSavedPrefs(context, 0, null, 8, null, alertList.get(position).getId(), alertList.get(position));
        } // temp. 122 update
        if (alertList.get(position).getCustomName() != null && !alertList.get(position).getCustomName().isEmpty()) { // temp. 122 update
            holder.alert_name.setText(alertList.get(position).getCustomName());
        } else {
            holder.alert_name.setText(alertList.get(position).getName());
        }

        if (oldColors == null) {
            oldColors = holder.alert_name.getTextColors();
        }
        if (!alertList.get(position).getOnlyNewBgListings().isEmpty()) {
            holder.alert_newListingIcon.setVisibility(View.VISIBLE);
            holder.alert_name.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        } else {
            holder.alert_newListingIcon.setVisibility(View.GONE);
            holder.alert_name.setTextColor(oldColors);
        }

        if (!alertList.get(position).getUpdateDate().equals(new GregorianCalendar(1997, Calendar.AUGUST, 24, 0, 0, 0).getTime())) {
            holder.alert_updateDate.setText(new SimpleDateFormat("h:mm a dd/M/yyyy", Locale.getDefault()).format(alertList.get(position).getUpdateDate()));
            holder.alert_updateDate.setVisibility(View.VISIBLE);
        } else {
            holder.alert_updateDate.setText("");
            holder.alert_updateDate.setVisibility(View.INVISIBLE);
        }

        if (alertList.get(position).hasKijiji()) {
            holder.alert_kijiji.setVisibility(View.VISIBLE);
        } else {
            holder.alert_kijiji.setVisibility(View.GONE);
        }
        if (alertList.get(position).hasEbay()) {
            holder.alert_ebay.setVisibility(View.VISIBLE);
        } else {
            holder.alert_ebay.setVisibility(View.GONE);
        }
        if (alertList.get(position).hasCraigslist()) {
            holder.alert_craigslist.setVisibility(View.VISIBLE);
        } else {
            holder.alert_craigslist.setVisibility(View.GONE);
        }
        if (alertList.get(position).hasFacebook()) {
            holder.alert_facebook.setVisibility(View.VISIBLE);
        } else {
            holder.alert_facebook.setVisibility(View.INVISIBLE); // this way the alert name keeps its constraint on this attribute
        }

        holder.alert_kijiji.setTypeface(null, Typeface.NORMAL);
        holder.alert_ebay.setTypeface(null, Typeface.NORMAL);
        holder.alert_craigslist.setTypeface(null, Typeface.NORMAL);
        holder.alert_facebook.setTypeface(null, Typeface.NORMAL);
        for (Listing listing : alertList.get(position).getOnlyNewBgListings()) {
            if (listing.getID().startsWith("k")) {
                holder.alert_kijiji.setTypeface(null, Typeface.BOLD);
            } else if (listing.getID().startsWith("e")) {
                holder.alert_ebay.setTypeface(null, Typeface.BOLD);
            } else if (listing.getID().startsWith("c")) {
                holder.alert_craigslist.setTypeface(null, Typeface.BOLD);
            } else if (listing.getID().startsWith("f")) {
                holder.alert_facebook.setTypeface(null, Typeface.BOLD);
            }
        }
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class Main_RecViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView alert_name;
        public TextView alert_updateDate;
        public ImageView alert_newListingIcon;
        public ConstraintLayout alert_constraint;
        public TextView alert_kijiji;
        public TextView alert_ebay;
        public TextView alert_craigslist;
        public TextView alert_facebook;
        OnAlertListener onAlertListener;

        public Main_RecViewHolder(View itemView, OnAlertListener onAlertListener) {
            super(itemView);
            alert_name = itemView.findViewById(R.id.alert_name);
            alert_updateDate = itemView.findViewById(R.id.alert_updateDate);
            alert_newListingIcon = itemView.findViewById(R.id.alert_newListingIcon);
            alert_constraint = itemView.findViewById(R.id.alert_constraint);
            alert_kijiji = itemView.findViewById(R.id.alert_kijiji);
            alert_ebay = itemView.findViewById(R.id.alert_ebay);
            alert_craigslist = itemView.findViewById(R.id.alert_craigslist);
            alert_facebook = itemView.findViewById(R.id.alert_facebook);
            this.onAlertListener = onAlertListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onAlertListener.onAlertClick(getBindingAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            onAlertListener.onAlertHold(view, getBindingAdapterPosition());
            return false;
        }
    }

    public interface OnAlertListener {
        void onAlertClick(int position);
        void onAlertHold(View view, int position);
    }
}
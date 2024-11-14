package com.simonits.adalerts.objects;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class Listing implements Serializable {

    private static final String TAG = "Listing";
    private final String id;
    private String url, name, image, price, location, posted;
    private Date date;
    private String permaDateStr;
    private boolean newListing, filtered;

    public Listing(String id) {
        this.id = id;
        price = "";
        location = "";
        newListing = false;
        filtered = false;
    }

    public String getID() {
        return id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setPosted(String posted, String format) {
        this.posted = posted;
        setPermaDateStr(format);
    }

    public String getPosted() {
        return posted;
    }

    private void setPermaDateStr(String format) {
        Calendar cal = Calendar.getInstance();
        if (id.startsWith("k") || id.startsWith("c")) {
            try {
                if (id.startsWith("k")) {
                    date = new SimpleDateFormat(format){{setTimeZone(TimeZone.getTimeZone("UTC"));}}.parse(posted);
                } else {
                    date = new SimpleDateFormat("yyyy-MM-dd H:m", Locale.getDefault()).parse(posted);
                }
                permaDateStr = new SimpleDateFormat("h:mma MMM-dd", Locale.getDefault()).format(date).replace("AM", "am").replace("PM", "pm");
                posted = permaDateStr;
            } catch (ParseException e) {
                Log.e(TAG, e.toString());
            }
        } else if (id.startsWith("e")) {
            permaDateStr = posted;
            Date currentDate = cal.getTime();
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String ogPermaDateStr = permaDateStr;
            permaDateStr += " " + year;
            try {
                date = Objects.requireNonNull(new SimpleDateFormat("dd-MMM H:m yyyy", Locale.getDefault()).parse(permaDateStr));
                if (date.after(currentDate)) {
                    year--;
                    permaDateStr = ogPermaDateStr + " " + year;
                    date = new SimpleDateFormat("dd-MMM H:m yyyy", Locale.getDefault()).parse(permaDateStr);
                }
                permaDateStr = new SimpleDateFormat("h:mma MMM-dd", Locale.getDefault()).format(date).replace("AM", "am").replace("PM", "pm");
            } catch (ParseException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public String getPermaDateStr() {
        return permaDateStr;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date, String permaDateStr) {
        this.date = date;
        this.permaDateStr = permaDateStr;
    }

    public void setNewListing(boolean newListing) {
        this.newListing = newListing;
    }

    public boolean isNewListing() {
        return newListing;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public boolean isFiltered() {
        return filtered;
    }

}

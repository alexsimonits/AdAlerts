package com.simonits.adalerts.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simonits.adalerts.R;
import com.simonits.adalerts.activities.DetailsActivity;
import com.simonits.adalerts.activities.FullscreenActivity;
import com.simonits.adalerts.adapters.ViewPagerAdapter;
import com.simonits.adalerts.custom.ViewPagerFixed;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.Header;
import com.simonits.adalerts.objects.SiteParam;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.receivers.AlarmReceiver;
import com.simonits.adalerts.services.ForegroundService;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utility {

    private static final String TAG = "Utility";

    public static OkHttpClient getClient(String site) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        Gson gson = new Gson();

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .addInterceptor(chain -> {
                        final Request original = chain.request();

                        Header[] objArr;
                        if (site.equals(Constants.TABSITE_KIJIJI)) {
                            objArr = gson.fromJson(mFirebaseRemoteConfig.getString("kHeaders0_64"), Header[].class);
                        } else { // ebay
                            objArr = gson.fromJson(mFirebaseRemoteConfig.getString("eHeaders0_64"), Header[].class);
                        }
                        Request.Builder builder = original.newBuilder();
                        /*try {
                            for (Header obj : objArr) {
                                builder.addHeader(obj.getName(), obj.getValue());
                            }
                        } catch (NullPointerException e) {
                            builder.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
                        }*/
                        builder.addHeader("user-agent", "PostmanRuntime/7.32.3");
                        builder.addHeader("Cookie", "dp1=bu1p/QEBfX0BAX19AQA**6a0a382d^bl/CA6a0a382d^; ebay=%5Esbf%3D%23%5E; nonsession=CgADKACBqCjgtODg4OTFhNTQxOGYwYTI0NDliYjVjOWQ1ZmZmZGEyMDkAywABZkfYNTG1m20V; s=CgAD4ACBmSSKtODg4OTFhNTQxOGYwYTI0NDliYjVjOWQ1ZmZmZGEyMDky6ywY; ak_bmsc=888B4F8F16803DD618FBFFC4D89EB8F8~000000000000000000000000000000~YAAQBpYqFw8CzTWPAQAAqRqJiBev3VrLNDnajqY/G9VoqGQ9C3L9ECpOcj0XlkLc1M5IZpFSOv2IQ+VxKiiiQKTdGnBF0Q64Q00eaqPXmKw46hzPz0/z+0+u4UStrOGnEhL+erUtBAJjXDp5qqchAWWXPkOIiORA+wOxcvbxY8JZJuScvscGTiB5ippw5EKU4FzDNhrAE3nJsUkM82vvnHByu3vtGfTQ+pADyDADC+C7hiLI9Cm7QtEz4JCVes4JGDCSxeCq8PqC37vbIm6rrDIrf8s8+xW2BjieJMUNXShCI0X/nW2xHH4xKzjwn5S7n5YMGtRSQzTzZ4J+AKz6+V0yka7ipRLijvWcxR18RUOo0a5J4iz9JkL94NNVwFc19o0l; JSESSIONID=E3CAE662EF3ACC6B66EEBC10B75C2F90");
                        final Request authorized = builder.build();

                        return chain.proceed(authorized);
                    }).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFacebookUA() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        HashMap<String, String> map = new Gson().fromJson(
                mFirebaseRemoteConfig.getString("fNetworkStuff1_07"), new TypeToken<HashMap<String, String>>() {
                }.getType()
        );
        return map.get("userAgent");
    }

    public static String getHtml(OkHttpClient client, String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            Log.i(TAG, "Data Fetch Error!!!");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    public static HashMap<String, String> getScrapeMap(String site, String type) {
        String key;
        if (type.equals("listings")) {
            switch (site) {
                case Constants.TABSITE_KIJIJI:
                    key = "kScrapeListings1_4_16";
                    break;
                case Constants.TABSITE_EBAY:
                    key = "eScrapeListings1_4_8";
                    break;
                case Constants.TABSITE_CRAIGSLIST:
                    key = "cScrapeListings1_4_2";
                    break;
                default: // Constants.TABSITE_FACEBOOK
                    key = "fScrapeListings1_4_6";
                    break;
            }
        } else { // "details"
            switch (site) {
                case Constants.TABSITE_KIJIJI:
                    key = "kScrapeDetails1_14";
                    break;
                case Constants.TABSITE_EBAY:
                    key = "eScrapeDetails1_4_13";
                    break;
                case Constants.TABSITE_CRAIGSLIST:
                    key = "cScrapeDetails1_14";
                    break;
                default: // Constants.TABSITE_FACEBOOK
                    key = "fScrapeDetails1_4_4";
                    break;
            }
        }
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return new Gson().fromJson(
                mFirebaseRemoteConfig.getString(key), new TypeToken<HashMap<String, String>>() {
                }.getType()
        );
    }

    private static String scrapeID(String htmlStr, int startIndex, HashMap<String, String> map, String siteChar) {
        String id;
        try {
            id = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("id_end")), startIndex)).trim();
            Log.i(TAG, "ID: " + siteChar + id);
            if (id.isEmpty() || id.contains(" ") || !Character.isDigit(id.charAt(0)) || !Character.isDigit(id.charAt(id.length() - 1))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'id' is empty OR contains a space OR doesn't start with a digit OR doesn't end with a digit");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                return "error";
            }
            id = siteChar + id;
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'id' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            return "error";
        }
        return id;
    }

    public static ArrayList<Listing> scrapeKijiji(Context context, String htmlStr, Alert alert) {
        ArrayList<Listing> freshListingList = new ArrayList<>();
        HashMap<String, String> map = getScrapeMap(Constants.TABSITE_KIJIJI, "listings");

        String strFind;
        int fromIndex = 0, startIndex;
        boolean filtered = false;

        try {
            strFind = Objects.requireNonNull(map.get("entryPoint")); // this entry point covers all info per item
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            return new ArrayList<Listing>() {{
                add(new Listing("error"));
            }};
        }

        while ((fromIndex = htmlStr.indexOf(strFind, fromIndex)) != -1) {
            startIndex = fromIndex + strFind.length();
            String id = scrapeID(htmlStr, startIndex, map, "k");

            String sub;
            try {
                sub = htmlStr.substring(fromIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("sub1_end")), fromIndex));
            } catch (StringIndexOutOfBoundsException e) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'sub' StringIndexOutOfBoundsException caught");
                Log.e(TAG, e.toString());
                FirebaseCrashlytics.getInstance().recordException(e);
                return new ArrayList<Listing>() {{
                    add(new Listing("error"));
                }};
            }
            if (!id.equals("error") && !sub.contains(Objects.requireNonNull(map.get("cantContain1"))) && sub.contains(Objects.requireNonNull(map.get("mustContain1"))) && !sub.contains(Objects.requireNonNull(map.get("cantContain2"))) && sub.contains(Objects.requireNonNull(map.get("mustContain2"))) && !sub.contains(Objects.requireNonNull(map.get("cantContain3")))) {
                Listing listing = new Listing(id);

                try {
                    startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("urlTag")), fromIndex) + Objects.requireNonNull(map.get("urlTag")).length();
                    String url = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("url_end")), startIndex)).trim();
                    Log.i(TAG, "URL: " + map.get("urlBeginningStr") + url);
                    if (url.isEmpty() || url.contains(" ") || !url.startsWith(Objects.requireNonNull(map.get("url_check")))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'url' is empty OR contains a space OR doesn't start with " + map.get("url_check"));
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        return new ArrayList<Listing>() {{
                            add(new Listing("error"));
                        }};
                    }
                    url = map.get("urlBeginningStr") + url;
                    listing.setUrl(url);
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'url' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    return new ArrayList<Listing>() {{
                        add(new Listing("error"));
                    }};
                }

                String name;
                try {
                    startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("nameTag_end")), htmlStr.indexOf(Objects.requireNonNull(map.get("nameTag_start")), fromIndex)) + Objects.requireNonNull(map.get("nameTag_end")).length();
                    name = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("name_end")), startIndex)).trim();
                    name = StringEscapeUtils.unescapeJava(htmlToPlain(name, "name"));
                    Log.i(TAG, "Name: " + name);
                    if (name.isEmpty()) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'name' is empty");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        name = "-TITLE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'name' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    name = "-TITLE ERROR-";
                }
                listing.setName(name);

                String image;
                try {
                    if (sub.contains(Objects.requireNonNull(map.get("imgTag1")))) {
                        startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("imgTag1")), fromIndex) + Objects.requireNonNull(map.get("imgTag1")).length();
                        image = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("img_end1")), startIndex)).trim();
                    } else if (sub.contains(Objects.requireNonNull(map.get("imgTag2")))) {
                        startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("imgTag2")), fromIndex) + Objects.requireNonNull(map.get("imgTag2")).length();
                        image = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("img_end2")), startIndex)).trim();
                    } else {
                        image = "no image";
                    }
                    Log.i(TAG, "Image: " + image);
                    if (image.isEmpty() || (!image.equals("no image") && (image.contains(" ") || !image.startsWith(Objects.requireNonNull(map.get("img_check")))))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space OR doesn't start with " + map.get("img_check"));
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        image = "-IMAGE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    image = "-IMAGE ERROR-";
                }
                listing.setImage(image);

                String price;
                try {
                    if (sub.contains(Objects.requireNonNull(map.get("priceStr1")))) {
                        price = "Please Contact";
                    } else if (sub.contains(Objects.requireNonNull(map.get("priceStr2")))) {
                        price = "Swap/Trade";
                    } else if (sub.contains(Objects.requireNonNull(map.get("priceStr3")))) {
                        price = "Free";
                    } else {
                        startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("priceTag")), fromIndex) + Objects.requireNonNull(map.get("priceTag")).length();
                        price = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex)).trim();
                        Log.i(TAG, "Price Value: " + price);
                        if (!price.isEmpty() && !price.equals(map.get("priceStr4")) && (!price.startsWith(Objects.requireNonNull(map.get("price_check"))) || !Character.isDigit(price.charAt(price.length() - 1)))) {
                            Log.i(TAG, "HTML SCRAPE ERROR: 'price' isn't empty AND doesn't equal " + " AND doesn't equal " + map.get("priceStr4") + " AND doesn't start with " + map.get("price_check") + " OR doesn't end in a digit");
                            FirebaseCrashlytics.getInstance().recordException(new Exception());
                            price = "-PRICE ERROR-";
                        } else if (price.isEmpty() || price.equals(map.get("priceStr4"))) {
                            price = "Price n/a";
                        } else {
                            if (price.length() > 2) {
                                price = price.substring(0, price.length() - 2);
                            }
                            price = "$" + price;
                        }
                    }
                    Log.i(TAG, "Price: " + price);
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'price' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    price = "-PRICE ERROR-";
                }
                listing.setPrice(price);

                String location;
                try {
                    startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("locTag_end")), htmlStr.indexOf(Objects.requireNonNull(map.get("locTag_start")), fromIndex)) + Objects.requireNonNull(map.get("locTag_end")).length();
                    location = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("loc_end")), startIndex)).trim();
                    location = Utility.htmlToPlain(location, "");
                    Log.i(TAG, "Location: " + location);
                    if (location.isEmpty() || !Character.isLetter(location.charAt(0)) || !Character.isLetter(location.charAt(location.length() - 1))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'location' is empty OR doesn't start with a letter OR doesn't end with a letter");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        location = "-LOCATION ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'location' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    location = "-LOCATION ERROR-";
                }
                listing.setLocation(location);

                String posted;
                try {
                    startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("postedTag")), fromIndex) + Objects.requireNonNull(map.get("postedTag")).length();
                    posted = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("posted_end")), startIndex)).trim();
                    posted = posted.replace(Objects.requireNonNull(map.get("unwantedStr1")), "").replace(Objects.requireNonNull(map.get("target1")), Objects.requireNonNull(map.get("replacement1"))); // "< 18 hours ago" -> "18 hours ago"
                    Log.i(TAG, "Posted: " + posted);
                    if (posted.isEmpty() || (!Character.isLetter(posted.charAt(0)) && !Character.isDigit(posted.charAt(0))) || (!Character.isLetter(posted.charAt(posted.length() - 1)) && !Character.isDigit(posted.charAt(posted.length() - 1)))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'posted' is empty OR doesn't start with a letter/digit OR doesn't end with a letter/digit");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        posted = "-POSTED ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'posted' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    posted = "-POSTED ERROR-";
                }
                listing.setPosted(posted, map.get("posted_format"));

                try {
                    if ((listing.getDate().after(alert.getLatestListingDate(Constants.TABSITE_KIJIJI)) || listing.getDate().equals(alert.getLatestListingDate(Constants.TABSITE_KIJIJI))) && !alert.getIDsHistory(Constants.TABSITE_KIJIJI).contains(listing.getID()) && !isInListings(alert.getKListings(), listing.getID())) {
                        listing.setNewListing(true);
                    } else if (context instanceof ForegroundService && isInListings(alert.getOnlyNewBgListings(), listing.getID())) { // this means the user hasn't opened the app since their last x minute cycle notification, so if the listing is in OnlyNewBgIDs it should keep it's newListing true value
                        listing.setNewListing(true);
                    }
                    if (listing.isNewListing() && isInListings(PreferenceUtils.getNewListingsList(context), listing.getID())) {
                        listing.setNewListing(false);
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    return new ArrayList<Listing>() {{
                        add(new Listing("error"));
                    }};
                }

                listing.setFiltered(!nameCheck(listing.getName(), alert.getKInclude(), alert.getKIncludeAny(), alert.getKExclude()));

                freshListingList.add(listing);
            } else if (!id.equals("error")) {
                Log.i(TAG, "^^ REMOVED B/C AD HAS NO POSTED DATE AND/OR IS A TOP-FEATURE AD");
                filtered = true;
            }

            fromIndex++;
        }

        int size = freshListingList.size();
        removeFiltered(freshListingList);
        if (freshListingList.size() < size) {
            filtered = true;
        }
        size = freshListingList.size();
        removeSpam(context, freshListingList);
        if (freshListingList.size() < size) {
            filtered = true;
        }
        updateFavourites(context, freshListingList);

        if (freshListingList.isEmpty() && !htmlStr.contains(Objects.requireNonNull(map.get("noResults"))) && !filtered) {
            freshListingList.add(new Listing("error"));
        }

        return freshListingList;
    }

    public static ArrayList<Listing> scrapeEbay(Context context, String htmlStr, Alert alert) {
        ArrayList<Listing> freshListingList = new ArrayList<>();
        HashMap<String, String> map = getScrapeMap(Constants.TABSITE_EBAY, "listings");

        String strFind;
        int fromIndex = 0, startIndex;

        try {
            strFind = Objects.requireNonNull(map.get("entryPoint")); // this entry point covers all info per item
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            return new ArrayList<Listing>() {{
                add(new Listing("error"));
            }};
        }

        while ((fromIndex = htmlStr.indexOf(strFind, fromIndex)) != -1) {
            if (htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain1"))) || htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain2"))) || htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain3")))) {
                strFind = "An info banner was found containing: \"items found from eBay International\" OR \"Results matching fewer words\" OR zero category results was found, so this is the end of the results OR \"Did you mean: ... (x results)\"."; // this is important because it changes the search string to something not found in the html
            } else {
                String listingSub;
                try {
                    listingSub = htmlStr.substring(fromIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("endPoint")), fromIndex));
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'listingSub' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    listingSub = htmlStr.substring(fromIndex);
                }

                String url;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("url_start"))) + Objects.requireNonNull(map.get("url_start")).length();
                    url = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("url_end")), startIndex)).trim();
                    Log.i(TAG, "URL: " + url);
                    if (url.isEmpty() || url.contains(" ") || !url.startsWith(Objects.requireNonNull(map.get("url_check")))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'url' is empty OR contains a space OR doesn't start with " + map.get("url_check"));
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        return new ArrayList<Listing>() {{
                            add(new Listing("error"));
                        }};
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'url' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    return new ArrayList<Listing>() {{
                        add(new Listing("error"));
                    }};
                }

                startIndex = url.indexOf(Objects.requireNonNull(map.get("id_start")), url.indexOf(Objects.requireNonNull(map.get("url_check"))) + Objects.requireNonNull(map.get("url_check")).length()) + Objects.requireNonNull(map.get("id_start")).length();
                String id = scrapeID(url, startIndex, map, "e");
                if (id.equals("error")) {
                    return new ArrayList<Listing>() {{
                        add(new Listing("error"));
                    }};
                }
                Listing listing = new Listing(id);
                listing.setUrl(url);

                String image;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("imgTag"))) + Objects.requireNonNull(map.get("imgTag")).length();
                    image = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("img_end")), startIndex)).trim();
                    Log.i(TAG, "Image: " + image);
                    if (image.isEmpty() || image.contains(" ") || !image.startsWith(Objects.requireNonNull(map.get("img_check")))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space OR doesn't start with " + map.get("img_check"));
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        image = "-IMAGE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    image = "-IMAGE ERROR-";
                }
                listing.setImage(image);

                String name;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("nameTag"))) + Objects.requireNonNull(map.get("nameTag")).length();
                    name = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("name_end")), startIndex)).trim();
                    if (name.contains(Objects.requireNonNull(map.get("cantContain4"))) || name.contains(Objects.requireNonNull(map.get("cantContain6")))) {
                        name = name.replace(Objects.requireNonNull(map.get("cantContain4")), "").replace(Objects.requireNonNull(map.get("cantContain6")), "").trim();
                    }
                    name = htmlToPlain(name, "name");
                    Log.i(TAG, "Name: " + name);
                    if (name.isEmpty()) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'name' is empty");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        name = "-TITLE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'name' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    name = "-TITLE ERROR-";
                }
                listing.setName(name);

                String price;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("priceTag"))) + Objects.requireNonNull(map.get("priceTag")).length();
                    price = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex));
                    price = price.replace(Objects.requireNonNull(map.get("target1")), Objects.requireNonNull(map.get("replacement1"))).replace(Objects.requireNonNull(map.get("target2")), Objects.requireNonNull(map.get("replacement2"))).replace(Objects.requireNonNull(map.get("target3")), Objects.requireNonNull(map.get("replacement3"))).trim();
                    if (listingSub.contains(Objects.requireNonNull(map.get("priceFormat1_html")))) {
                        price = price + map.get("priceFormat1_str");
                    } else if (listingSub.contains(Objects.requireNonNull(map.get("priceFormat2_html")))) {
                        price = price + map.get("priceFormat2_str");
                    } else if (listingSub.contains(Objects.requireNonNull(map.get("priceFormat4_html")))) {
                        price = price + map.get("priceFormat4_str");
                    }
                    if (listingSub.contains(Objects.requireNonNull(map.get("priceFormat3_html")))) {
                        price = price + map.get("priceFormat3_str");
                    } else if (listingSub.contains(Objects.requireNonNull(map.get("priceFormat5_html")))) {
                        price = price + map.get("priceFormat5_str");
                    }
                    Log.i(TAG, "Price: " + price);
                    if (price.isEmpty() || (!price.contains(Objects.requireNonNull(map.get("priceFormat1_str"))) && !price.contains(Objects.requireNonNull(map.get("priceFormat2_str"))) && !price.contains(Objects.requireNonNull(map.get("priceFormat3_str"))) && !price.contains(Objects.requireNonNull(map.get("priceFormat4_str"))) && !price.contains(Objects.requireNonNull(map.get("priceFormat5_str"))) && !price.startsWith(Objects.requireNonNull(map.get("price_check"))))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'price' is empty OR doesn't contain " + map.get("priceFormat1_str") + " AND doesn't contain " + map.get("priceFormat2_str") + " AND doesn't contain " + map.get("priceFormat3_str") + " AND doesn't contain " + map.get("priceFormat4_str") + " AND doesn't contain " + map.get("priceFormat5_str") + " AND doesn't start with " + map.get("price_check"));
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        price = "-PRICE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'price' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    price = "-PRICE ERROR-";
                }
                listing.setPrice(price);

                String posted;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("postedTag"))) + Objects.requireNonNull(map.get("postedTag")).length();
                    posted = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("posted_end")), startIndex)).trim();
                    Log.i(TAG, "Posted: " + posted);
                    if ((posted.isEmpty() || !Character.isDigit(posted.charAt(0)) || !Character.isDigit(posted.charAt(posted.length() - 1))) && !posted.equals(map.get("postedExtra"))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'posted' is empty OR doesn't start with a digit OR doesn't end with a digit");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        posted = "-POSTED ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'posted' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    posted = "-POSTED ERROR-";
                }
                if (!posted.equals(map.get("postedExtra"))) {
                    listing.setPosted(posted, "");

                    try {
                        if ((listing.getDate().after(alert.getLatestListingDate(Constants.TABSITE_EBAY)) || listing.getDate().equals(alert.getLatestListingDate(Constants.TABSITE_EBAY))) && !alert.getIDsHistory(Constants.TABSITE_EBAY).contains(listing.getID()) && !isInListings(alert.getEListings(), listing.getID())) {
                            listing.setNewListing(true);
                        } else if (context instanceof ForegroundService && isInListings(alert.getOnlyNewBgListings(), listing.getID())) { // this means the user hasn't opened the app since their last 15 minute cycle notification, so if the listing is in OnlyNewBgIDs it should keep it's newListing true value
                            listing.setNewListing(true);
                        }
                        if (listing.isNewListing() && isInListings(PreferenceUtils.getNewListingsList(context), listing.getID())) {
                            listing.setNewListing(false);
                        }
                    } catch (NullPointerException e) {
                        Log.e(TAG, e.toString());
                        FirebaseCrashlytics.getInstance().recordException(e);
                        return new ArrayList<Listing>() {{
                            add(new Listing("error"));
                        }};
                    }

                    listing.setFiltered(!nameCheck(listing.getName(), alert.getEInclude(), alert.getEIncludeAny(), alert.getEExclude()));

                    freshListingList.add(listing);
                } else {
                    Log.i(TAG, "^^ REMOVED B/C AD IS RELISTED");
                }
                fromIndex++;
            }
        }

        removeSpam(context, removeFiltered(freshListingList));
        updateFavourites(context, freshListingList);

        return freshListingList;
    }

    public static ArrayList<Listing> scrapeCraigslist(Context context, String htmlStr, Alert alert) {
        ArrayList<Listing> freshListingList = new ArrayList<>();
        HashMap<String, String> map = getScrapeMap(Constants.TABSITE_CRAIGSLIST, "listings");

        String strFind;
        int fromIndex = 0, startIndex;

        try {
            strFind = Objects.requireNonNull(map.get("entryPoint")); // this entry point covers all info per item
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            return new ArrayList<Listing>() {{
                add(new Listing("error"));
            }};
        }

        while ((fromIndex = htmlStr.indexOf(strFind, fromIndex)) != -1) {
            if (htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain1"))) || htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain2")))) {
                strFind = "\"Few local results found\" was found, so this is the end of the results."; // this is important because it changes the search string to something not found in the html
            } else {
                String listingSub;
                try {
                    listingSub = htmlStr.substring(fromIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("endPoint")), fromIndex));
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'listingSub' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    listingSub = htmlStr.substring(fromIndex);
                }

                startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("idTag"))) + Objects.requireNonNull(map.get("idTag")).length();
                String id = scrapeID(listingSub, startIndex, map, "c");
                if (id.equals("error")) {
                    return new ArrayList<Listing>() {{
                        add(new Listing("error"));
                    }};
                }
                Listing listing = new Listing(id);

                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("urlTag"))) + Objects.requireNonNull(map.get("urlTag")).length();
                    String url = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("url_end")), startIndex)).trim();
                    Log.i(TAG, "URL: " + url);
                    if (url.isEmpty() || url.contains(" ") || !url.startsWith(Objects.requireNonNull(map.get("url_check")))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'url' is empty OR contains a space OR doesn't start with " + map.get("url_check"));
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        return new ArrayList<Listing>() {{
                            add(new Listing("error"));
                        }};
                    }
                    listing.setUrl(url);
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'url' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    return new ArrayList<Listing>() {{
                        add(new Listing("error"));
                    }};
                }

                String name;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("name_start")), listingSub.indexOf(Objects.requireNonNull(map.get("nameTag")))) + Objects.requireNonNull(map.get("name_start")).length();
                    name = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("name_end")), startIndex)).trim();
                    name = htmlToPlain(name, "name");
                    Log.i(TAG, "Name: " + name);
                    if (name.isEmpty()) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'name' is empty");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        name = "-TITLE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'name' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    name = "-TITLE ERROR-";
                }
                listing.setName(name);

                String image;
                try {
                    if (listingSub.contains(Objects.requireNonNull(map.get("noImgHtml")))) {
                        image = "no image";
                    } else {
                        startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("imgTag"))) + Objects.requireNonNull(map.get("imgTag")).length();
                        image = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("img_end")), startIndex));
                        if (image.contains(Objects.requireNonNull(map.get("imgSeparator")))) {
                            image = image.substring(0, image.indexOf(Objects.requireNonNull(map.get("imgSeparator"))));
                        }
                        image = image.substring(image.indexOf(Objects.requireNonNull(map.get("img_start"))) + 1);
                        image = map.get("imgUrl_startStr") + image + map.get("imgUrl_endStr");
                    }
                    Log.i(TAG, "Image: " + image);
                    if (image.isEmpty() || (!image.equals("no image") && image.contains(" "))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        image = "-IMAGE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    image = "-IMAGE ERROR-";
                }
                listing.setImage(image);

                String price;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("priceTag"))) + Objects.requireNonNull(map.get("priceTag")).length();
                    price = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex)).trim();
                    Log.i(TAG, "Price: " + price);
                    if (price.isEmpty() || !price.startsWith(Objects.requireNonNull(map.get("price_check"))) || !Character.isDigit(price.charAt(price.length() - 1))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'price' is empty OR doesn't start with " + map.get("price_check") + " OR doesn't end in a digit");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        price = "-PRICE ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'price' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    price = "-PRICE ERROR-";
                }
                listing.setPrice(price);

                String location;
                try {
                    if (listingSub.contains(Objects.requireNonNull(map.get("locHtml")))) {
                        startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("locTag"))) + Objects.requireNonNull(map.get("locTag")).length();
                        location = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("loc_end")), startIndex)).trim();
                        location = location.replace(Objects.requireNonNull(map.get("unwantedStr2")), "").replace(Objects.requireNonNull(map.get("unwantedStr3")), "").replace(Objects.requireNonNull(map.get("target2")), Objects.requireNonNull(map.get("replacement2")));
                        location = htmlToPlain(location, "name"); // this is because the poster could enter their own text for location
                        Log.i(TAG, "Location: " + location);
                        if (location.isEmpty()) {
                            Log.i(TAG, "HTML SCRAPE ERROR: 'location' is empty");
                            FirebaseCrashlytics.getInstance().recordException(new Exception());
                            location = "-LOCATION ERROR-";
                        }
                    } else {
                        Log.i(TAG, "Location: None, so using the parent value: " + alert.getCLoc());
                        location = alert.getCLoc();
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'location' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    location = "-LOCATION ERROR-";
                }
                listing.setLocation(location);

                String posted;
                try {
                    startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("postedTag"))) + Objects.requireNonNull(map.get("postedTag")).length();
                    posted = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("posted_end")), startIndex)).trim();
                    Log.i(TAG, "Posted: " + posted);
                    if (posted.isEmpty() || !Character.isDigit(posted.charAt(0)) || !Character.isDigit(posted.charAt(posted.length() - 1))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'posted' is empty OR doesn't start with a digit OR doesn't end with a digit");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        posted = "-POSTED ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'posted' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    posted = "-POSTED ERROR-";
                }
                listing.setPosted(posted, "");

                try {
                    if ((listing.getDate().after(alert.getLatestListingDate(Constants.TABSITE_CRAIGSLIST)) || listing.getDate().equals(alert.getLatestListingDate(Constants.TABSITE_CRAIGSLIST))) && !alert.getIDsHistory(Constants.TABSITE_CRAIGSLIST).contains(listing.getID()) && !isInListings(alert.getCListings(), listing.getID())) {
                        listing.setNewListing(true);
                    } else if (context instanceof ForegroundService && isInListings(alert.getOnlyNewBgListings(), listing.getID())) { // this means the user hasn't opened the app since their last 15 minute cycle notification, so if the listing is in OnlyNewBgIDs it should keep it's newListing true value
                        listing.setNewListing(true);
                    }
                    if (listing.isNewListing() && isInListings(PreferenceUtils.getNewListingsList(context), listing.getID())) {
                        listing.setNewListing(false);
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    return new ArrayList<Listing>() {{
                        add(new Listing("error"));
                    }};
                }

                listing.setFiltered(!nameCheck(listing.getName(), alert.getCInclude(), alert.getCIncludeAny(), alert.getCExclude()));

                freshListingList.add(listing);
                fromIndex++;
            }
        }

        removeSpam(context, removeFiltered(freshListingList));
        updateFavourites(context, freshListingList);

        return freshListingList;
    }

    public static ArrayList<Listing> scrapeFacebook(Context context, String htmlStr, Alert alert) {
        ArrayList<Listing> freshListingList = new ArrayList<>();
        HashMap<String, String> map = getScrapeMap(Constants.TABSITE_FACEBOOK, "listings");

        String strFind;
        int fromIndex = 0, startIndex;
        boolean filtered = false;
        boolean tooOld = false;

        try {
            strFind = Objects.requireNonNull(map.get("entryPoint")); // this entry point covers all info per item
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            return new ArrayList<Listing>() {{
                add(new Listing("error"));
            }};
        }

        if (htmlStr.contains(Objects.requireNonNull(map.get("accountTimeOut")))) {
            return new ArrayList<Listing>() {{
                add(new Listing("accountTimeOut"));
            }};
        } else if (!htmlStr.contains(Objects.requireNonNull(map.get("noListingsHtml1"))) && !htmlStr.contains(Objects.requireNonNull(map.get("noListingsHtml2"))) && !htmlStr.contains(Objects.requireNonNull(map.get("noListingsHtml3"))) && !htmlStr.contains(Objects.requireNonNull(map.get("cantContain3")))) {
            Calendar cal = Calendar.getInstance();
            while ((fromIndex = htmlStr.indexOf(strFind, fromIndex)) != -1) {
                if (htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain1"))) || htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain2"))) || htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain4"))) || htmlStr.substring(0, fromIndex).contains(Objects.requireNonNull(map.get("cantContain9")))) {
                    strFind = "\">You Might Also Like<\" was found OR \">Results From Outside Your Search<\" was found, so this is the end of the results."; // this is important because it changes the search string to something not found in the html
                } else {
                    String listingSub;
                    try {
                        listingSub = htmlStr.substring(fromIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("endPoint")), fromIndex));
                    } catch (StringIndexOutOfBoundsException e) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'listingSub' StringIndexOutOfBoundsException caught");
                        Log.e(TAG, e.toString());
                        FirebaseCrashlytics.getInstance().recordException(e);
                        listingSub = htmlStr.substring(fromIndex);
                    }

                    if (!listingSub.contains(Objects.requireNonNull(map.get("cantContain5"))) && !listingSub.contains(Objects.requireNonNull(map.get("cantContain6"))) && !listingSub.contains(Objects.requireNonNull(map.get("cantContain7"))) && !listingSub.contains(Objects.requireNonNull(map.get("cantContain8")))) {
                        startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("idTag"))) + Objects.requireNonNull(map.get("idTag")).length();
                        String id = scrapeID(listingSub, startIndex, map, "f");
                        if (id.equals("error")) {
                            return new ArrayList<Listing>() {{
                                add(new Listing("error"));
                            }};
                        }
                        Listing listing = new Listing(id);

                        String image;
                        try {
                            startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("imgTag"))) + Objects.requireNonNull(map.get("imgTag")).length();
                            image = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("img_end")), startIndex)).trim();
                            image = htmlToPlain(image, Constants.TABSITE_FACEBOOK);
                            Log.i(TAG, "Image: " + image);
                            if (image.isEmpty() || image.contains(" ") || !image.startsWith(Objects.requireNonNull(map.get("img_check")))) {
                                Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space OR doesn't start with " + map.get("img_check"));
                                FirebaseCrashlytics.getInstance().recordException(new Exception());
                                image = "-IMAGE ERROR-";
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
                            Log.e(TAG, e.toString());
                            FirebaseCrashlytics.getInstance().recordException(e);
                            image = "-IMAGE ERROR-";
                        }
                        listing.setImage(image);

                        String price;
                        try {
                            startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("priceTag"))) + Objects.requireNonNull(map.get("priceTag")).length();
                            price = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex)).trim();
                            Log.i(TAG, "Price: " + price);
                            if (price.isEmpty()) {
                                Log.i(TAG, "HTML SCRAPE ERROR: 'price' is empty");
                                FirebaseCrashlytics.getInstance().recordException(new Exception());
                                price = "-PRICE ERROR-";
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            Log.i(TAG, "HTML SCRAPE ERROR: 'price' StringIndexOutOfBoundsException caught");
                            Log.e(TAG, e.toString());
                            FirebaseCrashlytics.getInstance().recordException(e);
                            price = "-PRICE ERROR-";
                        }
                        listing.setPrice(price);

                        String location;
                        try {
                            startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("locTag"))) + Objects.requireNonNull(map.get("locTag")).length();
                            location = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("loc_end")), startIndex)).trim();
                            location = Utility.htmlToPlain(location, Constants.TABSITE_FACEBOOK);
                            Log.i(TAG, "Location: " + location);
                            if (location.isEmpty() || !Character.isLetter(location.charAt(0)) || !Character.isLetter(location.charAt(location.length() - 1))) {
                                Log.i(TAG, "HTML SCRAPE ERROR: 'location' is empty OR doesn't start with a letter OR doesn't end with a letter");
                                FirebaseCrashlytics.getInstance().recordException(new Exception());
                                location = "-LOCATION ERROR-";
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            Log.i(TAG, "HTML SCRAPE ERROR: 'location' StringIndexOutOfBoundsException caught");
                            Log.e(TAG, e.toString());
                            FirebaseCrashlytics.getInstance().recordException(e);
                            location = "-LOCATION ERROR-";
                        }
                        listing.setLocation(location);

                        String name;
                        try {
                            startIndex = listingSub.indexOf(Objects.requireNonNull(map.get("nameTag"))) + Objects.requireNonNull(map.get("nameTag")).length();
                            name = listingSub.substring(startIndex, listingSub.indexOf(Objects.requireNonNull(map.get("name_end")), startIndex)).trim();
                            name = htmlToPlain(name, Constants.TABSITE_FACEBOOK);
                            Log.i(TAG, "Name: " + name);
                            if (name.isEmpty()) {
                                Log.i(TAG, "HTML SCRAPE ERROR: 'name' is empty");
                                FirebaseCrashlytics.getInstance().recordException(new Exception());
                                name = "-TITLE ERROR-";
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            Log.i(TAG, "HTML SCRAPE ERROR: 'name' StringIndexOutOfBoundsException caught");
                            Log.e(TAG, e.toString());
                            FirebaseCrashlytics.getInstance().recordException(e);
                            name = "-TITLE ERROR-";
                        }
                        listing.setName(name);

                        String url = Objects.requireNonNull(map.get("urlString")).replace("ID_GOES_HERE", id.substring(1));
                        Log.i(TAG, "URL: " + url);
                        listing.setUrl(url);

                        listing.setPosted("???", "");
                        cal.add(Calendar.SECOND, -1);
                        listing.setDate(cal.getTime(), "???");

                        if (!tooOld && !isInListings(alert.getFListingHistory(), listing.getID())) { // true if it's a new listing
                            listing.setNewListing(true);
                        } else if (context instanceof ForegroundService && isInListings(alert.getOnlyNewBgListings(), listing.getID())) { // this means the user hasn't opened the app since their last 15 minute cycle notification, so if the listing is in OnlyNewBgIDs it should keep it's newListing true value
                            listing.setNewListing(true);
                        } else {
                            tooOld = true;
                        }
                        if (listing.isNewListing() && isInListings(PreferenceUtils.getNewListingsList(context), listing.getID())) {
                            listing.setNewListing(false);
                        }

                        listing.setFiltered(!nameCheck(listing.getName(), alert.getFInclude(), alert.getFIncludeAny(), alert.getFExclude()));
                        freshListingList.add(listing);
                    } else {
                        filtered = true;
                    }
                    fromIndex++;
                }
            }
        } else {
            freshListingList.add(new Listing("no listings found"));
        }

        if (filtered && freshListingList.isEmpty()) {
            freshListingList.add(new Listing("no listings found"));
        }

        Collections.sort(freshListingList, (obj1, obj2) -> Boolean.compare(obj2.isNewListing(), obj1.isNewListing()));

        return freshListingList;
    }

    private static boolean nameCheck(String name, String includeAll, String includeAny, String exclude) {
        if (name.equals("-TITLE ERROR-")) {
            return true;
        }
        name = name.toLowerCase();
        if (!includeAll.isEmpty()) {
            includeAll = includeAll.toLowerCase();
            String[] includeArray = includeAll.split(", ");
            for (String includeStr : includeArray) {
                if (!name.contains(includeStr)) {
                    return false;
                }
            }
        }
        if (!includeAny.isEmpty()) {
            boolean containsAny = false;
            includeAny = includeAny.toLowerCase();
            String[] includeArray = includeAny.split(", ");
            for (String includeStr : includeArray) {
                if (name.contains(includeStr)) {
                    containsAny = true;
                    break;
                }
            }
            if (!containsAny) {
                return false;
            }
        }
        if (!exclude.isEmpty()) {
            exclude = exclude.toLowerCase();
            String[] excludeArray = exclude.split(", ");
            for (String excludeStr : excludeArray) {
                if (name.contains(excludeStr)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String htmlToPlain(String html, String type) {
        if (type.equals("desc")) {
            boolean next = false;
            while (!next) {
                if (html.contains("<style>")) {
                    int startIndex = html.indexOf("<style>");
                    int endIndex = html.indexOf("</style>") + 8;
                    html = html.replace(html.substring(startIndex, endIndex), "");
                } else if (html.contains("<style ")) {
                    int startIndex = html.indexOf("<style ");
                    int endIndex = html.indexOf("</style>") + 8;
                    html = html.replace(html.substring(startIndex, endIndex), "");
                } else {
                    next = true;
                }
            }
            html = html.replaceAll("<p>", "<p>\\\\n\\\\n").replaceAll("<span", "\\\\n\\\\n<span").replaceAll("<div", "\\\\n\\\\n<div").replaceAll("<li", "\\\\n-<li");
        }
        String plain = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").trim();
        plain = plain.replaceAll("&#x27;", "'").replaceAll("&#039;", "'").replaceAll("&#39;", "'").replaceAll("&#034;", "\"").replaceAll("&quot;", "\"").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&").trim();
        if (type.equals("name")) {
            plain = plain.replaceAll("\n", "").replaceAll(" +", " ");
        } else if (type.equals("desc")) {
            plain = plain.replaceAll("\\\\n", "\n").trim();
        } else if (type.equals(Constants.TABSITE_FACEBOOK) && !plain.isEmpty()) {
            Pattern p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
            Matcher m = p.matcher(plain);
            StringBuffer buf = new StringBuffer(plain.length());
            while (m.find()) {
                String ch = String.valueOf((char) Integer.parseInt(Objects.requireNonNull(m.group(1)), 16));
                m.appendReplacement(buf, Matcher.quoteReplacement(ch));
            }
            m.appendTail(buf);
            plain = buf.toString();

            plain = plain.replaceAll("\\\\n", "NewLineCharacter").replaceAll("\\\\/", "/").replaceAll("\\\\", "\"").replaceAll("NewLineCharacter", "\n");
        }
        return plain;
    }

    public static ArrayList<Listing> removeSpam(Context context, ArrayList<Listing> list) {
        Log.i(TAG, " ");
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (PreferenceUtils.spamCheck(list.get(i).getImage(), context) || PreferenceUtils.spamCheck(list.get(i).getName(), context)) {
                Log.i(TAG, "REMOVED B/C MARKED AS SPAM:");
                Log.i(TAG, list.get(i).getID());
                Log.i(TAG, list.get(i).getUrl());
                Log.i(TAG, list.get(i).getName());
                Log.i(TAG, list.get(i).getImage());
                Log.i(TAG, list.get(i).getPrice());
                Log.i(TAG, list.get(i).getLocation());
                Log.i(TAG, list.get(i).getPosted());
                Log.i(TAG, " ");
                list.remove(i);
                i--;
                size--;
            }
        }
        return list;
    }

    public static ArrayList<Listing> removeFiltered(ArrayList<Listing> list) {
        Log.i(TAG, " ");
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).isFiltered()) {
                Log.i(TAG, "vvvv FILTERED OUT");
                Log.i(TAG, list.get(i).getID());
                Log.i(TAG, list.get(i).getUrl());
                Log.i(TAG, list.get(i).getName());
                Log.i(TAG, list.get(i).getImage());
                Log.i(TAG, list.get(i).getPrice());
                Log.i(TAG, list.get(i).getLocation());
                Log.i(TAG, list.get(i).getPosted());
                Log.i(TAG, " ");
                list.remove(i);
                i--;
                size--;
            }
        }
        return list;
    }

    public static void updateFavourites(Context context, ArrayList<Listing> freshListingObjList) {
        ArrayList<Listing> favs = PreferenceUtils.getFavsList(context);
        for (int i = 0; i < favs.size(); i++) {
            if (isInListings(freshListingObjList, favs.get(i).getID())) {
                Listing oldFavListing = favs.remove(i);
                Listing newFavListing = Objects.requireNonNull(getListingFromID(freshListingObjList, oldFavListing.getID()));
                newFavListing.setPosted(oldFavListing.getPosted(), "");
                favs.add(i, newFavListing);
            }
        }
        PreferenceUtils.saveFavsList(favs, context);
    }

    public static void sliderDots(Context context, int startPosition, ViewPagerAdapter adapter, ViewPagerFixed viewPager) {
        LinearLayout sliderDotspanel;
        if (context instanceof FullscreenActivity) {
            sliderDotspanel = ((FullscreenActivity) context).findViewById(R.id.fullscreen_content_controls);
        } else {
            sliderDotspanel = ((DetailsActivity) context).findViewById(R.id.SliderDots);
        }
        int dotscount = adapter.getCount();
        ImageView[] dots = new ImageView[dotscount];

        for (int i = 0; i < dotscount; i++) {
            dots[i] = new ImageView(context);
            dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.nonactive_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            sliderDotspanel.addView(dots[i], params);

        }

        dots[startPosition].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dotscount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.nonactive_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public static SiteParam getObjFromTree(SiteParam[] array, String name) {
        for (SiteParam siteParam : array) {
            SiteParam obj = siteParam;
            if (obj.getName().equals(name)) {
                return obj;
            }
            if (obj.hasChildren()) {
                obj = getObjFromTree(obj.getChildren(), name);
                if (obj != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    public static ArrayList<String> getListingsIDs(ArrayList<Listing> listings) {
        ArrayList<String> ids = new ArrayList<>();
        for (Listing listing : listings) {
            ids.add(listing.getID());
        }
        return ids;
    }

    public static boolean isInListings(ArrayList<Listing> listings, String id) {
        for (Listing listing : listings) {
            if (listing.getID().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static Listing getListingFromID(ArrayList<Listing> listings, String id) {
        for (Listing listing : listings) {
            if (listing.getID().equals(id)) {
                return listing;
            }
        }
        Log.i(TAG, "getListingFromID could not find a listing with the id: " + id);
        return null;
    }

    public synchronized static void updateSavedPrefs(Context context, int section, String tabSite, int action, ArrayList<Listing> listings, int alertId, Alert alert) {
        if (action == 0) {
            ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
            for (Alert alertX : alertList) {
                if (alertX.hasKijiji()) {
                    if (alertX.hasNewListings(Constants.TABSITE_KIJIJI) || !alertX.getOnlyNewBgListings(Constants.TABSITE_KIJIJI).isEmpty()) {
                        alertX.setNewListingsFalse(Constants.TABSITE_KIJIJI);
                        alertX.removeOnlyNewBgListings(Constants.TABSITE_KIJIJI);
                    }
                }
                if (alertX.hasEbay()) {
                    if (alertX.hasNewListings(Constants.TABSITE_EBAY) || !alertX.getOnlyNewBgListings(Constants.TABSITE_EBAY).isEmpty()) {
                        alertX.setNewListingsFalse(Constants.TABSITE_EBAY);
                        alertX.removeOnlyNewBgListings(Constants.TABSITE_EBAY);
                    }
                }
                if (alertX.hasCraigslist()) {
                    if (alertX.hasNewListings(Constants.TABSITE_CRAIGSLIST) || !alertX.getOnlyNewBgListings(Constants.TABSITE_CRAIGSLIST).isEmpty()) {
                        alertX.setNewListingsFalse(Constants.TABSITE_CRAIGSLIST);
                        alertX.removeOnlyNewBgListings(Constants.TABSITE_CRAIGSLIST);
                    }
                }
                if (alertX.hasFacebook()) {
                    if (alertX.hasNewListings(Constants.TABSITE_FACEBOOK) || !alertX.getOnlyNewBgListings(Constants.TABSITE_FACEBOOK).isEmpty()) {
                        alertX.setNewListingsFalse(Constants.TABSITE_FACEBOOK);
                        alertX.removeOnlyNewBgListings(Constants.TABSITE_FACEBOOK);
                    }
                }
            }
            PreferenceUtils.saveAlertList(alertList, context);
        } else if (action == -1) {
            ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
            for (Alert alertX : alertList) {
                if (alertX.hasFacebook() && !alertX.getFListingHistory().isEmpty() && !alertX.getFListingHistory().get(0).getID().equals("no listings found")) {
                    alertX.setFListingHistory(removeSpam(context, alertX.getFListingHistory()));
                }
            }
            PreferenceUtils.saveAlertList(alertList, context);
        } else {
            if (section == Constants.UTILITY_TABBEDACTIVITY || action == 8) {
                boolean needsUpdate = false;
                Alert a = Objects.requireNonNull(getAlertFromList(PreferenceUtils.getAlertList(context), alertId));
                if (action == 1) {
                    if (a.hasNewListings(tabSite) || !a.getOnlyNewBgListings(tabSite).isEmpty()) {
                        a.setNewListingsFalse(tabSite);
                        a.removeOnlyNewBgListings(tabSite);
                        needsUpdate = true;
                    }
                } else if (action == 2) {
                    a.setListingsToFreshListings(tabSite, listings); // this in turn adds new listings got from TabFragment to the old listings
                    needsUpdate = true;
                } else if (action == 3) {
                    if (a.getFListingHistory().isEmpty()) {
                        a.setFListingHistory(new ArrayList<Listing>() {{
                            add(new Listing("no listings found"));
                        }});
                        needsUpdate = true;
                    }
                } else if (action == 4) {
                    if (!a.getFListingHistory().isEmpty() && a.getFListingHistory().get(0).getID().equals("no listings found")) {
                        a.setFListingHistory(new ArrayList<>());
                    }
                    a.addToFListingHistory(listings);
                    needsUpdate = true;
                } else if (action == 5) {
                    for (Listing listing : listings) {
                        a.addOnlyNewBgListing(listing);
                    }
                    needsUpdate = true;
                } else if (action == 6) {
                    a.refreshUpdateDate();
                    needsUpdate = true;
                } else if (action == 7) {
                    a.setFListingHistory(removeSpam(context, a.getFListingHistory()));
                    needsUpdate = true;
                } else if (action == 8) {
                    a = alert;
                    needsUpdate = true;
                }
                if (needsUpdate) {
                    ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
                    int alertPosition = getAlertPosition(alertList, alertId);
                    alertList.remove(alertPosition);
                    alertList.add(alertPosition, a);
                    PreferenceUtils.saveAlertList(alertList, context);
                }
            } else if (section == Constants.UTILITY_MAINACTIVITY) {
                ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
                if (action == 1) { // reorder alerts
                    for (int i = alertList.size() - 1; i >= 0; i--) {
                        boolean done = true;
                        for (int j = i; j >= 0; j--) {
                            if (alertList.get(j).getOnlyNewBgListings().isEmpty()) {
                                done = false;
                                break;
                            }
                        }
                        if (!done && !alertList.get(i).getOnlyNewBgListings().isEmpty()) {
                            alertList.add(0, alertList.remove(i));
                            i++;
                        }
                    }
                } else if (action == 2) { // change alertIds for alerts after deleted alert has been removed
                    for (int i = 1; i <= alertList.size() - alertId + 1; i++) {
                        for (Alert a : alertList) {
                            if (a.getId() == alertId + i) {
                                a.setId(a.getId() - 1);
                                break;
                            }
                        }
                    }
                } else if (action == 3) { // delete alert
                    alertList.remove(getAlertPosition(alertList, alertId));
                } else if (action == 4) {
                    alertList.get(getAlertPosition(alertList, alertId)).setCustomName(tabSite);
                } else if (action == 5) {
                    alertList.add(alert);
                } else if (action == 6) {
                    int alertPosition = getAlertPosition(alertList, alertId);
                    alertList.remove(alertPosition);
                    alertList.add(alertPosition, alert);
                }
                PreferenceUtils.saveAlertList(alertList, context);
            } else {
                ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
                if (action == 4) {
                    int size = alertList.size();
                    for (int i = 0; i < size; i++) {
                        alertList.get(i).disableFacebook();
                        if (!alertList.get(i).hasKijiji() && !alertList.get(i).hasEbay() && !alertList.get(i).hasCraigslist()) {
                            alertList.remove(i);
                            size--;
                            i--;
                        }
                    }
                }
                for (Alert a : alertList) {
                    if (a.getId() == alertId) {
                        if (action == 1) {
                            switch (tabSite) {
                                case Constants.TABSITE_KIJIJI:
                                    a.setKListings(listings);
                                    break;
                                case Constants.TABSITE_EBAY:
                                    a.setEListings(listings);
                                    break;
                                case Constants.TABSITE_CRAIGSLIST:
                                    a.setCListings(listings);
                                    break;
                                case Constants.TABSITE_FACEBOOK:
                                    if (listings == null) {
                                        if (a.getFListingHistory().isEmpty()) {
                                            a.setFListingHistory(new ArrayList<Listing>() {{
                                                add(new Listing("no listings found"));
                                            }});
                                        }
                                    } else {
                                        if (!a.getFListingHistory().isEmpty() && a.getFListingHistory().get(0).getID().equals("no listings found")) {
                                            a.setFListingHistory(new ArrayList<>());
                                        }
                                        a.addToFListingHistory(listings);
                                    }
                                    break;
                            }
                            a.refreshUpdateDate();
                        } else if (action == 2) {
                            a.addOnlyNewBgListing(listings.get(0));
                        } else if (action == 3) {
                            a.removeOnlyNewBgListing(listings.get(0));
                        }
                        break;
                    }
                }
                PreferenceUtils.saveAlertList(alertList, context);
            }
        }
    }

    public static Alert getAlertFromList(ArrayList<Alert> alertList, int id) {
        for (Alert alert : alertList) {
            if (alert.getId() == id) {
                return alert;
            }
        }
        return null;
    }

    public static int getAlertPosition(ArrayList<Alert> alertList, int alertID) {
        for (int i = 0; i < alertList.size(); i++) {
            if (alertList.get(i).getId() == alertID) {
                return i;
            }
        }
        return -1; // to purposely produce an error if it gets here
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public static void removeAllFacebook(Context context) {
        updateSavedPrefs(context, 0, null, 4, null, 0, null);
    }

    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setCheckAlarm(Context context, boolean freshAlarm) {
        Calendar calendar = Calendar.getInstance();
        if (freshAlarm) {
            PreferenceUtils.setCheckTime(calendar.getTimeInMillis() + 60 * 1000, context); // checkTimes are now 1 minute intervals: 1 * 60 * 1000
        }

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert manager != null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, PreferenceUtils.getCheckTime(context), pendingIntent);
        }

        String frequencyKey = getFrequencyKey(context);
        Log.i(TAG, "Frequency Key is " + frequencyKey);
        FirebaseCrashlytics.getInstance().log("Frequency Key is " + frequencyKey);
        String frequency = PreferenceUtils.optionGetFrequency(frequencyKey, context);
        if (frequency.equals(Constants.FREQUENCY_NEVER)) {
            Log.i(TAG, "Next Data Fetch is Never");
            FirebaseCrashlytics.getInstance().log("Next Data Fetch is Never");
        } else {
            calendar.setTimeInMillis(PreferenceUtils.getPreviousFetchTime(context) + getMilliseconds(frequency));
            SimpleDateFormat sdf = new SimpleDateFormat("H:mm dd/M/yyyy", Locale.getDefault());
            String time = sdf.format(calendar.getTime());
            Log.i(TAG, "Next Data Fetch is " + time);
            FirebaseCrashlytics.getInstance().log("Next Data Fetch is " + time);
        }
    }

    public static void cancelDataFetchAlarm(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, alarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert manager != null;
        manager.cancel(pendingIntent);
        Log.i(TAG, "Data Fetch Alarm Cancelled");
        FirebaseCrashlytics.getInstance().log("Data Fetch Alarm Cancelled");
    }

    public static String getFrequencyKey(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;

        String networkType = Constants.KEY_SETTING_WIFIFREQUENCY;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            final NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null) {
                Log.i(TAG, "NETWORKINFO IS NULL!!");
                return Constants.NO_FREQUENCY_KEY;
            } else {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    networkType = Constants.KEY_SETTING_MOBILEFREQUENCY;
                }
            }
        } else {
            final Network n = cm.getActiveNetwork();
            if (n == null) {
                Log.i(TAG, "NETWORK IS NULL!!");
                return Constants.NO_FREQUENCY_KEY;
            } else {
                final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                if (nc == null) {
                    Log.i(TAG, "NETWORK IS NULL!!");
                    return Constants.NO_FREQUENCY_KEY;
                }
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    networkType = Constants.KEY_SETTING_MOBILEFREQUENCY;
                }
            }
        }

        String otherType = "";
        boolean dnd = ((NotificationManager) Objects.requireNonNull(context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))).getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = Objects.requireNonNull(intent).getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean charging = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        boolean battery = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).isPowerSaveMode();
        if (dnd || battery || charging) {
            String dndFrequency = "0 Minute";
            if (dnd) {
                dndFrequency = PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_DNDFREQUENCY, context);
            }
            String batteryFrequency = "0 Minute";
            if (battery) {
                batteryFrequency = PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_BATTERYFREQUENCY, context);
            }
            String chargingFrequency = "0 Minute";
            if (charging) {
                chargingFrequency = PreferenceUtils.optionGetFrequency(Constants.KEY_SETTING_CHARGEFREQUENCY, context);
            }
            if (dndFrequency.equals(Constants.FREQUENCY_NEVER)) {
                otherType = Constants.KEY_SETTING_DNDFREQUENCY;
            } else if (batteryFrequency.equals(Constants.FREQUENCY_NEVER)) {
                otherType = Constants.KEY_SETTING_BATTERYFREQUENCY;
            } else if (chargingFrequency.equals(Constants.FREQUENCY_NEVER)) {
                otherType = Constants.KEY_SETTING_CHARGEFREQUENCY;
            } else {
                long[] secondsArray = {getMilliseconds(chargingFrequency), getMilliseconds(batteryFrequency), getMilliseconds(dndFrequency)};
                long seconds = 0;
                for (int i = 0; i < secondsArray.length; i++) {
                    if (secondsArray[i] >= seconds) {
                        switch (i) {
                            case 0:
                                otherType = Constants.KEY_SETTING_CHARGEFREQUENCY;
                                break;
                            case 1:
                                otherType = Constants.KEY_SETTING_BATTERYFREQUENCY;
                                break;
                            case 2:
                                otherType = Constants.KEY_SETTING_DNDFREQUENCY;
                                break;
                        }
                        seconds = secondsArray[i];
                    }
                }
            }
        }

        if (otherType.isEmpty()) {
            return networkType;
        } else if (otherType.equals(Constants.KEY_SETTING_CHARGEFREQUENCY) && networkType.equals(Constants.KEY_SETTING_WIFIFREQUENCY)) {
            return otherType;
        } else {
            String networkFrequency = PreferenceUtils.optionGetFrequency(networkType, context);
            String otherFrequency = PreferenceUtils.optionGetFrequency(otherType, context);
            if (otherFrequency.equals(Constants.FREQUENCY_NEVER)) {
                return otherType;
            } else if (networkFrequency.equals(Constants.FREQUENCY_NEVER)) {
                return networkType;
            } else if (getMilliseconds(networkFrequency) >= getMilliseconds(otherFrequency)) {
                return networkType;
            } else {
                return otherType;
            }
        }
    }

    public static long getMilliseconds(String frequency) {
        int number = Integer.parseInt(frequency.substring(0, frequency.indexOf(" ")));
        long seconds = 15 * 60; // 15 minutes as default
        if (frequency.contains("Minute")) {
            seconds = number * 60L;
        } else if (frequency.contains("Hour")) {
            seconds = number * 3600L;
        } else if (frequency.contains("Day")) {
            seconds = number * 86400L;
        } else if (frequency.contains("Week")) {
            seconds = number * 604800L;
        }
        return seconds * 1000;
    }

    public static String extractFrequencyTimeTypeFromArray(Context context, int index, int value) {
        String[] frequencyTimeTypeArray = context.getResources().getStringArray(R.array.frequency_time_types);
        String frequencyTimeType = frequencyTimeTypeArray[index];
        if (value == 1) {
            frequencyTimeType = frequencyTimeType.substring(0, frequencyTimeType.length() - 1);
        }
        return frequencyTimeType;
    }

    public static boolean doesWorkExist(Context context) {
        WorkManager instance = WorkManager.getInstance(context);
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosForUniqueWork("jobTag");
        try {
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                    Log.i(TAG, "Work Exists");
                    return true;
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, e.toString());
        }
        Log.i(TAG, "Work does not Exist");
        return false;
    }

    public static int getFacebookAlertsNum(Context context) {
        int num = 0;
        ArrayList<Alert> alertList = PreferenceUtils.getAlertList(context);
        for (Alert alert : alertList) {
            if (alert.hasFacebook()) {
                num++;
            }
        }
        return num;
    }

    public static String getLatLong(String postalCode, Context context) {
        String latLong = "";
        try {
            List<Address> addresses = new Geocoder(context).getFromLocationName(postalCode, 1);
            if (addresses != null && !addresses.isEmpty()) {
                latLong = addresses.get(0).getLatitude() + "%2C" + addresses.get(0).getLongitude();
            } else {
                Log.e(TAG, "Unable to find latitude and longitude for postal code");
                latLong = "00.00%2C-00.00";
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return latLong;
    }

}

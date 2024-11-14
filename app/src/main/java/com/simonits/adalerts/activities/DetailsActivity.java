package com.simonits.adalerts.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.simonits.adalerts.R;
import com.simonits.adalerts.adapters.ViewPagerAdapter;
import com.simonits.adalerts.custom.ViewPagerFixed;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";

    private String tabSite;
    private final ArrayList<String> images = new ArrayList<>();
    private String name, ePosted, cLoc, cPosted;

    private ProgressBar progressBar;
    private RelativeLayout darkScreen;
    private FloatingActionButton fabLink, fabFav;
    private WebView webView;
    private int fPass;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.details_progressBar);
        darkScreen = findViewById(R.id.details_darkScreen);

        name = getIntent().getStringExtra("name");
        tabSite = getIntent().getStringExtra("tabSite");
        Listing listingObj = (Listing) getIntent().getSerializableExtra("listing");
        String url = Objects.requireNonNull(listingObj).getUrl();
        ePosted = listingObj.getPosted();
        cLoc = listingObj.getLocation();
        cPosted = listingObj.getPosted();

        setTitle(name);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        fabLink = findViewById(R.id.details_fabLink);
        fabLink.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(url))));

        fabFav = findViewById(R.id.details_fabFav);

        ArrayList<Listing> favourites = PreferenceUtils.getFavsList(this);
        for (Listing listing : favourites) {
            if (listing.getID().equals(listingObj.getID())) {
                fabFav.setImageResource(R.drawable.starfullicon);
            }
        }

        fabFav.setOnClickListener(view -> {
            ArrayList<Listing> favourites1 = PreferenceUtils.getFavsList(DetailsActivity.this);
            int i = 0;
            for (Listing listing : favourites1) {
                if (listing.getID().equals(listingObj.getID())) {
                    favourites1.remove(i);
                    PreferenceUtils.saveFavsList(favourites1, DetailsActivity.this);
                    Snackbar.make(findViewById(android.R.id.content), "Removed from favourites", Snackbar.LENGTH_SHORT).show();
                    fabFav.setImageResource(R.drawable.staremptyicon);
                    fabFav.hide();
                    fabFav.show();
                    return;
                }
                i++;
            }
            favourites1.add(0, listingObj);
            PreferenceUtils.saveFavsList(favourites1, DetailsActivity.this);
            Snackbar.make(findViewById(android.R.id.content), "Added to favourites", Snackbar.LENGTH_SHORT).show();
            fabFav.setImageResource(R.drawable.starfullicon);
            fabFav.hide();
            fabFav.show();
        });

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout1.getTotalScrollRange()) {
                // Collapsed
                collapsingToolbarLayout.setTitle(name);
            } else if (verticalOffset == 0) {
                // Expanded
                collapsingToolbarLayout.setTitle(" ");
            } else {
                // Somewhere in between
                collapsingToolbarLayout.setTitle(" ");
            }
        });

        Log.i(TAG, "~~DETAILS STUFF START~~");
        Log.i(TAG, url);
        FirebaseCrashlytics.getInstance().log(url);
        if (tabSite.equals(Constants.TABSITE_FACEBOOK)) { // facebook tab
            if (PreferenceUtils.getFacebookLogin(this)) {
                webView = findViewById(R.id.details_webView);
                webView.setVisibility(View.INVISIBLE);
                webView.getSettings().setUserAgentString(Utility.getFacebookUA());
                webView.getSettings().setJavaScriptEnabled(true);
                webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        webView.loadUrl("javascript:window.HtmlViewer.showHTML" +
                                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                    }
                });
                fPass = 0;

                webView.loadUrl(url);
            } else {
                Log.i(TAG, getString(R.string.facebook_logged_out));
                TextView facebookLoggedOut = findViewById(R.id.details_listingGoneText);
                facebookLoggedOut.setText(getString(R.string.facebook_logged_out));
                facebookLoggedOut.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                darkScreen.setVisibility(View.GONE);
                Log.i(TAG, "DETAILS STUFF END");
            }
        } else {
            Executors.newSingleThreadExecutor().execute(() -> {
                HashMap<String, String> map = Utility.getScrapeMap(tabSite, "details");
                ArrayList<String> details;
                switch (tabSite) {
                    case Constants.TABSITE_KIJIJI:
                        details = getKijijiDetails(url, map);
                        break;
                    case Constants.TABSITE_EBAY:
                        details = getEbayDetails(url, map);
                        break;
                    case Constants.TABSITE_CRAIGSLIST:
                        details = getCraigslistDetails(url, map);
                        break;
                    default:
                        details = null;
                        break;
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (details == null) {
                        Log.i(TAG, "DETAILS STUFF CANCELLED");
                        findViewById(R.id.details_dataFetchErrorText).setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        darkScreen.setVisibility(View.GONE);
                    } else {
                        withResults(details);
                    }
                });
            });
        }

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.scale_in, R.anim.slide_out_left);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void showHTML(String html) {
            fPass++;
            if (fPass == 2) {
                runOnUiThread(() -> {
                    webView.setVisibility(View.GONE);
                    withResults(getFacebookDetails(html));
                });
                fPass = 0;
            }
        }
    }

    private ArrayList<String> getFacebookDetails(String htmlStr) {
        ArrayList<String> items = new ArrayList<>();
        HashMap<String, String> map = Utility.getScrapeMap(Constants.TABSITE_FACEBOOK, "details");

        if (htmlStr.contains(Objects.requireNonNull(map.get("cantContain1"))) || !htmlStr.contains(Objects.requireNonNull(map.get("detailsStart")))) {
            return new ArrayList<String>() {{
                add("not available");
            }};
        }

        int startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("detailsStart"))) + Objects.requireNonNull(map.get("detailsStart")).length();

        String detailsSub;
        try {
            detailsSub = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("detailsEnd")), startIndex));
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'detailsSub' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            detailsSub = htmlStr.substring(startIndex);
        }

        if (detailsSub.contains(Objects.requireNonNull(map.get("cantContain2"))) || detailsSub.contains(Objects.requireNonNull(map.get("cantContain3")))) {
            return new ArrayList<String>() {{
                add("not available");
            }};
        }

        String price;
        try {
            startIndex = detailsSub.indexOf(Objects.requireNonNull(map.get("priceTag"))) + Objects.requireNonNull(map.get("priceTag")).length();
            price = detailsSub.substring(startIndex, detailsSub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex)).trim();
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
        items.add(price); // items(0)

        String location;
        try {
            startIndex = detailsSub.indexOf(Objects.requireNonNull(map.get("locTag"))) + Objects.requireNonNull(map.get("locTag")).length();
            location = detailsSub.substring(startIndex, detailsSub.indexOf(Objects.requireNonNull(map.get("loc_end")), startIndex)).trim();
            location = Utility.htmlToPlain(location, Constants.TABSITE_FACEBOOK);
            Log.i(TAG, "Location: " + location);
            if (location.equals(", ") || !Character.isLetter(location.charAt(0)) || !Character.isLetter(location.charAt(location.length() - 1))) {
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
        items.add(location); // items(1)

        String posted;
        try {
            startIndex = detailsSub.indexOf(Objects.requireNonNull(map.get("postedTag"))) + Objects.requireNonNull(map.get("postedTag")).length();
            posted = detailsSub.substring(startIndex, detailsSub.indexOf(Objects.requireNonNull(map.get("posted_end")), startIndex)).trim();
            Log.i(TAG, "Posted: " + posted);
            if (posted.isEmpty() || !Character.isDigit(posted.charAt(0)) || !Character.isDigit(posted.charAt(posted.length() - 1))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'posted' is empty OR doesn't start with a digit OR doesn't end with a digit");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                posted = "-POSTED ERROR-";
            } else {
                Date date = new Date(Long.parseLong(posted) * 1000);
                posted = map.get("postedStr") + new SimpleDateFormat("h:mm a dd/M/yyyy", Locale.getDefault()).format(date);
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'posted' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            posted = "-POSTED ERROR-";
        } catch (NumberFormatException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'posted' NumberFormatException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            posted = "-POSTED ERROR-";
        }
        items.add(posted); // items(2)

        String condition;
        try {
            if (detailsSub.contains(Objects.requireNonNull(map.get("conditionTag")))) {
                startIndex = detailsSub.indexOf(Objects.requireNonNull(map.get("conditionTag_start"))) + Objects.requireNonNull(map.get("conditionTag_start")).length();
                condition = detailsSub.substring(startIndex, detailsSub.indexOf(Objects.requireNonNull(map.get("condition_end")), startIndex)).trim() + map.get("conditionStr");
            } else {
                condition = "n/a";
            }
            Log.i(TAG, "Condition: " + condition);
            if (condition.equals(map.get("conditionStr")) || (!condition.equals("n/a") && (!Character.isLetter(condition.charAt(0)) || !Character.isLetter(condition.charAt(condition.length() - Objects.requireNonNull(map.get("conditionStr")).length() - 1))))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'condition' is empty OR doesn't start with a letter OR doesn't end with a letter");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                condition = "-CONDITION ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'condition' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            condition = "-CONDITION ERROR-";
        }
        items.add(condition); // item(3)

        String desc;
        try {
            startIndex = detailsSub.indexOf(Objects.requireNonNull(map.get("descTag"))) + Objects.requireNonNull(map.get("descTag")).length();
            desc = detailsSub.substring(startIndex, detailsSub.indexOf(Objects.requireNonNull(map.get("desc_end")), startIndex)).trim();
            desc = Utility.htmlToPlain(desc, Constants.TABSITE_FACEBOOK);
            Log.i(TAG, "Description: " + desc);
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'description' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            desc = "-DESCRIPTION ERROR-";
        }
        items.add(desc); // items(4)

        try {
            startIndex = detailsSub.indexOf(Objects.requireNonNull(map.get("imgSub_start"))) + Objects.requireNonNull(map.get("imgSub_start")).length();
            String imgSub = detailsSub.substring(startIndex, detailsSub.indexOf(Objects.requireNonNull(map.get("imgSub_end")), startIndex));
            int fromIndex = 0;
            while ((fromIndex = imgSub.indexOf(Objects.requireNonNull(map.get("imgTag")), fromIndex)) != -1) {
                String image = imgSub.substring(fromIndex + Objects.requireNonNull(map.get("imgTag")).length(), imgSub.indexOf(Objects.requireNonNull(map.get("img_end")), fromIndex + Objects.requireNonNull(map.get("imgTag")).length()));
                image = Utility.htmlToPlain(image, Constants.TABSITE_FACEBOOK);
                if (images.isEmpty() || !images.contains(image)) {
                    Log.i(TAG, "Image: " + image);
                    if (image.isEmpty() || image.contains(" ") || !image.startsWith(Objects.requireNonNull(map.get("img_check")))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space OR doesn't start with " + map.get("img_check"));
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        images.clear();
                        images.add("error");
                        break;
                    }
                    images.add(image);
                }
                fromIndex++;
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            images.clear();
            images.add("error");
        }

        return items;
    }

    private ArrayList<String> getKijijiDetails(String url, HashMap<String, String> map) {
        ArrayList<String> items = new ArrayList<>();
        String HtmlStr = Utility.getHtml(Utility.getClient(tabSite), url);
        if (HtmlStr == null) {
            return null;
        }

        if (HtmlStr.contains(Objects.requireNonNull(map.get("notAvailableHtml"))) || !HtmlStr.contains(Objects.requireNonNull(map.get("sub1_start")))) {
            return new ArrayList<String>() {{
                add("not available");
            }};
        }

        int startIndex;
        String price;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("sub1_start")));
            String sub = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("sub1_end")), startIndex));
            if (sub.contains(Objects.requireNonNull(map.get("contains1"))) && HtmlStr.substring(HtmlStr.indexOf(Objects.requireNonNull(map.get("sub2_start"))) - Integer.parseInt(Objects.requireNonNull(map.get("sub2_offset"))), HtmlStr.indexOf(Objects.requireNonNull(map.get("sub2_end")))).contains(Objects.requireNonNull(map.get("contains2")))) {
                price = map.get("str1");
            } else if (sub.contains(Objects.requireNonNull(map.get("contains3")))) {
                price = map.get("str2");
            } else if (sub.contains(Objects.requireNonNull(map.get("contains5")))) {
                price = map.get("str7");
            } else if (!sub.contains(Objects.requireNonNull(map.get("priceTag")))) {
                price = "Price n/a";
            } else {
                startIndex = sub.indexOf(Objects.requireNonNull(map.get("priceTag"))) + Objects.requireNonNull(map.get("priceTag")).length();
                price = map.get("currencyStr") + sub.substring(startIndex, sub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex)).trim();
            }
            Log.i(TAG, "Price: " + price);
            if (Objects.requireNonNull(price).equals(map.get("currencyStr")) || (!price.equals(map.get("str1")) && !price.equals(map.get("str2")) && !price.equals(map.get("str7")) && !price.equals("Price n/a") && ((!Character.isDigit(price.charAt(Objects.requireNonNull(map.get("currencyStr")).length()))) || !Character.isDigit(price.charAt(price.length() - 1))))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'price' is empty OR doesn't equal " + map.get("str1") + " AND doesn't equal " + map.get("str2") + " AND doesn't equal " + map.get("str7") + " AND doesn't start with a digit OR doesn't end in a digit");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                price = "-PRICE ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'price' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            price = "-PRICE ERROR-";
        }
        items.add(price); // items(0)

        String location;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("locTag_end")), HtmlStr.indexOf(Objects.requireNonNull(map.get("locTag_start")))) + Objects.requireNonNull(map.get("locTag_end")).length();
            location = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("loc_end")), startIndex)).trim();
            location = Utility.htmlToPlain(location, "");
            Log.i(TAG, "Location: " + location);
            if (location.isEmpty() || (!Character.isLetter(location.charAt(0)) && !Character.isDigit(location.charAt(0))) || (!Character.isLetter(location.charAt(location.length() - 1)) && !Character.isDigit(location.charAt(location.length() - 1)))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'location' is empty OR doesn't start with a letter/digit OR doesn't end with a letter/digit");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                location = "-LOCATION ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'location' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            location = "-LOCATION ERROR-";
        }
        items.add(location); // items(1)

        String posted;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("postedTag_start")));
            if (startIndex == -1) {
                posted = "n/a";
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("sub3_start")));
                if (startIndex != -1) {
                    String postedSub = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("sub3_end")), startIndex));
                    if (postedSub.contains(Objects.requireNonNull(map.get("contains4")))) {
                        posted = map.get("str3");
                    } else if (postedSub.contains(Objects.requireNonNull(map.get("contains6")))) {
                        posted = map.get("str8");
                    }
                }
            } else {
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("postedTag_end")), startIndex) + Objects.requireNonNull(map.get("postedTag_end")).length();
                posted = map.get("str4") + HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("posted_end")), startIndex)).trim();
            }
            Log.i(TAG, "Posted: " + posted);
            if (Objects.requireNonNull(posted).equals(map.get("str4")) || (!posted.equals("n/a") && !posted.equals(map.get("str3")) && !posted.equals(map.get("str8")) && ((!Character.isLetter(posted.charAt(Objects.requireNonNull(map.get("str4")).length())) && !Character.isDigit(posted.charAt(Objects.requireNonNull(map.get("str4")).length()))) || !Character.isLetter(posted.charAt(posted.length() - 1))))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'posted' is empty OR doesn't equal n/a AND doesn't equal " + map.get("str3") + " AND doesn't equal " + map.get("str8") + " AND doesn't start with a letter/digit OR doesn't end with a letter");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                posted = "-POSTED ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'posted' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            posted = "-POSTED ERROR-";
        }
        items.add(posted); // items(2)

        String visits;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("visitsTag_start")));
            if (startIndex == -1) {
                visits = "n/a";
            } else {
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("visitsTag_end")), startIndex) + Objects.requireNonNull(map.get("visitsTag_end")).length();
                if ((HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("visits_end")), startIndex)).trim()).isEmpty()) {
                    startIndex = startIndex + Objects.requireNonNull(map.get("visitsTag_end")).length();
                }
                visits = (HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("visits_end")), startIndex)).trim()).replace(Objects.requireNonNull(map.get("unwantedStr1")), "").replace(Objects.requireNonNull(map.get("target3")), Objects.requireNonNull(map.get("replacement3"))) + map.get("str5");
            }
            Log.i(TAG, "Visits: " + visits);
            if (visits.equals(map.get("str5")) || (!visits.equals("n/a") && (!Character.isDigit(visits.charAt(0)) || !Character.isDigit(visits.charAt(visits.length() - Objects.requireNonNull(map.get("str5")).length() - 1))))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'visits' is empty OR doesn't equal n/a AND doesn't start with a digit OR doesn't end with a digit");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                visits = "-VISITS ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'visits' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            visits = "-VISITS ERROR-";
        }
        items.add(visits); // items(3)

        String descPlain;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("descTag_end")), HtmlStr.indexOf(Objects.requireNonNull(map.get("descTag_start")))) + Objects.requireNonNull(map.get("descTag_end")).length();
            String descHtml = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("desc_end")), startIndex)).trim();
            if (descHtml.contains(Objects.requireNonNull(map.get("str6")))) {
                descHtml = descHtml.substring(descHtml.indexOf(Objects.requireNonNull(map.get("str6"))) + Objects.requireNonNull(map.get("str6")).length());
            }
            Log.i(TAG, "Description HTML: " + descHtml);
            descPlain = Utility.htmlToPlain(descHtml, "desc");
            Log.i(TAG, "Description Plain: " + descPlain);
            if (descPlain.isEmpty()) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'description' is empty");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                descPlain = "-DESCRIPTION ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'description' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            descPlain = "-DESCRIPTION ERROR-";
        }
        items.add(descPlain); // items(4)

        try {
            String strFind = Objects.requireNonNull(map.get("imgTag"));
            int num = Objects.requireNonNull(map.get("imgTag")).length();
            if (!HtmlStr.contains(strFind)) {
                strFind = Objects.requireNonNull(map.get("altImgTag"));
                num = Objects.requireNonNull(map.get("altImgTag")).length();
            }
            int fromIndex = 0;
            while ((fromIndex = HtmlStr.indexOf(strFind, fromIndex)) != -1) {
                fromIndex += num;
                String image = HtmlStr.substring(fromIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("imgTag_end")), fromIndex)).trim();
                image = image.replace(Objects.requireNonNull(map.get("target1")), Objects.requireNonNull(map.get("replacement1"))).replace(Objects.requireNonNull(map.get("target2")), Objects.requireNonNull(map.get("replacement2"))).replace(Objects.requireNonNull(map.get("target4")), Objects.requireNonNull(map.get("replacement4")));
                Log.i(TAG, "Image: " + image);
                if (image.isEmpty() || image.contains(" ") || !image.startsWith(Objects.requireNonNull(map.get("img_check")))) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space OR doesn't start with " + map.get("img_check"));
                    FirebaseCrashlytics.getInstance().recordException(new Exception());
                    images.clear();
                    images.add("error");
                    break;
                }
                images.add(image);
                fromIndex++;
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            images.clear();
            images.add("error");
        }

        return items;
    }

    private ArrayList<String> getEbayDetails(String url, HashMap<String, String> map) {
        ArrayList<String> items = new ArrayList<>();
        OkHttpClient client = Utility.getClient(tabSite);
        String HtmlStr = Utility.getHtml(client, url);
        if (HtmlStr == null) {
            return null;
        }

        if (HtmlStr.contains(Objects.requireNonNull(map.get("cantContain"))) || !HtmlStr.contains(Objects.requireNonNull(map.get("mustContain")))) {
            return new ArrayList<String>() {{
                add("not available");
            }};
        }

        int startIndex;
        String price;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("sub1_start"))) + Integer.parseInt(Objects.requireNonNull(map.get("sub1_offset")));
            String sub = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("sub1_end")), startIndex));
            startIndex = sub.indexOf(Objects.requireNonNull(map.get("priceTag_end")), sub.indexOf(Objects.requireNonNull(map.get("priceTag_start")))) + Objects.requireNonNull(map.get("priceTag_end")).length();
            price = sub.substring(startIndex, sub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex)).trim();
            if (!price.contains(Objects.requireNonNull(map.get("currencyStr")))) {
                startIndex = sub.indexOf(Objects.requireNonNull(map.get("currencyStr")));
                price = sub.substring(startIndex, sub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex)).trim();
            }
            sub = Utility.htmlToPlain(sub, "desc");
            if (sub.contains(Objects.requireNonNull(map.get("priceContains1")))) {
                if (sub.contains(Objects.requireNonNull(map.get("priceContains2")))) {
                    price = price + map.get("priceStr1");
                } else if (sub.contains(Objects.requireNonNull(map.get("priceContains4")))) {
                    price = price + map.get("priceStr5");
                } else {
                    price = price + map.get("priceStr2");
                }
            } else if (sub.contains(Objects.requireNonNull(map.get("priceContains3")))) {
                price = price + map.get("priceStr3");
            } else if (sub.contains(Objects.requireNonNull(map.get("priceContains5")))) {
                price = price + map.get("priceStr6");
            } else {
                price = price + map.get("priceStr4");
            }
            Log.i(TAG, "Price: " + price);
            if (price.equals(map.get("priceStr4")) || !price.contains(Objects.requireNonNull(map.get("currencyStr"))) || (!price.contains(Objects.requireNonNull(map.get("priceStr1"))) && !price.contains(Objects.requireNonNull(map.get("priceStr5"))) && !price.contains(Objects.requireNonNull(map.get("priceStr2"))) && !price.contains(Objects.requireNonNull(map.get("priceStr3"))) && !price.contains(Objects.requireNonNull(map.get("priceStr6"))) && !price.contains(Objects.requireNonNull(map.get("priceStr4")))) || !Character.isDigit(price.charAt(Objects.requireNonNull(map.get("currencyStr")).length()))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'price' is empty OR doesn't contain " + map.get("currencyStr") + " OR doesn't contain " + map.get("priceStr1") + " AND doesn't contain " + map.get("priceStr5") + " AND doesn't contain " + map.get("priceStr2") + " AND doesn't contain " + map.get("priceStr3") + " AND doesn't contain " + map.get("priceStr6") + " AND doesn't contain " + map.get("priceStr4") + " OR doesn't start with a digit");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                price = "-PRICE ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'price' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            price = "-PRICE ERROR-";
        }
        items.add(price); // items(0)

        String location;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("locTag_end")), HtmlStr.indexOf(Objects.requireNonNull(map.get("locTag_start")))) + Objects.requireNonNull(map.get("locTag_end")).length();
            location = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("loc_end")), startIndex)).trim();
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
        items.add(location); // items(1)

        ePosted = map.get("postedStr") + ePosted;
        Log.i(TAG, "Posted: " + ePosted);
        items.add(ePosted); // item(2)

        String condition;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("conditionTag_end")), HtmlStr.indexOf(Objects.requireNonNull(map.get("conditionTag_start")))) + Objects.requireNonNull(map.get("conditionTag_end")).length();
            condition = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("condition_end")), startIndex)).trim() + map.get("conditionStr");
            Log.i(TAG, "Condition: " + condition);
            if (condition.equals(map.get("conditionStr")) || !Character.isLetter(condition.charAt(0)) || !Character.isLetter(condition.charAt(condition.length() - Objects.requireNonNull(map.get("conditionStr")).length() - 1))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'condition' is empty OR doesn't start with a letter OR doesn't end with a letter");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                condition = "-CONDITION ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'condition' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            condition = "-CONDITION ERROR-";
        }
        items.add(condition); // item(3)

        try {
            String image;
            String strFind = Objects.requireNonNull(map.get("imgContains1"));
            if (HtmlStr.contains(strFind)) { // true when there's more than a single image in the listing
                int fromIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("sub2_start")));
                while ((fromIndex = HtmlStr.indexOf(strFind, fromIndex)) != -1) {
                    fromIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("imgTag")), fromIndex) + Objects.requireNonNull(map.get("imgTag")).length();
                    image = HtmlStr.substring(fromIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("img_end")), fromIndex));
                    image = image.replace(Objects.requireNonNull(map.get("target1")), Objects.requireNonNull(map.get("replacement1"))).replace(Objects.requireNonNull(map.get("target2")), Objects.requireNonNull(map.get("replacement2")));
                    Log.i(TAG, "Image: " + image);
                    images.add(image);
                }
            } else {
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("imgAltTag_end")), HtmlStr.indexOf(Objects.requireNonNull(map.get("imgAltTag_start")))) + Objects.requireNonNull(map.get("imgAltTag_end")).length();
                image = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("imgAlt_end")), startIndex));
                if (image.contains(Objects.requireNonNull(map.get("imgContains2")))) {
                    image = image.substring(0, image.indexOf(Objects.requireNonNull(map.get("imgContains2")))).trim() + map.get("imgStr");
                    images.add(image);
                } else {
                    Log.i(TAG, "Image is not normal, so going to act as if no image.");
                }
            }
            if (!images.isEmpty()) {
                image = images.get(0);
                if (image.isEmpty() || image.contains(" ") || !image.startsWith(Objects.requireNonNull(map.get("img_check")))) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space OR doesn't start with " + map.get("img_check"));
                    FirebaseCrashlytics.getInstance().recordException(new Exception());
                    images.clear();
                    images.add("error");
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            images.clear();
            images.add("error");
        }

        String descPlain;
        try {
            String itemNum = url.substring(url.indexOf(Objects.requireNonNull(map.get("itemNum_start"))) + Objects.requireNonNull(map.get("itemNum_start")).length(), url.indexOf(Objects.requireNonNull(map.get("itemNum_end"))));
            String desLink = Objects.requireNonNull(map.get("desLink1")) + itemNum + Objects.requireNonNull(map.get("desLink2"));
            HtmlStr = "";

            try { // this is all new because OkHttpClient wasn't working with this URL for some reason
                HttpURLConnection connection = (HttpURLConnection) new URL(desLink).openConnection();
                connection.setRequestMethod("GET");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    HtmlStr = response.toString();
                }
            } catch (IOException e) {
                Log.i(TAG, "Error!!!");
                Log.e(TAG, e.toString());
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("descTag"))) + Objects.requireNonNull(map.get("descTag")).length();
            String descHtml = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("desc_end")), startIndex)).trim();
            Log.i(TAG, "Description HTML: " + descHtml);
            descPlain = Utility.htmlToPlain(descHtml, "desc");
            Log.i(TAG, "Description Plain: " + descPlain);
            if (descPlain.isEmpty()) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'description' is empty");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                descPlain = "-DESCRIPTION ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'description' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            descPlain = "-DESCRIPTION ERROR-";
        }
        items.add(descPlain); // items(4)

        return items;
    }

    private ArrayList<String> getCraigslistDetails(String url, HashMap<String, String> map) {
        ArrayList<String> items = new ArrayList<>();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(chain -> {
                    final Request original = chain.request();
                    final Request authorized = original.newBuilder()
                            // don't think i need cookies to get consistent data from Craigslist. Craigslist is just that easy boi
                            .build();
                    return chain.proceed(authorized);
                })
                .build();
        String HtmlStr = Utility.getHtml(client, url);
        if (HtmlStr == null) {
            return null;
        }

        if (HtmlStr.contains(Objects.requireNonNull(map.get("cantContain"))) || !HtmlStr.contains(Objects.requireNonNull(map.get("mustContain")))) {
            return new ArrayList<String>() {{
                add("not available");
            }};
        }

        int startIndex;
        String sub;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("sub1_start")));
            sub = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("sub1_end")), startIndex));
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'sub1' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }

        String price;
        try {
            if (HtmlStr.contains(Objects.requireNonNull(map.get("priceTag")))) {
                startIndex = sub.indexOf(Objects.requireNonNull(map.get("priceTag"))) + Objects.requireNonNull(map.get("priceTag")).length();
                price = sub.substring(startIndex, sub.indexOf(Objects.requireNonNull(map.get("price_end")), startIndex));
            } else {
                Log.i(TAG, "Price: No price tag found.");
                price = map.get("noPriceTagStr");
            }
            Log.i(TAG, "Price: " + price);
            if (Objects.requireNonNull(price).isEmpty() || !price.startsWith(Objects.requireNonNull(map.get("price_check"))) || !Character.isDigit(price.charAt(price.length() - 1))) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'price' is empty OR doesn't start with " + map.get("price_check") + " OR price doesn't end with a digit");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                price = "-PRICE ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'price' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            price = "-PRICE ERROR-";
        }
        items.add(price); // items(0)

        String location;
        try {
            if (HtmlStr.contains(Objects.requireNonNull(map.get("locTag")))) {
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("locTag"))) + Objects.requireNonNull(map.get("locTag")).length();
                location = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("loc_end")), startIndex));
            } else if (sub.contains(Objects.requireNonNull(map.get("locAltTag")))) {
                startIndex = sub.indexOf(Objects.requireNonNull(map.get("locAltTag"))) + Objects.requireNonNull(map.get("locAltTag")).length();
                location = sub.substring(startIndex, sub.indexOf(Objects.requireNonNull(map.get("locAlt_end")))).replace(Objects.requireNonNull(map.get("unwanted1")), "").replace(Objects.requireNonNull(map.get("unwanted2")), "").trim();
            } else {
                Log.i(TAG, "Location: None, so going with general location.");
                location = cLoc;
            }
            location = Utility.htmlToPlain(location, "name"); // this is because the poster could enter their own text for location
            Log.i(TAG, "Location: " + location);
            if (location.isEmpty()) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'location' is empty");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                location = "-LOCATION ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'location' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            location = "-LOCATION ERROR-";
        }
        items.add(location); // items(1)

        Log.i(TAG, "Posted: " + cPosted);
        items.add(cPosted); // item(2)

        if (HtmlStr.contains(Objects.requireNonNull(map.get("sub2_start")))) {
            try {
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("sub2_start")));
                sub = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("sub2_end")), startIndex));
            } catch (StringIndexOutOfBoundsException e) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'sub2' StringIndexOutOfBoundsException caught");
                Log.e(TAG, e.toString());
                FirebaseCrashlytics.getInstance().recordException(e);
                return null;
            }
            if (sub.contains(Objects.requireNonNull(map.get("conditionTag")))) {
                String condition;
                try {
                    startIndex = sub.indexOf(Objects.requireNonNull(map.get("conditionTag"))) + Objects.requireNonNull(map.get("conditionTag")).length();
                    condition = sub.substring(startIndex, sub.indexOf(Objects.requireNonNull(map.get("condition_end")), startIndex)) + map.get("conditionStr");
                    Log.i(TAG, "Condition: " + condition);
                    if (condition.equals(map.get("conditionStr")) || !Character.isLetter(condition.charAt(0)) || !Character.isLetter(condition.charAt(condition.length() - Objects.requireNonNull(map.get("conditionStr")).length() - 1))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'condition' is empty OR doesn't start with a letter OR doesn't end with a letter");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        condition = "-CONDITION ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'condition' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    condition = "-CONDITION ERROR-";
                }
                items.add(condition); // item(3)
            } else if (sub.contains(Objects.requireNonNull(map.get("brbaTag")))) {
                try {
                    startIndex = sub.indexOf(Objects.requireNonNull(map.get("brbaTag"))) + Objects.requireNonNull(map.get("brbaTag")).length();
                    sub = sub.substring(startIndex, sub.indexOf(Objects.requireNonNull(map.get("brba_end")), startIndex)).trim();
                    sub = Utility.htmlToPlain(sub, "desc");
                    Log.i(TAG, "House BR / Ba: " + sub);
                    if (sub.isEmpty() || !Character.isDigit(sub.charAt(0)) || !Character.isLetter(sub.charAt(sub.length() - 1))) {
                        Log.i(TAG, "HTML SCRAPE ERROR: 'house BR/Ba' is empty OR doesn't start with a digit OR doesn't end with a letter");
                        FirebaseCrashlytics.getInstance().recordException(new Exception());
                        sub = "-BR/BA ERROR-";
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'house BR/Ba' StringIndexOutOfBoundsException caught");
                    Log.e(TAG, e.toString());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    sub = "-BR/BA ERROR-";
                }
                items.add(sub); // item(3)
            } else {
                items.add("n/a"); // items(3)
            }
        } else {
            items.add("n/a"); // items(3)
        }

        try {
            String image;
            if (HtmlStr.contains(Objects.requireNonNull(map.get("sub3_start")))) { // true when there's more than a single image in the listing
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("sub3_start")));
                sub = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("sub3_end")), startIndex));
                String strFind = Objects.requireNonNull(map.get("imgTag"));
                int fromIndex = 0;
                while ((fromIndex = sub.indexOf(strFind, fromIndex)) != -1) {
                    fromIndex += Objects.requireNonNull(map.get("imgTag")).length();
                    image = sub.substring(fromIndex, sub.indexOf(Objects.requireNonNull(map.get("img_end")), fromIndex));
                    image = image.replace(Objects.requireNonNull(map.get("target1")), Objects.requireNonNull(map.get("replacement1"))).replace(Objects.requireNonNull(map.get("target2")), Objects.requireNonNull(map.get("replacement2")));
                    Log.i(TAG, "Image: " + image);
                    images.add(image);
                    fromIndex++;
                }
            } else if (HtmlStr.contains(Objects.requireNonNull(map.get("imgAltTag")))) {
                startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("imgAltTag"))) + Objects.requireNonNull(map.get("imgAltTag")).length();
                image = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("imgAlt_end")), startIndex));
                image = image.replace(Objects.requireNonNull(map.get("target1")), Objects.requireNonNull(map.get("replacement1"))).replace(Objects.requireNonNull(map.get("target2")), Objects.requireNonNull(map.get("replacement2")));
                Log.i(TAG, "Image: " + image);
                images.add(image);
            }
            if (!images.isEmpty()) {
                image = images.get(0);
                if (image.isEmpty() || image.contains(" ") || !image.startsWith(Objects.requireNonNull(map.get("img_check")))) {
                    Log.i(TAG, "HTML SCRAPE ERROR: 'image' is empty OR contains a space OR doesn't start with " + map.get("img_check"));
                    FirebaseCrashlytics.getInstance().recordException(new Exception());
                    images.clear();
                    images.add("error");
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'image' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            images.clear();
            images.add("error");
        }

        String descPlain;
        try {
            startIndex = HtmlStr.indexOf(Objects.requireNonNull(map.get("descTag_end")), HtmlStr.indexOf(Objects.requireNonNull(map.get("descTag_start"))) + Objects.requireNonNull(map.get("descTag_start")).length());
            String descHtml = HtmlStr.substring(startIndex, HtmlStr.indexOf(Objects.requireNonNull(map.get("desc_end")), startIndex)).trim();
            Log.i(TAG, "Description HTML: " + descHtml);
            descPlain = Utility.htmlToPlain(descHtml, "desc");
            Log.i(TAG, "Description Plain: " + descPlain);
            if (descPlain.isEmpty()) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'description' is empty");
                FirebaseCrashlytics.getInstance().recordException(new Exception());
                descPlain = "-DESCRIPTION ERROR-";
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.i(TAG, "HTML SCRAPE ERROR: 'description' StringIndexOutOfBoundsException caught");
            Log.e(TAG, e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
            descPlain = "-DESCRIPTION ERROR-";
        }
        items.add(descPlain); // items(4)

        return items;
    }

    private void withResults(ArrayList<String> param) {
        if (param.get(0).equals("not available")) {
            Log.i(TAG, getString(R.string.listing_gone));
            TextView listingGone = findViewById(R.id.details_listingGoneText);
            listingGone.setVisibility(View.VISIBLE);
        } else {
            fabLink.show();
            fabFav.show();

            ((TextView) findViewById(R.id.details_name)).setText(name);
            ((TextView) findViewById(R.id.details_price)).setText(param.get(0));
            ((TextView) findViewById(R.id.details_location)).setText(param.get(1));
            if (param.get(2).equals("n/a")) {
                findViewById(R.id.details_posted).setVisibility(View.GONE);
            } else {
                ((TextView) findViewById(R.id.details_posted)).setText(param.get(2));
            }
            if (param.get(3).equals("n/a")) {
                findViewById(R.id.details_visits_condition).setVisibility(View.GONE);
            } else {
                ((TextView) findViewById(R.id.details_visits_condition)).setText(param.get(3));
            }
            if (!param.get(4).isEmpty()) {
                findViewById(R.id.details_cardView).setVisibility(View.VISIBLE);
            }
            ((TextView) findViewById(R.id.details_description)).setText(param.get(4));

            if (images.isEmpty()) {
                findViewById(R.id.details_noImageText).setVisibility(View.VISIBLE);
            } else if (images.get(0).equals("error")) {
                findViewById(R.id.details_imageErrorText).setVisibility(View.VISIBLE);
            } else {
                ViewPagerFixed viewPager = findViewById(R.id.details_viewPager);
                ViewPagerAdapter adapter = new ViewPagerAdapter(this, images);
                viewPager.setAdapter(adapter);

                Utility.sliderDots(this, 0, adapter, viewPager);
            }
        }
        progressBar.setVisibility(View.GONE);
        darkScreen.setVisibility(View.GONE);
        Log.i(TAG, "DETAILS STUFF END");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.scale_in, R.anim.slide_out_left);
        }

        return super.onOptionsItemSelected(item);
    }

}

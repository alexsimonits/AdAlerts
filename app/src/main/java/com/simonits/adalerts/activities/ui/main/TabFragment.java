package com.simonits.adalerts.activities.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.simonits.adalerts.activities.DetailsActivity;
import com.simonits.adalerts.R;
import com.simonits.adalerts.activities.TabbedActivity;
import com.simonits.adalerts.adapters.Listings_RecAdapter;
import com.simonits.adalerts.dialogs.ConfirmDialog;
import com.simonits.adalerts.objects.Alert;
import com.simonits.adalerts.objects.Listing;
import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.PreferenceUtils;
import com.simonits.adalerts.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TabFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, Listings_RecAdapter.OnListingListener {

    private static final String TAG = "TabFragment";

    private String tabSite;
    private int tabNum;
    private boolean tabViewed, waitingToView, newListingsFound;
    private Alert alert;
    private Context context;
    private int listingPosition;

    private Listings_RecAdapter adapter;
    private RecyclerView recView;
    private boolean detailsClicked;

    private ArrayList<Listing> listingObjList, onlyNewListings;

    private int page;
    private String lastListingNumber;

    private Button more;
    private TextView endResultsText, fetchErrorText;
    private WebView webView;
    private ProgressBar progressBar;
    private RelativeLayout darkScreen;
    private volatile boolean jsInjected;
    private boolean fFiltered;

    public static TabFragment newInstance(int alertPosition, String tabSite, int tabNum) {
        TabFragment fragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("alertPosition", alertPosition);
        bundle.putString("tabSite", tabSite);
        bundle.putInt("tabNum", tabNum);
        fragment.setArguments(bundle);
        return fragment;
    }

    public TabFragment() {
        // supposed to do nothing and be empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int alertID = -1; // -1 to purposely error if arguments is null
        if (getArguments() != null) {
            alertID = getArguments().getInt("alertPosition");
            tabSite = getArguments().getString("tabSite");
            tabNum = getArguments().getInt("tabNum");
        }
        context = getActivity();
        alert = Objects.requireNonNull(Utility.getAlertFromList(PreferenceUtils.getAlertList(context), alertID));
        onlyNewListings = alert.getOnlyNewBgListings(tabSite);
        listingObjList = new ArrayList<>();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tabbed, container, false);

        progressBar = root.findViewById(R.id.tabbed_progressBar);
        darkScreen = root.findViewById(R.id.fragment_darkScreen);
        recView = root.findViewById(R.id.listings_recView);

        tabViewed = false;
        waitingToView = false;
        newListingsFound = false;
        page = 1;
        lastListingNumber = "";

        more = root.findViewById(R.id.button_more);
        more.setVisibility(View.GONE);
        more.setOnClickListener(view -> {
            more.setClickable(false);
            progressBar.setVisibility(View.VISIBLE);
            darkScreen.setVisibility(View.VISIBLE);
            if (tabSite.equals(Constants.TABSITE_FACEBOOK)) { // facebook tab doesn't use an AsyncTask. ALSO, this should never be facebook because there's no "more" button for facebook lol
                webView.loadUrl(alert.getFURL());
            } else {
                doBackgroundWork();
            }
        });

        endResultsText = root.findViewById(R.id.textView_endResults);
        endResultsText.setVisibility(View.GONE);
        fetchErrorText = root.findViewById(R.id.textView_fetchError);
        fetchErrorText.setVisibility(View.GONE);

        webView = root.findViewById(R.id.fragment_webView);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setUserAgentString(Utility.getFacebookUA());
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //String cookies = CookieManager.getInstance().getCookie(url);
                if (!jsInjected) {
                    jsInjected = true;
                    webView.loadUrl("javascript:window.HtmlViewer.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                }
            }
        });

        Log.i(TAG, " ");
        if (tabSite.equals(Constants.TABSITE_FACEBOOK)) { // facebook tab doesn't use an AsyncTask
            webView.loadUrl(alert.getFURL());
        } else {
            doBackgroundWork();
        }

        return root;
    }

    private void doBackgroundWork() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ArrayList<Listing> freshListingObjList;
            switch (tabSite) {
                case Constants.TABSITE_KIJIJI:
                    freshListingObjList = getKijijiData();
                    break;
                case Constants.TABSITE_EBAY:
                    freshListingObjList = getEbayData();
                    break;
                case Constants.TABSITE_CRAIGSLIST:
                    freshListingObjList = getCraigslistData();
                    break;
                default:
                    freshListingObjList = null;
                    break;
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                if (freshListingObjList == null) {
                    fetchErrorText.setVisibility(View.VISIBLE);
                    more.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    darkScreen.setVisibility(View.GONE);
                } else {
                    withResults(freshListingObjList);
                }
            });
        });
    }

    @Override
    public void onListingClick(int position) {
        detailsClicked = true;
        listingPosition = position;
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("tabSite", tabSite);
        intent.putExtra("listing", listingObjList.get(position));
        intent.putExtra("name", listingObjList.get(position).getName());
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.scale_out);
    }

    @Override
    public void onListingHold(View view, int position) {
        listingPosition = position;
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.setOnMenuItemClickListener(TabFragment.this);
        popupMenu.inflate(R.menu.menu_tabbed_popup);
        Menu menu = popupMenu.getMenu();
        if (listingObjList.get(position).getID().startsWith("k")) {
            menu.findItem(R.id.fragment_item_kijiji).setVisible(true);
        } else if (listingObjList.get(position).getID().startsWith("e")) {
            menu.findItem(R.id.fragment_item_ebay).setVisible(true);
        } else if (listingObjList.get(position).getID().startsWith("c")) {
            menu.findItem(R.id.fragment_item_craigslist).setVisible(true);
        } else if (listingObjList.get(position).getID().startsWith("f")) {
            menu.findItem(R.id.fragment_item_facebook).setVisible(true);
        }
        if (Utility.isInListings(PreferenceUtils.getFavsList(context), listingObjList.get(position).getID())) {
            menu.findItem(R.id.fragment_item_removeFav).setVisible(true);
        } else {
            menu.findItem(R.id.fragment_item_addFav).setVisible(true);
        }
        popupMenu.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, tabSite + " is visible!!!");
        tabViewed = true;
        if (waitingToView) {
            updateAlert();
            waitingToView = false;
        }
        if (detailsClicked) {
            String id = listingObjList.get(listingPosition).getID();
            if (Utility.isInListings(onlyNewListings, id)) {
                onlyNewListings.remove(Utility.getListingFromID(onlyNewListings, id));
                adapter.notifyDataSetChanged();
            }
            detailsClicked = false;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        ArrayList<Listing> favourites;
        int itemId = menuItem.getItemId();
        if (itemId == R.id.fragment_item_kijiji || itemId == R.id.fragment_item_ebay || itemId == R.id.fragment_item_craigslist || itemId == R.id.fragment_item_facebook) {
            detailsClicked = true;
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(listingObjList.get(listingPosition).getUrl())));
        } else if (itemId == R.id.fragment_item_addFav) {
            favourites = PreferenceUtils.getFavsList(getActivity());
            favourites.add(0, listingObjList.get(listingPosition));
            PreferenceUtils.saveFavsList(favourites, getActivity());
            Snackbar.make(requireActivity().findViewById(android.R.id.content), "Added to favourites", Snackbar.LENGTH_SHORT).show();
        } else if (itemId == R.id.fragment_item_removeFav) {
            favourites = PreferenceUtils.getFavsList(getActivity());
            int i = 0;
            for (Listing listing : favourites) {
                if (listing.getID().equals(listingObjList.get(listingPosition).getID())) {
                    favourites.remove(i);
                    PreferenceUtils.saveFavsList(favourites, getActivity());
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), "Removed from favourites", Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                i++;
            }
        } else if (itemId == R.id.fragment_item_spam) {
            ConfirmDialog dialog = new ConfirmDialog(this.getContext(), listingObjList.get(listingPosition).getName(), Constants.DIALOGTYPE_SPAM);
            requireActivity().getSupportFragmentManager().setFragmentResultListener(Constants.KEY_FRAGMENTREQUEST, dialog, (requestKey, result) -> {
                markSpam(listingPosition);
                if (Utility.isInListings(onlyNewListings, listingObjList.get(listingPosition).getID())) {
                    onlyNewListings.remove(Utility.getListingFromID(onlyNewListings, listingObjList.get(listingPosition).getID()));
                }
                Utility.removeSpam(this.getContext(), listingObjList);
                if (tabSite.equals(Constants.TABSITE_FACEBOOK)) {
                    Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, null, -1, null, 0, null);
                }

                adapter = new Listings_RecAdapter(getContext(), listingObjList, onlyNewListings, this);
                recView.setLayoutManager(new GridLayoutManager(getContext(), 1));
                recView.setAdapter(adapter);
            });
            dialog.show(requireActivity().getSupportFragmentManager(), "confirm dialog");
        } else {
            return false;
        }
        return true;
    }

    private ArrayList<Listing> getKijijiData() {
        String kLatLong = alert.getKLatLong();
        String url = alert.getKURL(page, this.getContext());
        if (!kLatLong.equals(alert.getKLatLong())) {
            Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, tabSite, 8, null, alert.getId(), alert);
        }
        Log.i(TAG, "~~~TabFragment " + url + " ~~~");
        FirebaseCrashlytics.getInstance().log("~~~TabFragment " + url + " ~~~");
        String htmlStr = Utility.getHtml(Utility.getClient(tabSite), url);
        if (htmlStr == null) {
            return null;
        }
        ArrayList<Listing> freshListingList = Utility.scrapeKijiji(context, htmlStr, alert);
        if (!freshListingList.isEmpty() && freshListingList.get(0).getID().equals("error")) {
            Log.i(TAG, "Data Fetch Error!!!");
            FirebaseCrashlytics.getInstance().log("Data Fetch Error!!!");
            return null;
        } else {
            page++;
            String nextPage = Objects.requireNonNull(Utility.getScrapeMap(Constants.TABSITE_KIJIJI, "listings").get("nextPage")).replace("PAGENUMBER", Integer.toString(page));
            if (!htmlStr.contains(nextPage)) { // if another page doesn't exist
                if (page == 2) {
                    page = -1;
                } else {
                    page = 0;
                }
            }
        }
        return freshListingList;
    }

    private ArrayList<Listing> getEbayData() {
        String url = alert.getEURL(page);
        Log.i(TAG, "~~~TabFragment " + url + " ~~~");
        FirebaseCrashlytics.getInstance().log("~~~TabFragment " + url + " ~~~");
        String htmlStr = Utility.getHtml(Utility.getClient(tabSite), url);
        if (htmlStr == null) {
            return null;
        }
        ArrayList<Listing> freshListingList = Utility.scrapeEbay(context, htmlStr, alert);
        if (!freshListingList.isEmpty() && freshListingList.get(0).getID().equals("error")) {
            Log.i(TAG, "Data Fetch Error!!!");
            FirebaseCrashlytics.getInstance().log("Data Fetch Error!!!");
            return null;
        } else {
            page++;
            HashMap<String, String> map = Utility.getScrapeMap(Constants.TABSITE_EBAY, "listings");
            if (!htmlStr.contains(Objects.requireNonNull(map.get("nextPageExists")))) { // if another page doesn't exist
                if (page == 2) {
                    page = -1;
                } else {
                    page = 0;
                }
            }
        }
        return freshListingList;
    }

    private ArrayList<Listing> getCraigslistData() {
        String url = alert.getCURL(page, lastListingNumber);
        Log.i(TAG, "~~~TabFragment " + url + " ~~~");
        FirebaseCrashlytics.getInstance().log("~~~TabFragment " + url + " ~~~");
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(chain -> {
                    final Request original = chain.request();
                    final Request authorized = original.newBuilder()
                            // don't think i need cookies to get consistent data from Craigslist. Craigslist is just that easy boi
                            .build();
                    return chain.proceed(authorized);
                })
                .build();
        String htmlStr = Utility.getHtml(client, url);
        if (htmlStr == null) {
            return null;
        }
        ArrayList<Listing> freshListingList = Utility.scrapeCraigslist(context, htmlStr, alert);
        if (!freshListingList.isEmpty() && freshListingList.get(0).getID().equals("error")) {
            Log.i(TAG, "Data Fetch Error!!!");
            FirebaseCrashlytics.getInstance().log("Data Fetch Error!!!");
            return null;
        } else {
            page++;
            try {
                HashMap<String, String> map = Utility.getScrapeMap(Constants.TABSITE_CRAIGSLIST, "listings");
                int startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("nextPage1"))) + Objects.requireNonNull(map.get("nextPage1")).length();
                String rangeTo = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("nextPage2")), startIndex));
                startIndex = htmlStr.indexOf(Objects.requireNonNull(map.get("nextPage3"))) + Objects.requireNonNull(map.get("nextPage3")).length();
                String totalCount = htmlStr.substring(startIndex, htmlStr.indexOf(Objects.requireNonNull(map.get("nextPage4")), startIndex));
                if (rangeTo.equals(totalCount) || (!htmlStr.contains(Objects.requireNonNull(map.get("nextPage1"))) && !htmlStr.contains(Objects.requireNonNull(map.get("nextPage3"))))) { // if another page doesn't exist
                    if (page == 2) {
                        page = -1;
                    } else {
                        page = 0;
                    }
                } else {
                    lastListingNumber = rangeTo;
                }
            } catch (StringIndexOutOfBoundsException e) {
                Log.i(TAG, "HTML SCRAPE ERROR: 'nextPage' StringIndexOutOfBoundsException caught");
                Log.e(TAG, e.toString());
                FirebaseCrashlytics.getInstance().recordException(e);
                if (page == 2) {
                    page = -1;
                } else {
                    page = 0;
                }
            }
        }
        return freshListingList;
    }

    private void withResults(ArrayList<Listing> freshListingObjList) {
        if (page == 2 || page == -1 || tabSite.equals(Constants.TABSITE_FACEBOOK)) {
            Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, tabSite, 6, null, alert.getId(), null); // sets updateDate
            listingObjList = freshListingObjList;
            ArrayList<Listing> freshNewListings = new ArrayList<>();
            for (Listing listing : freshListingObjList) {
                if (listing.isNewListing()) {
                    onlyNewListings.add(listing);
                    newListingsFound = true;
                    freshNewListings.add(listing);
                }
            }
            if (newListingsFound) {
                updateTabTitle();
            }
            if (tabViewed) {
                updateAlert();
            } else {
                waitingToView = true;
                if (newListingsFound) {
                    Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, tabSite, 5, freshNewListings, alert.getId(), null); // adds new listings found to the alert.onlyNewBgListings
                }
            }
            for (Listing listing : onlyNewListings) {
                if (!Utility.isInListings(listingObjList, listing.getID())) {
                    listingObjList.add(0, listing);
                }
            } // adds in onlyNewBgListings that were notified to the user but for any reason are not in the freshly fetched list
        } else { // this is for when they click the "more" button to get the next page of results
            listingObjList.addAll(freshListingObjList);
        }

        if (!tabSite.equals(Constants.TABSITE_FACEBOOK)) {
            if (page == 0 || page == -1 || freshListingObjList.isEmpty()) {
                more.setVisibility(View.GONE);
                endResultsText.setVisibility(View.VISIBLE);
            } else {
                more.setClickable(true);
                more.setVisibility(View.VISIBLE);
                endResultsText.setVisibility(View.GONE);
            }
        } else {
            more.setVisibility(View.GONE);
        }

        Log.i(TAG, "Tab " + tabSite + " onlyNewListings IDs: " + Arrays.toString(Utility.getListingsIDs(onlyNewListings).toArray()));
        Log.i(TAG, " ");

        adapter = new Listings_RecAdapter(getContext(), listingObjList, onlyNewListings, this);
        recView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);
        darkScreen.setVisibility(View.GONE);
    }

    private ArrayList<Listing> extractFacebookData(String htmlStr) {
        Log.i(TAG, "~~~TabFragment " + alert.getFURL() + " ~~~");
        FirebaseCrashlytics.getInstance().log("~~~TabFragment " + alert.getFURL() + " ~~~");
        ArrayList<Listing> freshListingList = Utility.scrapeFacebook(context, htmlStr, alert);
        if (!freshListingList.isEmpty() && (freshListingList.get(0).getID().equals("error") || freshListingList.get(0).getID().equals("accountTimeOut"))) {
            freshListingList.clear();
        } else {
            int size = freshListingList.size();
            Utility.removeSpam(context, Utility.removeFiltered(freshListingList));
            if (freshListingList.size() < size) {
                fFiltered = true;
            }
            Utility.updateFavourites(context, freshListingList);
        }
        return freshListingList;
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void showHTML(String html) {
            requireActivity().runOnUiThread(() -> {
                fFiltered = false;
                ArrayList<Listing> freshListingList = extractFacebookData(html);
                if (freshListingList.isEmpty() && !fFiltered) {
                    Log.i(TAG, "FACEBOOK DATA FETCH ERROR!!! TRYING AGAIN");
                    FirebaseCrashlytics.getInstance().log("FACEBOOK DATA FETCH ERROR!!! TRYING AGAIN");
                    jsInjected = false;
                } else if (freshListingList.isEmpty() || freshListingList.get(0).getID().equals("no listings found")) {
                    webView.setVisibility(View.GONE);
                    Log.i(TAG, "NO FACEBOOK LISTINGS FOUND");
                    FirebaseCrashlytics.getInstance().log("NO FACEBOOK LISTINGS FOUND");
                    if (!alert.getFListingHistory().isEmpty() && !alert.getFListingHistory().get(0).getID().equals("no listings found")) {
                        withResults(alert.getFListingHistory());
                    } else {
                        Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, tabSite, 3, null, alert.getId(), null);
                        endResultsText.setVisibility(View.VISIBLE);
                        withResults(new ArrayList<>());
                    }
                } else {
                    webView.setVisibility(View.GONE);
                    Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, tabSite, 4, freshListingList, alert.getId(), null);
                    alert = Objects.requireNonNull(Utility.getAlertFromList(PreferenceUtils.getAlertList(context), alert.getId()));
                    withResults(alert.getFListingHistory());
                    endResultsText.setVisibility(View.GONE);
                }
            });
        }
    }

    private void markSpam(int position) {
        if (!listingObjList.get(position).getImage().equals("no image") && !listingObjList.get(position).getImage().equals("-IMAGE ERROR-")) {
            if (!PreferenceUtils.spamCheck(listingObjList.get(position).getImage(), this.getContext())) {
                PreferenceUtils.addSpam(listingObjList.get(position).getImage(), this.getContext());
            }
        }
        if (!listingObjList.get(position).getName().equals("-TITLE ERROR-")) {
            if (!PreferenceUtils.spamCheck(listingObjList.get(position).getName(), this.getContext())) {
                PreferenceUtils.addSpam(listingObjList.get(position).getName(), this.getContext());
            }
        }
        Snackbar.make(requireActivity().findViewById(android.R.id.content), "Marked as spam", Snackbar.LENGTH_SHORT).show();
    }

    private void updateAlert() {
        Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, tabSite, 1, null, alert.getId(), null);
        if (newListingsFound) {
            Utility.updateSavedPrefs(context, Constants.UTILITY_TABBEDACTIVITY, tabSite, 2, listingObjList, alert.getId(), null);
        }
        if (!onlyNewListings.isEmpty()) {
            ArrayList<Listing> savedNewListings = PreferenceUtils.getNewListingsList(context);
            for (int i = 0; i < savedNewListings.size(); i++) {
                if (Utility.isInListings(onlyNewListings, savedNewListings.get(i).getID())) {
                    savedNewListings.remove(i);
                    i--;
                }
            }
            ArrayList<Listing> updatedSavedNewListings = new ArrayList<>(onlyNewListings);
            updatedSavedNewListings.addAll(savedNewListings);
            while (updatedSavedNewListings.size() > 100) {
                updatedSavedNewListings.remove(updatedSavedNewListings.size() - 1);
            } // makes sure updatedSavedNewListings does not get too long to where the app lags hard in this recView
            PreferenceUtils.saveNewListingsList(updatedSavedNewListings, context);
        }
    }

    private void updateTabTitle() {
        String tabTitle = Objects.requireNonNull(Objects.requireNonNull(TabbedActivity.tabs.getTabAt(tabNum)).getText()).toString();
        if (!tabTitle.contains("(!)")) {
            Objects.requireNonNull(TabbedActivity.tabs.getTabAt(tabNum)).setText("(!) " + tabTitle);
        }
    }

}
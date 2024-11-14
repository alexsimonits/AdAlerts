package com.simonits.adalerts.objects;

import android.content.Context;

import com.simonits.adalerts.utils.Constants;
import com.simonits.adalerts.utils.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

public class Alert extends AlertClassHelper {

    private Date updateDate = new GregorianCalendar(1997, Calendar.AUGUST, 24, 0, 0, 0).getTime();
    private int id;
    private final String name;
    private String customName;
    private final ArrayList<Listing> onlyNewBgListings;

    private ArrayList<Listing> kListings, eListings, cListings;
    private ArrayList<Listing> fListingHistory;

    private final boolean kijiji, ebay, craigslist;
    private boolean facebook;

    private Date kLatestListingDate, eLatestListingDate, cLatestListingDate;
    private final Date fLatestListingDate;
    private ArrayList<String> kIDsHistory, eIDsHistory, cIDsHistory;

    // AlertActivity options //
    private String kURL = "";
    private String kURL_nextPage = "";
    private final String kNameUrl;
    private final String kLoc;
    private String kLocationName;
    private String kLocationID;
    private final String kKm;
    private final String kPostalZip;
    private String kLatLong = "";
    private final String kCat;
    private String kCatName;
    private String kCatID;
    private final String kPrice, kFrom, kTo, kPriceUrl;
    private final String kType;
    private final String kTypeUrl;
    private String kInclude, kIncludeAny, kExclude;
    private String eURL = "";
    private String eURL_nextPage = "";
    private final String eLoc;
    private final String eKm;
    private final String ePostalZip;
    private final String eLocUrl;
    private final String eCat;
    private String eCatUrl;
    private final String ePrice, eFrom, eTo, ePriceUrl;
    private final String eType;
    private final String eTypeUrl;
    private String eInclude, eIncludeAny, eExclude;
    private String cURL = "";
    private String cURL_nextPage = "";
    private final String cNameUrl;
    private final String cLoc;
    private String cLocUrl;
    private final String cPrice, cFrom, cTo, cPriceUrl;
    private final String cCat;
    private String cCatURL;
    private String cInclude, cIncludeAny, cExclude;
    private String fURL = "";
    private final String fNameUrl;
    private String fCat;
    private String fCatUrl;
    private final String fPrice, fFrom, fTo, fPriceUrl;
    private String fInclude, fIncludeAny, fExclude;

    public Alert(int id, String name, String customName, boolean kijiji, boolean ebay, boolean craigslist, boolean facebook, String kLoc, String kKm, String kPostalZip, String kCat, String kPrice, String kFrom, String kTo, String kType, String kInclude, String kIncludeAny, String kExclude, String eLoc, String eKm, String ePostalZip, String eCat, String ePrice, String eFrom, String eTo, String eType, String eInclude, String eIncludeAny, String eExclude, String cLoc, String cPrice, String cFrom, String cTo, String cCat, String cInclude, String cIncludeAny, String cExclude, String fCat, String fPrice, String fFrom, String fTo, String fInclude, String fIncludeAny, String fExclude) {
        this.id = id;
        if (name.isEmpty() && facebook) {
            name = fCat;
        } else if (name.isEmpty() && kijiji) {
            name = kCat;
        } else if (name.isEmpty() && craigslist) {
            name = cCat;
        }
        this.name = name;
        this.customName = customName != null ? customName : "";
        onlyNewBgListings = new ArrayList<>();
        kListings = new ArrayList<>();
        eListings = new ArrayList<>();
        cListings = new ArrayList<>();
        fListingHistory = new ArrayList<>();

        this.kijiji = kijiji;
        this.ebay = ebay;
        this.craigslist = craigslist;
        this.facebook = facebook;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0); // round the minute down, taking off the seconds (in case a new listing is of the same minute)
        kLatestListingDate = calendar.getTime();
        eLatestListingDate = calendar.getTime();
        cLatestListingDate = calendar.getTime();
        fLatestListingDate = calendar.getTime(); // fLatestListingDate always stays the creation date because facebook has much different listing management

        kIDsHistory = new ArrayList<>();
        eIDsHistory = new ArrayList<>();
        cIDsHistory = new ArrayList<>();

        if (name.equals(kCat)) {
            kNameUrl = "";
        } else {
            kNameUrl = "/" + name.replace(" ", "-");
        }
        this.kLoc = kLoc;
        this.kKm = kKm;
        if (!kPostalZip.isEmpty() && kPostalZip.length() != 6) {
            kPostalZip = kPostalZip.substring(0, 3);
        }
        this.kPostalZip = kPostalZip;
        String kKmLatLongURL = createKLocation(kLoc, kKm);
        kLocationName = getKLocationName();
        kLocationID = getKLocationID();
        this.kCat = kCat;
        createKCat(kCat);
        kCatName = getKCatName();
        kCatID = getKCatID();
        if (!kCatName.isEmpty() && !kCatID.isEmpty()) {
            kLocationName = kLocationName.replace("b-", "");
        }
        this.kPrice = kPrice;
        this.kFrom = kFrom;
        this.kTo = kTo;
        kPriceUrl = getUrlPrice(kPrice, kFrom, kTo, "&price=", "__");
        this.kType = kType;
        kTypeUrl = createKType(kType);
        this.kInclude = kInclude;
        this.kIncludeAny = kIncludeAny;
        this.kExclude = kExclude;

        this.eLoc = eLoc;
        this.eKm = eKm;
        this.ePostalZip = ePostalZip;
        eLocUrl = createELocation(eLoc, eKm, ePostalZip);
        this.eCat = eCat;
        eCatUrl = getUrlPart(eCat, "eCategories");
        this.ePrice = ePrice;
        this.eFrom = eFrom;
        this.eTo = eTo;
        ePriceUrl = getUrlPrice(ePrice, eFrom, eTo, "&_udlo=", "&_udhi=");
        this.eType = eType;
        eTypeUrl = createEType(eType);
        this.eInclude = eInclude;
        this.eIncludeAny = eIncludeAny;
        this.eExclude = eExclude;

        if (name.equals(cCat)) {
            cNameUrl = "";
        } else {
            cNameUrl = "&query=" + name.replace(" ", "+");
        }
        this.cLoc = cLoc;
        cLocUrl = getUrlPart(cLoc, "cLocations");
        this.cFrom = cFrom;
        this.cTo = cTo;
        this.cPrice = cPrice;
        cPriceUrl = getUrlPrice(cPrice, cFrom, cTo, "&min_price=", "&max_price=");
        this.cCat = cCat;
        cCatURL = getUrlPart(cCat, "cCategories0_64");
        this.cInclude = cInclude;
        this.cIncludeAny = cIncludeAny;
        this.cExclude = cExclude;

        if (name.equals(fCat)) {
            fNameUrl = "";
        } else {
            fNameUrl = "&query=" + name.replace(" ", "%20");
        }
        this.fCat = fCat;
        fCatUrl = getUrlPart(fCat, "fCategories1_3_4");
        this.fPrice = fPrice;
        this.fFrom = fFrom;
        this.fTo = fTo;
        fPriceUrl = getUrlPrice(fPrice, fFrom, fTo, "&minPrice=", "&maxPrice=");
        this.fInclude = fInclude;
        this.fIncludeAny = fIncludeAny;
        this.fExclude = fExclude;

        createURLs(kKmLatLongURL);
    }

    private void createURLs(String kKmLatLongURL) {
        if (kijiji) {
            String autoCat = "";
            if (kCatName.equals("auto-detect")) {
                kCatName = "";
                autoCat = "&dc=true";
            }
            String extraID = "";
            if (!kNameUrl.isEmpty()) {
                extraID = "k0";
            }
            kURL = "https://www.kijiji.ca/" + kCatName + kLocationName + kNameUrl + "/" + extraID + kCatID + kLocationID + "?sort=dateDesc" + kKmLatLongURL + kTypeUrl + kPriceUrl + autoCat;
            kURL_nextPage = "https://www.kijiji.ca/" + kCatName + kLocationName + kNameUrl + "/page-*PAGENUMBER*/" + extraID + kCatID + kLocationID + "?sort=dateDesc" + kKmLatLongURL + kTypeUrl + kPriceUrl + autoCat;
        }
        if (ebay) {
            String allCats = "";
            if (eCatUrl.isEmpty()) {
                allCats = "&_oac=1&_sacat=0";
            } else if (eCatUrl.equals("auto-detect")) {
                eCatUrl = "";
            }
            eURL = "https://www.ebay.ca/sch" + eCatUrl + "/i.html?_nkw=" + name.replace(" ", "+") + "&_sop=10&_ipg=25" + eLocUrl + ePriceUrl + eTypeUrl + allCats + "&rt=nc&_dmd=1";
            eURL_nextPage = "https://www.ebay.ca/sch" + eCatUrl + "/i.html?_nkw=" + name.replace(" ", "+") + "&_sop=10&_ipg=25&_pgn=*PAGENUMBER*" + eLocUrl + ePriceUrl + eTypeUrl + allCats + "&rt=nc&_dmd=1";
        }
        if (craigslist) {
            cURL = "https://" + cLocUrl + ".craigslist.org/search/" + cCatURL + "?sort=date" + cPriceUrl + cNameUrl;
            cURL_nextPage = "https://" + cLocUrl + ".craigslist.org/search/" + cCatURL + "?s=*LASTLISTINGNUMBER*" + "&sort=date" + cPriceUrl + cNameUrl;
        }
        if (facebook) {
            fURL = "https://www.facebook.com/marketplace/search/?sortBy=creation_time_descend" + fNameUrl + fCatUrl + fPriceUrl;
        }
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void refreshUpdateDate() {
        updateDate = Calendar.getInstance().getTime();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public ArrayList<Listing> getOnlyNewBgListings() {
        return onlyNewBgListings;
    }

    public ArrayList<Listing> getOnlyNewBgListings(String tabSite) {
        ArrayList<Listing> listings = new ArrayList<>();
        for (Listing listing : onlyNewBgListings) {
            if (tabSite.equals(Constants.TABSITE_KIJIJI) && listing.getID().startsWith("k")) {
                listings.add(listing);
            } else if (tabSite.equals(Constants.TABSITE_EBAY) && listing.getID().startsWith("e")) {
                listings.add(listing);
            } else if (tabSite.equals(Constants.TABSITE_CRAIGSLIST) && listing.getID().startsWith("c")) {
                listings.add(listing);
            } else if (tabSite.equals(Constants.TABSITE_FACEBOOK) && listing.getID().startsWith("f")) {
                listings.add(listing);
            }
        }
        return listings;
    }

    public void addOnlyNewBgListing(Listing listing) {
        for (int i = 0; i < onlyNewBgListings.size(); i++) {
            if (onlyNewBgListings.get(i).getID().equals(listing.getID())) {
                onlyNewBgListings.remove(i);
                onlyNewBgListings.add(i, listing);
                return;
            }
        }
        onlyNewBgListings.add(listing);
    }

    public void removeOnlyNewBgListing(Listing listing) {
        for (int i = 0; i < onlyNewBgListings.size(); i++) {
            if (onlyNewBgListings.get(i).getID().equals(listing.getID())) {
                onlyNewBgListings.remove(i);
                return;
            }
        }
    }

    public void removeOnlyNewBgListings(String tabSite) {
        ArrayList<Listing> removalListings = getOnlyNewBgListings(tabSite);
        if (!removalListings.isEmpty()) {
            for (Listing listing : removalListings) {
                removeOnlyNewBgListing(listing);
            }
        }
    }

    public ArrayList<Listing> getKListings() {
        return kListings;
    }

    public void setKListings(ArrayList<Listing> kBgListings) {
        kListings = kBgListings;
        if (!kListings.isEmpty()) {
            kLatestListingDate = updateLatestListingDate(kListings, kLatestListingDate);
            updateIDsHistory(kListings, kIDsHistory, 50);// reportedly actually 40, but sometimes counts a bit over that
        }
    }

    public ArrayList<Listing> getEListings() {
        return eListings;
    }

    public void setEListings(ArrayList<Listing> eBgListings) {
        eListings = eBgListings;
        if (!eListings.isEmpty()) {
            eLatestListingDate = updateLatestListingDate(eListings, eLatestListingDate);
            updateIDsHistory(eListings, eIDsHistory, 50);
        }
    }

    public ArrayList<Listing> getCListings() {
        return cListings;
    }

    public void setCListings(ArrayList<Listing> cBgListings) {
        cListings = cBgListings;
        if (!cListings.isEmpty()) {
            cLatestListingDate = updateLatestListingDate(cListings, cLatestListingDate);
            updateIDsHistory(cListings, cIDsHistory, 120);
        }
    }

    public ArrayList<Listing> getFListingHistory() {
        return fListingHistory;
    }

    public boolean hasNewListings(String tabSite) {
        switch (tabSite) {
            case Constants.TABSITE_KIJIJI:
                for (Listing listing : kListings) {
                    if (listing.isNewListing()) {
                        return true;
                    }
                }
                break;
            case Constants.TABSITE_EBAY:
                for (Listing listing : eListings) {
                    if (listing.isNewListing()) {
                        return true;
                    }
                }
                break;
            case Constants.TABSITE_CRAIGSLIST:
                for (Listing listing : cListings) {
                    if (listing.isNewListing()) {
                        return true;
                    }
                }
                break;
            case Constants.TABSITE_FACEBOOK:
                for (Listing listing : fListingHistory) {
                    if (listing.isNewListing()) {
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    public void setNewListingsFalse(String tabSite) {
        switch (tabSite) {
            case Constants.TABSITE_KIJIJI:
                for (Listing listing : kListings) {
                    listing.setNewListing(false);
                }
                break;
            case Constants.TABSITE_EBAY:
                for (Listing listing : eListings) {
                    listing.setNewListing(false);
                }
                break;
            case Constants.TABSITE_CRAIGSLIST:
                for (Listing listing : cListings) {
                    listing.setNewListing(false);
                }
                break;
            case Constants.TABSITE_FACEBOOK:
                for (Listing listing : fListingHistory) {
                    listing.setNewListing(false);
                }
                break;
        }
    }

    public void setListingsToFreshListings(String site, ArrayList<Listing> listings) {
        switch (site) {
            case Constants.TABSITE_KIJIJI:
                for (Listing listing : listings) {
                    listing.setNewListing(false);
                }
                kListings = listings;
                if (!kListings.isEmpty()) {
                    kLatestListingDate = updateLatestListingDate(kListings, kLatestListingDate);
                    updateIDsHistory(kListings, kIDsHistory, 50);// reportedly actually 40, but sometimes counts a bit over that
                }
                break;
            case Constants.TABSITE_EBAY:
                for (Listing listing : listings) {
                    listing.setNewListing(false);
                }
                eListings = listings;
                if (!eListings.isEmpty()) {
                    eLatestListingDate = updateLatestListingDate(eListings, eLatestListingDate);
                    updateIDsHistory(eListings, eIDsHistory, 50);
                }
                break;
            case Constants.TABSITE_CRAIGSLIST:
                for (Listing listing : listings) {
                    listing.setNewListing(false);
                }
                cListings = listings;
                if (!cListings.isEmpty()) {
                    cLatestListingDate = updateLatestListingDate(cListings, cLatestListingDate);
                    updateIDsHistory(cListings, cIDsHistory, 120);
                }
                break;
            case Constants.TABSITE_FACEBOOK:
                for (Listing listing : listings) {
                    Listing fListing = Utility.getListingFromID(fListingHistory, listing.getID());
                    if (fListing != null) {
                        fListing.setNewListing(false);
                        // ^^ OnlyNewListings are already added to fHistory so we don't add again
                    }
                }
                break;
        }
    }

    public boolean hasKijiji() {
        return kijiji;
    }

    public boolean hasEbay() {
        return ebay;
    }

    public boolean hasCraigslist() {
        return craigslist;
    }

    public boolean hasFacebook() {
        return facebook;
    }

    public void disableFacebook() {
        facebook = false;
    }

    public String getKURL(int page, Context context) {
        if (kURL.contains("&ll=&")) {
            kLatLong = Utility.getLatLong(kPostalZip, context);
            if (!kLatLong.isEmpty()) {
                kURL = kURL.substring(0, kURL.indexOf("&ll=") + 4) + kLatLong + kURL.substring(kURL.indexOf("&ll=") + 4);
                kURL_nextPage = kURL_nextPage.substring(0, kURL_nextPage.indexOf("&ll=") + 4) + kLatLong + kURL_nextPage.substring(kURL_nextPage.indexOf("&ll=") + 4);
            }
        }

        if (page == 1) {
            return kURL;
        } else {
            return kURL_nextPage.replace("*PAGENUMBER*", Integer.toString(page));
        }
    }

    public String getEURL(int page) {
        if (page == 1) {
            return eURL;
        } else {
            return eURL_nextPage.replace("*PAGENUMBER*", Integer.toString(page));
        }
    }

    public String getCURL(int page, String lastListingNumber) {
        if (page == 1) {
            return cURL;
        } else {
            return cURL_nextPage.replace("*LASTLISTINGNUMBER*", lastListingNumber);
        }
    }

    public String getFURL() {
        return fURL;
    }

    public void setFListingHistory(ArrayList<Listing> fListingHistory) {
        this.fListingHistory = fListingHistory;
    }

    public void addToFListingHistory(ArrayList<Listing> listings) {
        int i = 0;
        for (Listing listing : listings) {
            if (!Utility.isInListings(fListingHistory, listing.getID())) {
                // adds brand new listings to the top of fListingHistory
                fListingHistory.add(i, listing);
                i++;
            } else {
                // swaps out an old version of a listing in fListingHistory with a new version from listings, but sets new listing date as old listing date
                Listing oldListing = Objects.requireNonNull(Utility.getListingFromID(fListingHistory, listing.getID()));
                listing.setDate(oldListing.getDate(), oldListing.getPermaDateStr());
                int index = fListingHistory.indexOf(oldListing);
                fListingHistory.remove(index);
                fListingHistory.add(index, listing);
            }
        }

        while (fListingHistory.size() > 75) {
            fListingHistory.remove(fListingHistory.size() - 1);
        } // makes sure fListingHistory does not get too long to where the app lags hard in TabFragment, amongst other areas
    }

    public Date getLatestListingDate(String site) {
        switch (site) {
            case Constants.TABSITE_KIJIJI:
                return kLatestListingDate;
            case Constants.TABSITE_EBAY:
                return eLatestListingDate;
            case Constants.TABSITE_CRAIGSLIST:
                return cLatestListingDate;
            case Constants.TABSITE_FACEBOOK:
                return fLatestListingDate;
            default:
                return null;
        }
    }

    public ArrayList<String> getIDsHistory(String site) {
        switch (site) {
            case Constants.TABSITE_KIJIJI:
                return kIDsHistory;
            case Constants.TABSITE_EBAY:
                return eIDsHistory;
            case Constants.TABSITE_CRAIGSLIST:
                return cIDsHistory;
            default:
                return null;
        }
    }

    public void setIDsHistory() {
        kIDsHistory = new ArrayList<>();
        eIDsHistory = new ArrayList<>();
        cIDsHistory = new ArrayList<>();
    } // temporary. 1.21 update

    private static Date updateLatestListingDate(ArrayList<Listing> listings, Date latestListingDate) {
        Date freshDate = listings.get(0).getDate();
        if (freshDate.after(latestListingDate)) {
            latestListingDate = freshDate;
        }
        return latestListingDate;
    }

    private static void updateIDsHistory(ArrayList<Listing> listings, ArrayList<String> IDsHistory, int maxListingsPerPage) {
        int i = 0;
        for (Listing listing : listings) {
            if (!IDsHistory.contains(listing.getID())) {
                IDsHistory.add(i, listing.getID());
                i++;
            }
        }
        while (IDsHistory.size() > maxListingsPerPage) {
            IDsHistory.remove(IDsHistory.size() - 1);
        }
    }

    public String getKLoc() {
        return kLoc;
    }

    public String getKKm() {
        return kKm;
    }

    public String getKPostalZip() {
        return kPostalZip;
    }

    public String getKLatLong() {
        return kLatLong;
    }

    public String getKCat() {
        return kCat;
    }

    public String getKPrice() {
        return kPrice;
    }

    public String getKFrom() {
        return kFrom;
    }

    public String getKTo() {
        return kTo;
    }

    public String getKType() {
        return kType;
    }

    public String getKInclude() {
        return kInclude;
    }

    public String getKIncludeAny() {
        return kIncludeAny;
    }

    public String getKExclude() {
        return kExclude;
    }

    public String getELoc() {
        return eLoc;
    }

    public String getEKm() {
        return eKm;
    }

    public String getEPostalZip() {
        return ePostalZip;
    }

    public String getECat() {
        return eCat;
    }

    public String getEPrice() {
        return ePrice;
    }

    public String getEFrom() {
        return eFrom;
    }

    public String getETo() {
        return eTo;
    }

    public String getEType() {
        return eType;
    }

    public String getEInclude() {
        return eInclude;
    }

    public String getEIncludeAny() {
        return eIncludeAny;
    }

    public String getEExclude() {
        return eExclude;
    }

    public String getCLoc() {
        return cLoc;
    }

    public String getCPrice() {
        return cPrice;
    }

    public String getCFrom() {
        return cFrom;
    }

    public String getCTo() {
        return cTo;
    }

    public String getCCat() {
        return cCat;
    }

    public String getCInclude() {
        return cInclude;
    }

    public String getCIncludeAny() {
        return cIncludeAny;
    }

    public String getCExclude() {
        return cExclude;
    }

    public String getFPrice() {
        return fPrice;
    }

    public String getFFrom() {
        return fFrom;
    }

    public String getFTo() {
        return fTo;
    }

    public void setFURL() {
        if (facebook) {
            try {
                fCatUrl = getUrlPart(fCat, "fCategories1_3_4");
            } catch (NullPointerException e) {
                fCat = "All Categories";
                fCatUrl = "";
            }
            fURL = "https://www.facebook.com/marketplace/search/?sortBy=creation_time_descend&query=" + name.replace(" ", "%20") + fCatUrl + fPriceUrl + "&exact=false";
        }
    } // temporary. 1.3.4 update

    public String getFCat() {
        return fCat;
    }

    public String getFInclude() {
        return fInclude;
    }

    public String getFIncludeAny() {
        return fIncludeAny;
    }

    public String getFExclude() {
        return fExclude;
    }

    public void removeBrokenFacebookListings() {
        for (int i = 0; i < fListingHistory.size(); i++) {
            if (fListingHistory.get(i).getID().equals("accountTimeOut")) {
                fListingHistory.remove(i);
                i--;
            }
        }
    } // temp. 106 update

    public void setIncludeAny() {
        kIncludeAny = "";
        eIncludeAny = "";
        cIncludeAny = "";
        fIncludeAny = "";
    } // temp. 108 update

    public void formatKeywords() {
        kInclude = kInclude.replaceAll(",", "").replaceAll(" ", ", ");
        kIncludeAny = kIncludeAny.replaceAll(",", "").replaceAll(" ", ", ");
        kExclude = kExclude.replaceAll(",", "").replaceAll(" ", ", ");
        eInclude = eInclude.replaceAll(",", "").replaceAll(" ", ", ");
        eIncludeAny = eIncludeAny.replaceAll(",", "").replaceAll(" ", ", ");
        eExclude = eExclude.replaceAll(",", "").replaceAll(" ", ", ");
        cInclude = cInclude.replaceAll(",", "").replaceAll(" ", ", ");
        cIncludeAny = cIncludeAny.replaceAll(",", "").replaceAll(" ", ", ");
        cExclude = cExclude.replaceAll(",", "").replaceAll(" ", ", ");
        fInclude = fInclude.replaceAll(",", "").replaceAll(" ", ", ");
        fIncludeAny = fIncludeAny.replaceAll(",", "").replaceAll(" ", ", ");
        fExclude = fExclude.replaceAll(",", "").replaceAll(" ", ", ");
    } // temp. 122 update

    public void adjustFURL() {
        if (facebook && !fNameUrl.isEmpty()) {
            fURL = "https://www.facebook.com/marketplace/search?daysSinceListed=1&sortBy=best_match" + fNameUrl + fCatUrl + fPriceUrl;
        }
    } // temp. 122 update

    public void fixNulls() {
        if (cLocUrl == null) {
            cLocUrl = "";
        }
        if (cCatURL == null) {
            cCatURL = "";
        }
        if (eCatUrl == null) {
            eCatUrl = "";
        }
        if (fCatUrl == null) {
            fCatUrl = "";
        }
        if (kCatName == null) {
            kCatName = "";
        }
        if (kCatID == null) {
            kCatID = "";
        }
        if (kLocationName == null) {
            kLocationName = "";
        }
        if (kLocationID == null) {
            kLocationID = "";
        }
        if (kURL == null) {
            kURL = "";
        }
        if (eURL == null) {
            eURL = "";
        }
        if (cURL == null) {
            cURL = "";
        }
        if (fURL == null) {
            fURL = "";
        }
        if (kURL_nextPage == null) {
            kURL_nextPage = "";
        }
        if (eURL_nextPage == null) {
            eURL_nextPage = "";
        }
        if (cURL_nextPage == null) {
            cURL_nextPage = "";
        }
        if (customName == null) {
            customName = "";
        }
        if (updateDate == null) {
            updateDate = new GregorianCalendar(1997, Calendar.AUGUST, 24, 0, 0, 0).getTime();
        }
        if (kLatLong == null) {
            kLatLong = "";
        }
    } // temp. 122 update

    public void fixKURL() {
        if (!kURL.isEmpty() && kNameUrl.isEmpty()) {
            kURL = kURL.replace("/k0", "/");
            kURL_nextPage = kURL_nextPage.replace("/k0", "/");
        }
    } // temp. 128 update

    public void addKLatLong() {
        kLatLong = "";
        if (!kURL.isEmpty() && kURL.contains("&address=")) {
            kURL = kURL.replace("&address=" + kURL.substring(kURL.indexOf("&address=") + "&address=".length(), kURL.indexOf("&radius=")), "&ll=");
        }
    } // temp. 130 update

    public void fixKURL_nextPage() {
        if (!kURL_nextPage.isEmpty()) {
            if (!kURL_nextPage.contains("https://")) {
                kURL_nextPage = kURL_nextPage.replaceFirst("htt.*?ps://", "https://");
            }
            if (kURL_nextPage.contains("&address=")) {
                kURL_nextPage = kURL_nextPage.replace("&address=" + kURL_nextPage.substring(kURL_nextPage.indexOf("&address=") + "&address=".length(), kURL_nextPage.indexOf("&radius=")), "&ll=");
                if (!kLatLong.isEmpty()) {
                    kURL_nextPage = kURL_nextPage.replaceFirst("&ll=.*?&radius=", "&ll=" + kLatLong + "&radius=");
                }
            }
        }
    } // temp. 133 update

    public void adjustFURLAgain() {
        if (facebook) {
            fURL = "https://www.facebook.com/marketplace/search/?sortBy=creation_time_descend" + fNameUrl + fCatUrl + fPriceUrl;
        }
    } // temp. 134 update

}
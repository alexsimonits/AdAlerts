package com.simonits.adalerts.objects;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.simonits.adalerts.utils.Utility;

import java.util.Objects;

class AlertClassHelper {

    private transient String kLocationName, kLocationID; // transient so these aren't checked when alert objects are changing/creating/transferring/whatever. It's because the system was getting confused since these variables share same name with Alert class variables
    private transient String kCatName, kCatID;

    String getUrlPrice(String priceName, String from, String to, String fromUrlPart, String toUrlPart) {
        if (priceName.equals("Specific Price")) {
            return fromUrlPart + from + toUrlPart + to;
        } else if (priceName.equals("Free")) {
            return fromUrlPart + "0" + toUrlPart + "0";
        } else {
            return "";
        }
    }

    String createELocation(String eLoc, String eKm, String ePostalZip) {
        if (eLoc.equals("Canada Only")) {
            return "&LH_PrefLoc=1";
        } else if (eLoc.equals("North America")) {
            return "&LH_PrefLoc=3";
        } else {
            return "&LH_PrefLoc=99&_sadis=" + eKm + "&_stpos=" + ePostalZip + "&_fspt=1";
        }
    }

    String createEType(String eType) {
        if (eType.equals("Auction")) {
            return "&LH_Auction=1";
        } else if (eType.equals("Buy It Now")) {
            return "&LH_BIN=1";
        } else { // All Listings
            return "";
        }
    }

    String createKType(String kType) {
        if (kType.equals("Offering")) {
            return "&ad=offering";
        } else if (kType.equals("Wanted")) {
            return "&ad=wanted";
        } else { // All Listings
            return "";
        }
    }

    String createKLocation(String loc, String km) {
        Gson gson = new Gson();
        SiteParam locObj = Objects.requireNonNull(Utility.getObjFromTree(gson.fromJson(FirebaseRemoteConfig.getInstance().getString("kLocations1_17"), SiteParam[].class), loc));
        kLocationName = locObj.getValue1() != null ? locObj.getValue1() : "";
        kLocationID = locObj.getValue2() != null ? locObj.getValue2() : "";
        if (loc.equals("Specific Location")) {
            return "&ll=&radius=" + km + ".0";
        } else {
            return "";
        }
    }

    void createKCat(String cat) {
        Gson gson = new Gson();
        SiteParam obj = Objects.requireNonNull(Utility.getObjFromTree(gson.fromJson(FirebaseRemoteConfig.getInstance().getString("kCategories0_64"), SiteParam[].class), cat));
        kCatName = obj.getValue1() != null ? obj.getValue1() : "";
        kCatID = obj.getValue2() != null ? obj.getValue2() : "";
    }

    String getUrlPart(String param, String key) {
        Gson gson = new Gson();
        SiteParam obj = Objects.requireNonNull(Utility.getObjFromTree(gson.fromJson(FirebaseRemoteConfig.getInstance().getString(key), SiteParam[].class), param));
        return obj.getValue1() != null ? obj.getValue1() : "";
    }

    String getKCatName() {
        return kCatName;
    }

    String getKCatID() {
        return kCatID;
    }

    String getKLocationName() {
        return kLocationName;
    }

    String getKLocationID() {
        return kLocationID;
    }

}

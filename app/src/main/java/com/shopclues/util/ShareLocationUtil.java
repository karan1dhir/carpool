package com.shopclues.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Created by manish on 16/8/17.
 */

public class ShareLocationUtil {

    private ShareLocationUtil() {

    }

    private static class SharePrefrenceHelper {
        private static final ShareLocationUtil INSTANCE = new ShareLocationUtil();
    }

    public static ShareLocationUtil getInstance() {

        return SharePrefrenceHelper.INSTANCE;
    }


    public void shareLocationExplicit(Context context, Location lastLocation) {
//        String link = formatLocation(lastLocation, "googlelocUrl");
        String link = "Visit <a href=\"http://www.google.com\">google</a> for more info.";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, link);
        intent.setType("text/plain");
        context.startActivity(Intent.createChooser(intent, "By Shopclues"));
    }

    private String getLatitude(Location location) {
        return String.format(Locale.US, "%2.5f", location.getLatitude());
    }

    private String getLongitude(Location location) {
        return String.format(Locale.US, "%3.5f", location.getLongitude());
    }

    private String formatLocation(Location location, String format) {
        return MessageFormat.format(format,
                getLatitude(location), getLongitude(location));
    }
}

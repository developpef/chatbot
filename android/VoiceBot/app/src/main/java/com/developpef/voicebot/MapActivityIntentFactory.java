package com.developpef.voicebot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by frup43821 on 29/09/2017.
 */

public class MapActivityIntentFactory {

    public static Intent startGoogleMaps(double lat, double lng) {
        String posStr = lat+","+lng;
        // Creates an Intent that will load a map
        Uri gmmIntentUri = Uri.parse("geo:" + posStr + "?q=" + posStr);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        return mapIntent;
    }

    public static Intent startMapBox(Activity parent, double lat, double lng) {
        Intent intent = new Intent(parent, MapActivity.class);
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        return intent;
    }
}
package com.developpef.voicebot;

import android.app.Activity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Created by frup43821 on 29/09/2017.
 *
 * Activite pour les maps MapBox
 */
public class MapActivity extends Activity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), "pk.eyJ1IjoicGVmIiwiYSI6ImNpZXBuODc1MzAwN2x0dW04NzlvYzdhbW8ifQ.uRNIJR3rb5FtHv2PWJlq3w");

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map);

        final double lat = getIntent().getDoubleExtra("lat",0.0);
        final double lng = getIntent().getDoubleExtra("lng",0.0);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                LatLng position = new LatLng(lat, lng);
                mapboxMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("Ici!")
                );

                //mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(position,15.0));

                CameraPosition camPos = new CameraPosition.Builder()
                        .target(position)
                        .zoom(15.0)
                        .tilt(50)
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos),4000);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

}

package com.developpef.voicebot;

import android.content.Context;
import android.location.Location;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by frup43821 on 26/09/2017.
 */

public class MapView extends FrameLayout {
    private Subject<GoogleMap> mapSubject;
    private View parentView;
    private Location location;

    public MapView(@NonNull Context context, View parentView, Location loc) {
        super(context);
        this.parentView = parentView;
        this.location = loc;
        init(context, null);
    }

    public MapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MapView(@NonNull Context context, @Nullable AttributeSet attrs,
                   @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final SupportMapFragment mapFragment = SupportMapFragment.newInstance();

        if (!isInEditMode()) {
            FragmentTransaction fragmentTransaction =
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(parentView.findViewById(R.id.map).getId(), mapFragment);
            fragmentTransaction.commit();

            mapSubject = BehaviorSubject.create();

            Observable.create(new ObservableOnSubscribe<GoogleMap>() {
                @Override
                public void subscribe(@io.reactivex.annotations.NonNull ObservableEmitter<GoogleMap> e) throws Exception {
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.addMarker(new MarkerOptions()
                                    .position(position));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                        }
                    });
                }
            }).subscribe(mapSubject);
        }
    }
}

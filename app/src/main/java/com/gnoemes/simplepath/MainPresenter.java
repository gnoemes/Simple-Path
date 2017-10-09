package com.gnoemes.simplepath;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

@InjectViewState
public class MainPresenter extends MvpPresenter<MainView> implements DirectionCallback,GoogleMap.OnMapLoadedCallback{

    private static final String API_KEY = "AIzaSyAtk70uPt409PXoHbH3VvvxrtUzYP1pF5U";
    private Context context;
    private LatLng from;
    private LatLng to;
    private PolylineOptions direction;
    private LatLngBounds bounds;


    public MainPresenter(Context context) {
        this.context = context;
    }

    public void showMyLocation() {
        LatLng myLoc = getMyLocation();
        if (myLoc == null) {
            getViewState().showError();
        } else {
            getViewState().showLocation(myLoc);
        }

    }

    public void requestDirection(LatLng to) {
        from = getMyLocation();
        if (from == null) {
            getViewState().showError();
        } else {
            GoogleDirection.withServerKey(API_KEY)
                    .from(from)
                    .to(to)
                    .transitMode(TransportMode.DRIVING)
                    .execute(this);
            this.to = to;
        }
        Log.i("DEVE", "requestDirection: ");
    }


    public void requestDirection(LatLng from, LatLng to) {
        GoogleDirection.withServerKey(API_KEY)
                .from(from)
                .to(to)
                .transitMode(TransportMode.DRIVING)
                .execute(this);
        this.from = from;
        this.to = to;
    }

    private LatLng getMyLocation() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }
        if (myLocation != null) {
           return new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
        }
        return null;
    }

    @Override
    public void onDirectionSuccess(Direction dir, String rawBody) {
        if (dir.isOK()) {
            getViewState().setMarkers(new MarkerOptions().position(from),new MarkerOptions().position(to));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(from);
            builder.include(to);
            bounds = builder.build();
            ArrayList<LatLng> points = dir.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            direction = DirectionConverter.createPolyline(context, points, 5, Color.BLACK);
            getViewState().setDirection(direction, bounds);
        }
        Log.i("DEVE", "onDirectionSuccess: ");
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        t.printStackTrace();
        Log.i("DEVE", "onDirectionFailure: ");
    }


    @Override
    public void onMapLoaded() {
        if (from != null && to != null && bounds != null && direction != null) {
            getViewState().updateLastConfiguration(direction,bounds,new MarkerOptions().position(from),new MarkerOptions().position(to));
        }
        Log.i("DEVE", "onMapLoaded: ");
    }
}

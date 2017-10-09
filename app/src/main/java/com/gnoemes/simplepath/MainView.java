package com.gnoemes.simplepath;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public interface MainView extends MvpView {

    @StateStrategyType(SkipStrategy.class)
    void showLocation(LatLng latLng);
    @StateStrategyType(SkipStrategy.class)
    void setDirection(PolylineOptions direction, LatLngBounds bounds);
    @StateStrategyType(SkipStrategy.class)
    void setMarkers(MarkerOptions... markerOptions);
    @StateStrategyType(SkipStrategy.class)
    void updateLastConfiguration(PolylineOptions direction, LatLngBounds bounds, MarkerOptions... markerOptions);
    @StateStrategyType(SkipStrategy.class)
    void showError();
}

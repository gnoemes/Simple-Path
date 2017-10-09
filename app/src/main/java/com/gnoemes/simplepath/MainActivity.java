package com.gnoemes.simplepath;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends MvpAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, MainView {

    @InjectPresenter
    MainPresenter presenter;

    @ProvidePresenter
    MainPresenter providePresenter() {
        return new MainPresenter(this);
    }

    private PlaceAutocompleteFragment searchFragment;
    private PlaceAutocompleteFragment searchPathFrom;
    private PlaceAutocompleteFragment searchPathTo;
    private GoogleMap mMap;
    private Marker fromMarker;
    private Marker toMarker;
    private LatLng fromLatLng;
    private LatLng toLatLng;
    private Polyline path;
    private int mapType;
    @BindView(R.id.fab_accept)
    FloatingActionButton acceptPathFab;
    @BindView(R.id.fab_find_path)
    FloatingActionButton findPathFab;
    @BindView(R.id.fab_my_location)
    FloatingActionButton myLocationFab;
    @BindView(R.id.path_card)
    CardView pathCard;
    @BindView(R.id.search_card)
    CardView searchCard;
    @BindView(R.id.swap)
    ImageView swapBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initNavigationMenu();
        initFragments();
        initListeners();

        if (savedInstanceState != null) {
            searchCard.setVisibility(savedInstanceState.getInt("searchCard") == View.GONE ? View.GONE: View.VISIBLE);
            pathCard.setVisibility(savedInstanceState.getInt("pathCard") == View.GONE ? View.GONE: View.VISIBLE);
            acceptPathFab.setVisibility(savedInstanceState.getInt("acceptPathFab") == View.GONE ? View.GONE: View.VISIBLE);
            fromLatLng = savedInstanceState.getParcelable("from");
            toLatLng = savedInstanceState.getParcelable("to");
            mapType = savedInstanceState.getInt("mapType",1);
        } else {
            mapType = 1;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("searchCard",searchCard.getVisibility());
        outState.putInt("pathCard",pathCard.getVisibility());
        outState.putInt("acceptPathFab",acceptPathFab.getVisibility());
        outState.putParcelable("from",fromLatLng);
        outState.putParcelable("to",toLatLng);
        outState.putInt("mapType",mMap.getMapType());
    }

    private void initFragments() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        ((View) findViewById(R.id.place_autocomplete_search_button)).setVisibility(View.GONE);
        findViewById(R.id.place_autocomplete_clear_button).setPadding(12,12,12,12);
        searchFragment.setHint("Search");
        searchPathFrom = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment_path_from);
        pathCard.getChildAt(0).findViewById(R.id.linearPath).findViewById(R.id.autocomplete_fragment_path_from).findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        pathCard.getChildAt(0).findViewById(R.id.linearPath).findViewById(R.id.autocomplete_fragment_path_from).findViewById(R.id.place_autocomplete_clear_button).setPadding(12,12,12,12);
        searchPathFrom.setHint("From");
        searchPathTo = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment_path_to);
        pathCard.getChildAt(0).findViewById(R.id.linearPath).findViewById(R.id.autocomplete_fragment_path_to).findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        pathCard.getChildAt(0).findViewById(R.id.linearPath).findViewById(R.id.autocomplete_fragment_path_to).findViewById(R.id.place_autocomplete_clear_button).setPadding(12,12,12,12);
        searchPathTo.setHint("To");
    }


    private void initListeners() {
        myLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               presenter.showMyLocation();
            }
        });

        swapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText fromText =  searchPathFrom.getView().findViewById(R.id.place_autocomplete_search_input);
                EditText toText =  searchPathTo.getView().findViewById(R.id.place_autocomplete_search_input);
                String from = toText.getText().toString();
                String to = fromText.getText().toString();
                Log.i("DEVE", "onClick: " + from +" " + to);
                searchPathFrom.setText(from);
                searchPathTo.setText(to);
                LatLng tmp = fromLatLng;
                fromLatLng = toLatLng;
                toLatLng = tmp;
            }
        });

        findPathFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if (searchCard.getVisibility() == View.VISIBLE) {
                     searchCard.setVisibility(View.GONE);
                     pathCard.setVisibility(View.VISIBLE);
                     acceptPathFab.setVisibility(View.VISIBLE);
                     findPathFab.setImageResource(R.drawable.ic_search_white_24dp);
                 } else {
                     searchCard.setVisibility(View.VISIBLE);
                     pathCard.setVisibility(View.GONE);
                     acceptPathFab.setVisibility(View.GONE);
                     findPathFab.setImageResource(R.drawable.ic_directions_white_24dp);
                 }
            }
        });

        acceptPathFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromLatLng != null && toLatLng != null) {
                    clearPrevPath();
                    presenter.requestDirection(fromLatLng,toLatLng);
                } else  {
                    Snackbar.make(view,"Choose origin and destination",Snackbar.LENGTH_SHORT)
                        .setAction("Action",null).show();
                }
            }
        });

        searchFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                presenter.requestDirection(place.getLatLng());
                clearPrevPath();
            }

            @Override
            public void onError(Status status) {

                Log.e("DEVE", "onError: " + status.getStatusMessage());
            }
        });

        searchPathFrom.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                fromLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {

            }
        });

        searchPathTo.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                toLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_standard:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                break;
             case R.id.nav_satellite:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                break;
             case R.id.nav_relief:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here toMarker request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // toMarker handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMapType(mapType);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMapLoadedCallback(presenter);
    }

    private void initNavigationMenu() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        ImageView menu = (ImageView) findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawer.isDrawerOpen(Gravity.START)) {
                    drawer.openDrawer(Gravity.START);
                } else {
                    drawer.closeDrawer(Gravity.START);
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void showLocation(LatLng location) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,16.5f));
        }
    }

    @Override
    public void setDirection(PolylineOptions direction, LatLngBounds bounds) {
        if (mMap != null) {
            path = mMap.addPolyline(direction);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,130));
        }
        if (mMap != null && mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            final View view = findViewById(R.id.coordinator);
            Snackbar.make(view,"Can't find direction in Satellite mode",Snackbar.LENGTH_LONG)
                    .setAction("Error",null).show();
        }

        Log.i("DEVE", "setDirection: ");
    }

    @Override
    public void setMarkers(MarkerOptions... markerOptions) {
        if (mMap != null) {
            fromMarker = mMap.addMarker(markerOptions[0]);
            toMarker = mMap.addMarker(markerOptions[1]);
        }
        Log.i("DEVE", "setMarkers: ");
    }

    @Override
    public void updateLastConfiguration(PolylineOptions direction, LatLngBounds bounds, MarkerOptions... markerOptions) {
        fromMarker = mMap.addMarker(markerOptions[0]);
        toMarker = mMap.addMarker(markerOptions[1]);
        path = mMap.addPolyline(direction);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,130));
    }

    @Override
    public void showError() {
        final View view = findViewById(R.id.coordinator);
        Snackbar.make(view,"Check connection",Snackbar.LENGTH_LONG)
                .setAction("Error",null).show();
    }

    public void clearPrevPath() {
        if (fromMarker != null && toMarker != null) {
            fromMarker.remove();
            toMarker.remove();
        }
        if (path != null) {
            path.remove();
        }
        Log.i("DEVE", "clearPrevPath: ");
    }

}

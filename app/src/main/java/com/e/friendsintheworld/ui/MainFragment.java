package com.e.friendsintheworld.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.e.friendsintheworld.MainActivity;
import com.e.friendsintheworld.MessageHandler;
import com.e.friendsintheworld.R;
import com.e.friendsintheworld.SharedViewModel;
import com.e.friendsintheworld.controllers.MainController;
import com.e.friendsintheworld.models.Group;
import com.e.friendsintheworld.models.Pin;
import com.e.friendsintheworld.models.RegisteredGroup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MainFragment extends Fragment {
    private MainController controller;
    private MapView mMapView;
    private SharedViewModel sharedViewModel;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng currentLatLng;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        setRetainInstance(true);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        registerListeners();

        setMap();

        return view;
    }

    private void registerListeners() {
        sharedViewModel.getLocationsUpdated().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                Log.v("observe", "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
                googleMap.clear();

                ArrayList<RegisteredGroup> registeredGroupsArr = sharedViewModel.getRegisteredGroups().getValue();
                ArrayList<MarkerOptions> markerOptionsArr = new ArrayList<>();
                if (registeredGroupsArr != null){
                    for (int i=0 ; i<registeredGroupsArr.size(); i++){
                        if (registeredGroupsArr.get(i).isShowOnMap()){

                            ArrayList<Pin> pinsArr =  registeredGroupsArr.get(i).getMembersLocations();
                            if (pinsArr != null) {

                                for (int j=0 ; j<pinsArr.size(); j++){
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(pinsArr.get(j).getPosition());
                                    markerOptions.title(pinsArr.get(j).getUsername());
                                    markerOptionsArr.add(markerOptions);
                                    googleMap.addMarker(markerOptions);
                                }
                            }

                        }

                    }
                }

                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                    }
                });
            }
        });
    }

    public void setController(MainController controller){
        this.controller = controller;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    public void setMap(){

        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MainController.REQUEST_ACCESS_FINE_LOCATION);
                    return;
                } else {
                    mMap.setMyLocationEnabled(true);
                    Log.v("aaaaaaaaaaaaaa" , "elseeeeeeeeeeeeeeeeeeeeeee");

                }
                googleMap = mMap;
                sharedViewModel.setLocationsUpdated(System.currentTimeMillis());
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                Location loc =  locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(),true));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 4);
                googleMap.animateCamera(cameraUpdate);
            }
        });
    }
}
package com.e.friendsintheworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.e.friendsintheworld.controllers.MainController;
import com.e.friendsintheworld.ui.GroupsFragment;
import com.e.friendsintheworld.ui.MainFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private MainController mainController;
    private int currentOrientation;
    private OrientationEventListener listener;
    private String language;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (savedInstanceState == null) {
            mainController = new MainController(this, false);
            setFragment(mainController.getMainFragment(), false);
        } else{
            //if (currentOrientation != getResources().getConfiguration().orientation){
            this.language = savedInstanceState.getString("language");
            Locale myLocale = new Locale(this.language);
            Resources res = getResources();
            DisplayMetrics dm = getResources().getDisplayMetrics();
            Configuration conf = getResources().getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            onConfigurationChanged(conf);
            mainController = new MainController(this, true);

            //Log.v("ifffffffffffffff", "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            //}

        }



    }

    protected void onDestroy() {
        /*if (currentOrientation == getResources().getConfiguration().orientation){
            Log.v("befooooooooooooore if", String.valueOf(currentOrientation));
            mainController.notRotation();
        } else{
            Log.v("befooooooooooooore else", String.valueOf(currentOrientation));
            mainController.rotation();
        }*/
        mainController.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //currentOrientation = getResources().getConfiguration().orientation;
        super.onPause();
    }

    public void setFragment(Fragment fragment, boolean backStack) {
        if (!isOnline()) {
            Toast.makeText(getApplicationContext(), "You are offline!! \n Please check your internet connection!!", Toast.LENGTH_LONG).show();
        }

        if (backStack) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.addToBackStack(null).commit();
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();

            //mMapView = getSupportFragmentManager().findFragmentById(R.id.fragment_container).getView().findViewById(R.id.mapView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btnGroups) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!(fragment instanceof GroupsFragment)){
                mainController.showGroupsFragment();
            }
        } else if (id == R.id.btnLanguage){
            View popupMap = findViewById(R.id.btnGroups);
            PopupMenu popupMenu = new PopupMenu(this, popupMap);
            popupMenu.inflate(R.menu.menu_languages);
            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.btn_english){
                        changeLanguage("en");
                    }
                    else {
                        changeLanguage("sv");
                    }

                    return true;
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeLanguage (String language) {
        this.language = language;
        Locale myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Configuration conf = getResources().getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        onConfigurationChanged(conf);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof MainFragment){
            setFragment(mainController.getMainFragment(), false);
        }else if (fragment instanceof GroupsFragment){
            getSupportFragmentManager().popBackStack();
            mainController.showGroupsFragment();
        }
        //getSupportFragmentManager().popBackStack();

       /* Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //currentOrientation = getResources().getConfiguration().orientation;
        if (this.language == null){
            this.language = "en";
        }
        Log.v("laaaaaaaaaaaaaang", language);

        outState.putString("language",this.language);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v("RequePermisionsResult" , String.valueOf(requestCode));
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MainController.REQUEST_ACCESS_FINE_LOCATION);
        } else{
            mainController.getCurrentLocation(requestCode);

            mainController.setMap();

        }
    }

    public boolean isOnline() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }
}
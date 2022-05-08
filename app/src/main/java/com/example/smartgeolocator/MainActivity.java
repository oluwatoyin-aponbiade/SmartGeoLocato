package com.example.smartgeolocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_update, tv_address, tv_countOfCrumbs;
    Switch sw_locationupdates, sw_gps;
    Button btn_newWayPoint,btn_showWayPointList, btn_showMap,sign_out;

    boolean updateOn = false;


    // current location
 Location currentLocation;
    // list of saved locations
    List<Location>savedLocations;


    // Location request is a config file for all settings related to fusedlocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    //google's API for location services. Majority of the function in this app depend on this class.
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // give UI variable a value

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_update = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        btn_showWayPointList= findViewById(R.id.btn_showWayPointList);
        btn_newWayPoint=findViewById(R.id.btn_newWayPoint);
        tv_countOfCrumbs = findViewById(R.id.tv_countOfCrumbs);
        btn_showMap= findViewById(R.id.btn_showMap);
        sign_out= findViewById(R.id.btn_signout);

        // set all properties of locationRequest

        locationRequest = new LocationRequest();
        // how often does the default location check occur?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        //how often does the location check occur when set to the most frequent update?
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // this event is triggered whenever the interval is met.
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // save the location
                Location location= locationResult.getLastLocation();
                updateUIValues(location);

            }
        };
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Profile.class);
                startActivity(i);
            }
        });
        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the gps location

                //add the new location to the global list
                ToyinApplication toyinApplication = (ToyinApplication)getApplicationContext();
                savedLocations = toyinApplication.getMyLocations();
                savedLocations.add(currentLocation);
            }
        });
        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, showSavedLocationList.class);
                startActivity(i);
            }
        });

       btn_showMap.setOnClickListener(new View.OnClickListener() {
          @Override
           public void onClick(View view) {
              Intent i = new Intent(MainActivity.this, MapsActivity.class);
              startActivity(i);
           }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    // most accurate - use GPS
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Tower + WIFI");

                }
            }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationupdates.isChecked()) {
                    // turn on location tracking
                    startLocationUpdates();
                } else {
                    // turn off location tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();

    }// end onCreate Method

    private void stopLocationUpdates() {
        tv_update.setText("Location is being Tracked");
        tv_lat.setText("NOT tracking Location");
        tv_lon.setText("NOT tracking Location");
        tv_speed.setText("NOT tracking Location");
        tv_address.setText("NOT tracking Location");
        tv_accuracy.setText("NOT tracking Location");
        tv_altitude.setText("NOT tracking Location");
        tv_sensor.setText("NOT tracking Location");
fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    private void startLocationUpdates() {
        tv_update.setText("Location is NOT being Tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode){
        case PERMISSIONS_FINE_LOCATION:
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }
            else{
                Toast.makeText(this, "this APP requires permission to be granted in order to work Properly", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
    }
    }

    private void updateGPS(){
        //get permissions from the user to track GPS
        //get the current location fro the fused client
        //update the UI - i.e. set all properties in their associated text view items.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            // user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // we got permissions. put the values of location. xx into the UI components.
                    updateUIValues(location);
                    currentLocation=location;

                }
            });

        }
        else{
            // permission not granted yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        // update all TextView object with the new location
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        // check if device can return or calculate altitude
        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_altitude.setText("Not Available");

        }
        // check if device can return or calculate  speed
        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else {
            tv_speed.setText("Not Available");

        }
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_address.setText("Unable to retrieve your Street Address");
        }

        ToyinApplication toyinApplication = (ToyinApplication)getApplicationContext();
        savedLocations = toyinApplication.getMyLocations();
        // show the number of way points / crumbs saved
        tv_countOfCrumbs.setText(Integer.toString(savedLocations.size()));


    }
}
package com.example.famakindaniel7.locationtracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;

import static com.example.famakindaniel7.locationtracking.R.id.map;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "MapsActivity";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    private double latitudeValue = 0.0;
    private double longitudeValue = 0.0;
    private GoogleMap mMap;

    private ArrayList<LatLng> points;
    Polyline polyline;
    private final static String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        //mMap.setMyLocationEnabled(true);
        //Button track = (Button) findViewById(R.id.start_tracking);
        //track.setOnClickListener(this);


        //mMap.setMyLocationEnabled(true);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLocationRequest = createLocationRequest();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            });
        }
    }

    private void markStartingLocationOnMap(GoogleMap mapObject, LatLng location){
        mapObject.addMarker(new MarkerOptions().position(location).title("Current location"));
        mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        //Log.d(TAG, "Connection method has been called");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            if (mLastLocation != null) {
                                latitudeValue = mLastLocation.getLatitude();
                                longitudeValue = mLastLocation.getLongitude();
                                //Log.d(TAG, "Latitude 4: " + latitudeValue + " Longitude 4: " + longitudeValue);
                                refreshMap(mMap);
                                markStartingLocationOnMap(mMap, new LatLng(latitudeValue, longitudeValue));
                                startPolyline(mMap, new LatLng(latitudeValue, longitudeValue));
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

        /*if (mCurrentLocation != null) {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            //Location location = mGoogleApiClient.getLastLocation();
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            points.add(latLng);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            mMap.animateCamera(cameraUpdate);
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //MapDemoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        //noinspection MissingPermission
        mMap.setMyLocationEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        //noinspection MissingPermission
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }
    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude); //you already have this

        points.add(latLng); //added

        redrawLine(); //added
    }

    //@Override
    //public void onClick(View view) {}
    private void redrawLine(){

        mMap.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        //addMarker(); //add Marker in current position
        polyline = mMap.addPolyline(options); //add Polyline
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
         mMap.addMarker(new MarkerOptions().position(currentLatLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
    }
    //private void addMarker(){
       // MarkerOptions options = new MarkerOptions();
       // LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
       // options.position(currentLatLng);
       // Marker mapMarker = mMap.addMarker(options);
    //}

    private void startPolyline(GoogleMap map, LatLng location) {
        if (map == null) {
            Log.d(TAG, "Map object is not null");
            return;
        }
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.add(location);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(0).latitude, positions.get(0).longitude))
                .zoom(17)
                .bearing(90)
                .tilt(40)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();
    }


    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(routeReceiver);
    }
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}

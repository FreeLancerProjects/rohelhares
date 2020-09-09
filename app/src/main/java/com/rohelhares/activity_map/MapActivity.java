package com.rohelhares.activity_map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rohelhares.R;
import com.rohelhares.model.PlaceDirectionModel;
import com.rohelhares.remote.Api;
import com.rohelhares.databinding.ActivityMapBinding;
import com.rohelhares.databinding.DialogCustomBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private ActivityMapBinding binding;
    private String lang;
    private HashMap<Integer, List<Double>> markerlist;
    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 50.0f;
    private int count = 0;
    private boolean aceept = false;
    private int pos = -1;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private double lat = 0.0, lng = 0.0;
    private List<List<Double>> lists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map);
        updateUI();
        initView();
        CheckPermission();

    }

    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{fineLocPerm}, loc_req);
        } else {

            initGoogleApi();
        }
    }

    private void initGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    private void initView() {
        lists = new ArrayList<>();
        markerlist = new HashMap<>();
        Paper.init(this);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        binding.setLang(lang);
        binding.fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateDialogAlert(MapActivity.this);
            }
        });
    }

    private void updateUI() {

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setTrafficEnabled(true);
            mMap.setBuildingsEnabled(true);
            mMap.setIndoorEnabled(true);
            // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));

            mMap.setOnMapClickListener(latLng -> {
                Log.e(";;;;", count + "");
                if (count > 0) {
                    count -= 1;
                    List<Double> list;
                    if (pos != -1 && markerlist.get(pos) != null) {
                        list = markerlist.get(pos);
                        Log.e(";llfllfll", ";l;l;;");
                    } else {
                        list = new ArrayList<>();
                    }
                    list.add(latLng.latitude);
                    list.add(latLng.longitude);
                    if (pos != -1 && markerlist.get(pos) != null) {
                        markerlist.remove(pos);
                        markerlist.put(pos, list);
                    } else {
                        markerlist.put(markerlist.size(), list);
                    }
                    if (count == 0) {
                        aceept = false;
                        pos = -1;
                        updateDataMapUI();
                    } else {
                        pos = markerlist.size() - 1;
                        Log.e(";lldl", pos + "");
                    }
                }

            });
        }

    }


    private void updateDataMapUI() {
        for (int key : markerlist.keySet()) {
            addMarker(markerlist.get(key));


        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
    }

    private void initLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(60000);
        LocationSettingsRequest.Builder request = new LocationSettingsRequest.Builder();
        request.addLocationRequest(locationRequest);
        request.setAlwaysShow(false);


        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startLocationUpdate();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MapActivity.this, 100);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;

                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        if(lists.size()>0){
            for(int i=0;i<lists.size();i++){
                List<Double> doubleList=lists.get(i);
                for(int j=0;j<doubleList.size();j+=2){
                    if(lat==doubleList.get(j)&&lng==doubleList.get(j+1)){
                        Toast.makeText(MapActivity.this,"you go"+doubleList.get(j)+" "+doubleList.get(j+1),Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));

        if (googleApiClient != null) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null) {
            if (locationCallback != null) {
                LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
                googleApiClient.disconnect();
                googleApiClient = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initGoogleApi();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            startLocationUpdate();
        }

    }


    private void addMarker(List<Double> branchs) {
        Log.e("ldlld", branchs.get(0) + "");
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentPadding(15, 15, 15, 15);
        // iconGenerator.setTextAppearance(R.style.iconGenText);
        // iconGenerator.setBackground(ContextCompat.getDrawable(activity, android:R.drawable.ic_map));
        // iconGenerator.setColor(R.color.black);

        Marker marker;
        Marker marker1;
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(0), branchs.get(1))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        marker1 = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(2), branchs.get(3))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // getDirection(branchs.get(0) + "", branchs.get(1) + "", branchs.get(2) + "", branchs.get(3) + "");
        drawRoute(branchs.get(0), branchs.get(1), branchs.get(2), branchs.get(3));

    }

    private void getDirection(String client_lat, String client_lng, String driver_lat, String driver_lng) {
        Log.e("sssss", client_lat + " " + client_lng + " " + driver_lat + " " + driver_lng);

        String origin = "", dest = "";
        origin = client_lat + "," + client_lng;
        dest = driver_lat + "," + driver_lng;
        Log.e("ldlldl", origin + " " + dest);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .getDirection(origin, dest, "rail", getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDirectionModel>() {
                    @Override
                    public void onResponse(Call<PlaceDirectionModel> call, Response<PlaceDirectionModel> response) {
                        if (response.body() != null && response.body().getRoutes().size() > 0) {
                            Log.e("ldldkdkkd", response.body().getRoutes().get(0).getOverview_polyline().getPoints());
                            //  drawRoute(PolyUtil.decode(response.body().getRoutes().get(0).getOverview_polyline().getPoints()));

                        } else {
                            Log.e("sssss", "ldlldldldl" + "");
                        }
                    }

                    @Override
                    public void onFailure(Call<PlaceDirectionModel> call, Throwable t) {
                        try {
                        } catch (Exception e) {
                        }
                    }
                });

    }

    private void drawRoute(double from_latitude, double from_longitude, double to_latitude, double to_longitude) {
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        PolylineOptions polyLineOptions = new PolylineOptions();
        points.add(new LatLng(from_latitude, from_longitude));
        points.add(new LatLng(to_latitude, to_longitude));
        polyLineOptions.width(7 * 1);
        polyLineOptions.geodesic(true);
        polyLineOptions.color(this.getResources().getColor(R.color.colorPrimary));
        polyLineOptions.addAll(points);
        Polyline polyline = mMap.addPolyline(polyLineOptions);
        polyline.setGeodesic(true);
        List<Double> doubleList = new ArrayList<>();
        for (double i = from_latitude; i < to_latitude; i += .1) {
            for (double j = from_longitude; j < to_longitude; j += .1) {
                doubleList.add(i);
                doubleList.add(j);
                Log.e("lsllsl",i+" "+j);
            }
        }
        if(from_latitude==to_latitude&&from_longitude==to_longitude){
            doubleList.add(from_latitude);
            doubleList.add(from_longitude);
            doubleList.add(to_latitude);
            doubleList.add(to_longitude);
        }
        lists.add(doubleList);

    }

    public void CreateDialogAlert(Context context) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();

        DialogCustomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_custom, null, false);

        binding.tvgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 2;
                aceept = true;

                dialog.dismiss();
            }

        });
        binding.tvreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 2;
                aceept = true;

                dialog.dismiss();
            }
        });
        binding.tvgoreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 2;
                aceept = true;

                dialog.dismiss();

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }
}

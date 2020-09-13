package com.rohelhares.activity_map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
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
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.room.Room;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rohelhares.R;
import com.rohelhares.Room_Database.AddGeo;
import com.rohelhares.Room_Database.My_Database;
import com.rohelhares.databinding.DialogMessageBinding;
import com.rohelhares.model.PlaceDirectionModel;
import com.rohelhares.remote.Api;
import com.rohelhares.databinding.ActivityMapBinding;
import com.rohelhares.databinding.DialogCustomBinding;
import com.rohelhares.share.Common;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    private ActivityMapBinding binding;
    private String lang;
    private HashMap<Integer, List<Double>> markerlist;
    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 50.0f;
    private int count = 0;
    private boolean aceept = false;
    private int pos = -1;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final int READ_REQ = 1;

    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private double lat = 0.0, lng = 0.0;
    private List<List<Double>> lists;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String gps_perm = Manifest.permission.ACCESS_FINE_LOCATION;
    public Location location;
    private List<String> title;
    private List<String> content;
    public static My_Database my_database;
    private List<AddGeo> addgeo;
    private Uri uri;
    private List<File> files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map);
        updateUI();
        initView();
        CheckPermission();

    }

    public void checkReadPermission() {
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, READ_REQ);
        } else {
            SelectImage(READ_REQ);
        }
    }

    private void SelectImage(int req) {

        Intent intent = new Intent();

        if (req == READ_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);

            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("audio/*");
            startActivityForResult(intent, req);

        }
    }

    private void initView() {
        title = new ArrayList<>();
        content = new ArrayList<>();
        files = new ArrayList<>();
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
        my_database = Room.databaseBuilder(getApplicationContext(), My_Database.class, "geodb").allowMainThreadQueries().build();
        addgeo = this.my_database.myDoe().getgeo();
        for (int i = 0; i < addgeo.size(); i++) {
            content.add(addgeo.get(i).getContent());
            title.add(addgeo.get(i).getTitle());
            File file = new File(addgeo.get(i).getSound());
            Log.e("llll",file.getPath());
            files.add(file);
            files.add(file);
            ArrayList<Double> listfogeo = new ArrayList<>();
            listfogeo.add(addgeo.get(i).getFrom_lat());
            listfogeo.add(addgeo.get(i).getFrom_lng());
            listfogeo.add(addgeo.get(i).getTo_lat());
            listfogeo.add(addgeo.get(i).getTo_lng());
            markerlist.put(i, listfogeo);

        }

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
            // mMap.setMyLocationEnabled(true);
            // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
            if (count == 0) {
                if (markerlist.size() > 0) {
                    updateDataMapUI();
                }
            }
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
                        AddGeo addGeo = new AddGeo();
                        addGeo.setContent(content.get(content.size() - 1));
                        addGeo.setTitle(title.get(title.size() - 1));
                        List<Double> list1 = markerlist.get(markerlist.size() - 1);
                        addGeo.setFrom_lat(list1.get(0));
                        addGeo.setFrom_lng(list1.get(1));
                        addGeo.setTo_lat(list1.get(2));
                        addGeo.setTo_lng(list1.get(3));
                        addGeo.setSound(files.get(files.size() - 1).getPath());
                        Log.e("lflflfl", files.get(files.size() - 1).getPath());
                        this.my_database.myDoe().add_geo(addGeo);

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

        lists.clear();
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        for (int key : markerlist.keySet()) {
            addMarker(markerlist.get(key));


        }


    }


    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        updateDataMapUI();

        if (lists.size() > 0) {
            //   Toast.makeText(MapActivity.this,lists.get(0).size()+"",Toast.LENGTH_LONG).show();
            for (int i = 0; i < lists.size(); i++) {
                List<Double> doubleList = lists.get(i);
                for (int j = 0; j < doubleList.size(); j += 2) {
                    //     Toast.makeText(MapActivity.this, "" + doubleList.get(j) + " " + doubleList.get(j + 1) + " " + lat + " " + lng, Toast.LENGTH_LONG).show();

                    if (String.format("%.5g%n", lat).equals(String.format("%.5g%n", doubleList.get(j))) && String.format("%.5g%n", lng).equals(String.format("%.5g%n", doubleList.get(j + 1)))) {
                        //Toast.makeText(MapActivity.this, ";f;;f;f;", Toast.LENGTH_LONG).show();
                        String sound_Path = "android.resource://" + getPackageName() + "/" + R.raw.not;
                        //    Toast.makeText(MapActivity.this, "" + doubleList.get(j) + " " + doubleList.get(j + 1) + " " + lat + " " + lng, Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            String CHANNEL_ID = "my_channel_02";
                            CharSequence CHANNEL_NAME = "my_channel_name";
                            int IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE);
                            channel.setShowBadge(true);
                            channel.setSound(Uri.parse(sound_Path), new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                                    .build()
                            );

                            builder.setChannelId(CHANNEL_ID);
                            builder.setSound(Uri.parse(sound_Path), AudioManager.STREAM_NOTIFICATION);
                            builder.setSmallIcon(R.drawable.ic_notification);


                            builder.setContentTitle(title.get(i));


                            builder.setContentText(content.get(i));


                            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
                            builder.setLargeIcon(bitmap);
                            manager.createNotificationChannel(channel);
                            manager.notify(new Random().nextInt(200), builder.build());
                        } else {
                            //  Toast.makeText(MapActivity.this, ";f;;f;f;", Toast.LENGTH_LONG).show();

                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

                            builder.setSound(Uri.parse(sound_Path), AudioManager.STREAM_NOTIFICATION);
                            builder.setSmallIcon(R.drawable.ic_notification);

                            builder.setContentTitle(title.get(i));


                            builder.setContentText(content.get(i));


                            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
                            builder.setLargeIcon(bitmap);
                            manager.notify(new Random().nextInt(200), builder.build());

                        }
                        break;
                    }
                }
            }
        }

    }

//    void getLocation() {
//        try {
//            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
//        } catch (SecurityException e) {
//            Log.e(":slslslsl", e.getLocalizedMessage());
//        }
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initGoogleApiClient();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == READ_REQ) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectImage(requestCode);
            } else {
                // Toast.makeText(this, getString(R.string.per), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQ && resultCode == Activity.RESULT_OK && data != null) {

            uri = data.getData();
            File file = new File(Common.getImagePath(this, uri));
            files.add(file);


        }

    }


    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
//
//            getLocation();
//        }
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, gps_perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{gps_perm}, loc_req);
        } else {

            initGoogleApiClient();

        }
    }

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    private void initLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(1);
        locationRequest.setInterval(1);
        LocationSettingsRequest.Builder request = new LocationSettingsRequest.Builder();
        request.addLocationRequest(locationRequest);
        request.setAlwaysShow(false);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request.build());

        result.setResultCallback(result1 -> {

            Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdate();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(MapActivity.this, 1255);
                    } catch (Exception e) {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e("not available", "not available");
                    break;
            }
        });

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
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
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


    private void addMarker(List<Double> branchs) {
        Log.e("ldlld", branchs.get(0) + "");
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentPadding(15, 15, 15, 15);
        // iconGenerator.setTextAppearance(R.style.iconGenText);
        // iconGenerator.setBackground(ContextCompat.getDrawable(activity, android:R.drawable.ic_map));
        // iconGenerator.setColor(R.color.black);

        Marker marker;
        Marker marker1;
        try {
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(0), branchs.get(1))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            marker1 = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(2), branchs.get(3))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // getDirection(branchs.get(0) + "", branchs.get(1) + "", branchs.get(2) + "", branchs.get(3) + "");
            drawRoute(branchs.get(0), branchs.get(1), branchs.get(2), branchs.get(3));
        } catch (Exception e) {

        }


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
        Log.e("lflfll", from_latitude + " " + to_latitude + " " + from_longitude + " " + to_longitude);
        if (from_latitude < to_latitude && from_longitude < to_longitude) {
            for (double i = from_latitude; i < to_latitude; i += .1) {
                for (double j = from_longitude; j < to_longitude; j += .1) {
                    doubleList.add(i);
                    doubleList.add(j);
                    Log.e("lsllsl", i + " " + j);
                }
            }
        } else if (to_latitude < from_latitude && to_longitude < from_longitude) {
            for (double i = to_latitude; i < from_latitude; i += .1) {
                for (double j = to_longitude; j < from_longitude; j += .1) {
                    doubleList.add(i);
                    doubleList.add(j);
                    Log.e("lsllsl", i + " " + j);
                }
            }
        } else if (to_latitude < from_latitude && from_latitude < to_latitude) {
            for (double i = to_latitude; i < from_latitude; i += .1) {
                for (double j = from_longitude; j < to_longitude; j += .1) {
                    doubleList.add(i);
                    doubleList.add(j);
                    Log.e("lsllsl", i + " " + j);
                }
            }
        } else {
            for (double i = from_latitude; i < to_latitude; i += .1) {
                for (double j = to_longitude; j < from_longitude; j += .1) {
                    doubleList.add(i);
                    doubleList.add(j);
                    Log.e("lsllsl", i + " " + j);
                }
            }
        }

        if (from_latitude == to_latitude && from_longitude == to_longitude) {
            Log.e("lsllsl", fineLocPerm + " " + fineLocPerm);

            doubleList.add(from_latitude);
            doubleList.add(from_longitude);
            doubleList.add(to_latitude);
            doubleList.add(to_longitude);
        }
        lists.add(doubleList);

    }

    public void CreateDialogAlert(Context context) {
        checkReadPermission();
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();

        DialogCustomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_custom, null, false);

        binding.tvgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 2;
                aceept = true;

                dialog.dismiss();
                CreateDialogMesaage(context);
            }

        });
        binding.tvreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 2;
                aceept = true;

                dialog.dismiss();
                CreateDialogMesaage(context);

            }
        });
        binding.tvgoreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 2;
                aceept = true;

                dialog.dismiss();
                CreateDialogMesaage(context);

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    public void CreateDialogMesaage(Context context) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();

        DialogMessageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_message, null, false);

        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.add(binding.edtName.getText().toString() + "");
                content.add(binding.edtContent.getText().toString() + "");
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

}

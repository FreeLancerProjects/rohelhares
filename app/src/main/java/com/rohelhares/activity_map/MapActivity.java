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
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TimePicker;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.rohelhares.adapter.TimesAdapter;
import com.rohelhares.databinding.DialogDisplayBinding;
import com.rohelhares.databinding.DialogMessageBinding;
import com.rohelhares.databinding.DialogShowmessageBinding;
import com.rohelhares.model.PlaceDirectionModel;
import com.rohelhares.model.TimesModel;
import com.rohelhares.remote.Api;
import com.rohelhares.databinding.ActivityMapBinding;
import com.rohelhares.databinding.DialogCustomBinding;
import com.rohelhares.share.Common;
import com.rohelhares.tags.Tags;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private float zoom = 10.0f;
    private int count = 0;
    private boolean aceept = false;
    private int pos = -1;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final int READ_REQ = 1;
    private MediaRecorder recorder;
    private final String write_perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String audio_perm = Manifest.permission.RECORD_AUDIO;
    private final int write_req = 100;
    private boolean isPermissionGranted = false;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private double lat = 0.0, lng = 0.0;
    private List<List<Double>> lists;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public Location location;
    private List<String> title;
    private List<Integer> times;
    private List<Integer> hiddens;
    private List<Integer> hiddenstimer;
    private List<Integer> opens;

    private List<Integer> countsnum;
    private List<String> content;
    private List<String> from;
    private List<String> to;
    private List<String> go;

    public static My_Database my_database;
    private List<AddGeo> addgeo;
    private Uri uri;
    private List<File> files;
    private int countnum = 1;
    private int open;
    private String time = "0";
    private CountDownTimer timer;
    private int hidden;
    private String goes;
    private int hidden2;
    private List<List<LatLng>> allPolygon;
    private List<LatLng> polygonList;
    private boolean canDraw = true;
    private int position;
    private AlertDialog dialog;
    private List<PolygonOptions> polygonOptionsList;
    private boolean zoomL = true;
    private boolean timerShow = true;
    private List<Marker> markers;

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
        markers = new ArrayList<>();
        allPolygon = new ArrayList<>();
        polygonList = new ArrayList<>();
        polygonOptionsList = new ArrayList<>();
        title = new ArrayList<>();
        content = new ArrayList<>();
        times = new ArrayList<>();
        countsnum = new ArrayList<>();
        hiddens = new ArrayList<>();
        hiddenstimer = new ArrayList<>();
        opens = new ArrayList<>();
        from = new ArrayList<>();
        to = new ArrayList<>();
        files = new ArrayList<>();
        lists = new ArrayList<>();
        go = new ArrayList<>();
        markerlist = new HashMap<>();
        Paper.init(this);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        binding.setLang(lang);
        binding.fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 4;
                aceept = true;
                canDraw = true;
            }
        });
        binding.fabclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null) {
                    timer.cancel();
                }
                binding.fr.setVisibility(View.GONE);
                title.clear();
                content.clear();
                times.clear();
                time = "0";
                countsnum.clear();
                hiddens.clear();
                hiddenstimer.clear();
                opens.clear();
                from.clear();
                to.clear();
                files.clear();
                lists.clear();
                go.clear();
                markerlist.clear();
                my_database.myDoe().deleteallorder();
                allPolygon.clear();
                mMap.clear();
                marker = null;
            }
        });
        my_database = Room.databaseBuilder(getApplicationContext(), My_Database.class, "geodb").allowMainThreadQueries().build();
        checkWritePermission();

    }

    private void updateUI() {

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setIndoorEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setTrafficEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

            zoom = mMap.getMaxZoomLevel();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setOnMapClickListener(latLng -> {
                Log.e("dd", "11");

                if (canDraw) {
                    Log.e("dd", "22");

                    if (polygonList.size() >= 4) {
                        // allPolygon.add(polygonList);
                        polygonList = new ArrayList<>();
                        markerlist.clear();

                    }
                    polygonList.add(latLng);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(""));
                    markers.add(marker);

                    if (polygonList.size() == 4) {
                        Log.e("dd", "33");

                        allPolygon.add(polygonList);

                        //drawPolygon();
                        CreateDialogMesaage(this, polygonList);
                        canDraw = false;

                    }
                }

            });
        }

    }

    private void drawPolygon(List<LatLng> polygonList) {
        timerShow=true;
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(polygonList);

        polygonOptions.geodesic(true);
        polygonOptions.fillColor(ContextCompat.getColor(this, R.color.black));
        polygonOptionsList.add(polygonOptions);
        if (mMap != null) {
            mMap.addPolygon(polygonOptions);

        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        if (zoomL) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16.5f));

        }
        zoomL = false;

        mMap.setMyLocationEnabled(true);

         if (isInsideArea(new LatLng(lat, lng)) && times.size() > 0 && times.size() == allPolygon.size()) {
            Log.e("llllnnbbb", lat + "");

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date date = new Date(System.currentTimeMillis());
            try {
                date = formatter.parse(formatter.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date date1 = null;
            try {
                date1 = formatter.parse(from.get(position));
            } catch (ParseException e) {
                //   e.printStackTrace();
            }
            Date date2 = null;
            try {
                date2 = formatter.parse(to.get(position));
            } catch (ParseException e) {
                // e.printStackTrace();
            }
           /* if (files.get(position) != null && files.get(position).getPath() != null && !files.get(position).getPath().equals(null)) {
                initAudio(files.get(position).getPath());
                Log.e("8888",files.get(position).getPath());
            }*/
            Log.e("flflfllsss", date.getTime() + "   " + date1.getTime() + "  " + from.get(position) + "  " + date2.getTime());
            if (opens.get(position) == 1 && date.getTime() >= date1.getTime() && date.getTime() < date2.getTime()&&timerShow) {

                String sound_Path = "android.resource://" + getPackageName() + "/" + R.raw.not;
                //    Toast.makeText(MapActivity.this, "" + doubleList.get(j) + " " + doubleList.get(j + 1) + " " + lat + " " + lng, Toast.LENGTH_LONG).show();

                    Log.e("7777",files.get(position).getPath());

                for (int l = 0; l < countsnum.get(position); l++) {
                    if (files.get(position) != null && files.get(position).getPath() != null && !files.get(position).getPath().equals(null)) {
                        initAudio(files.get(position).getPath());
                        Log.e("8888",files.get(position).getPath());
                    }
                    try {
                        Log.e("mmmm", title.get(position));

                    } catch (Exception e) {

                    }
                    if (!title.get(position).equals("") && !title.get(position).equals(null) && title.get(position) != null && !title.get(position).isEmpty()) {
                        Log.e("mmmm", title.get(position));

                        CreateDialogshowMesaage(this, title.get(position), content.get(position));

                    }
                }

            } else if (opens.get(position) != 1&&timerShow) {
                if (times.get(position) > 0) {
                        startTimer(times.get(position), position);

                } else {
                    if (!title.get(position).equals("") && !title.get(position).equals(null) && title.get(position) != null && !title.get(position).isEmpty()) {

                        CreateDialogshowMesaage(MapActivity.this, title.get(position), content.get(position));
                    }
                }
            }


        }


    }

    public void back() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;

        }

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
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

        } else if (requestCode == write_req && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true;
        }
    }

    private void startTimer(int time, int i) {
        timerShow=false;

        timer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long l) {
                SimpleDateFormat format = new SimpleDateFormat("ss", Locale.ENGLISH);
                String time = format.format(new Date(l));
                try {
                    if (hiddenstimer.get(i) == 0) {
                        binding.fr.setVisibility(View.VISIBLE);
                        binding.tv1.setText(time);
                    }
                }catch (Exception e){

                }

                //  Toast.makeText(MapActivity.this,time,Toast.LENGTH_LONG).show();
            }


            @Override
            public void onFinish() {
                binding.tv1.setText("0");
                binding.fr.setVisibility(View.GONE);
                String sound_Path = "android.resource://" + getPackageName() + "/" + R.raw.not;
                //    Toast.makeText(MapActivity.this, "" + doubleList.get(j) + " " + doubleList.get(j + 1) + " " + lat + " " + lng, Toast.LENGTH_LONG).show();
                if (countsnum.size() > 0) {
                    for (int l = 0; l < countsnum.get(i); l++) {
                        if (files.size() > 0 && files.get(position) != null && files.get(position).getPath() != null && !files.get(position).getPath().equals(null)) {
                            initAudio(files.get(position).getPath());
                            //  CreateDialogDisplay(MapActivity.this, files.get(i).getPath());
                        }
                        if (!title.get(position).equals("") && !title.get(position).equals(null) && title.get(position) != null && !title.get(position).isEmpty()) {
                            CreateDialogshowMesaage(MapActivity.this, title.get(i), content.get(i));
                        }
                    }
                }
            }
        };

        timer.start();

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

    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{fineLocPerm}, loc_req);
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
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


    private void addMarker(List<Double> branchs, int hidden) {
        Log.e("ldlldssss", hidden + "");
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentPadding(15, 15, 15, 15);
        Marker marker;
        Marker marker1;
        try {
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(0), branchs.get(1))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            marker1 = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(2), branchs.get(3))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            drawRoute(branchs.get(0), branchs.get(1), branchs.get(2), branchs.get(3), hidden);
        } catch (Exception e) {

        }


    }

    private void drawRoute(double from_latitude, double from_longitude, double to_latitude, double to_longitude, int hidden) {
        if (hidden == 0) {
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
        }
    }

    private boolean isInsideArea(LatLng latLng) {

        if (allPolygon.size() > 0) {
            for (List<LatLng> latLngs : allPolygon) {
                if (PolyUtil.containsLocation(latLng, latLngs, true)) {
                    position = allPolygon.indexOf(latLngs);
                    return true;
                }
            }
        }

        return false;
    }

    public void CreateDialogAlert(Context context, List<LatLng> polygonList) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();

        DialogCustomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_custom, null, false);

        binding.cardgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.cardgo.setBackgroundColor(getResources().getColor(R.color.second));
                binding.cardgoreturn.setBackgroundColor(getResources().getColor(R.color.white));
                binding.cardreturn.setBackgroundColor(getResources().getColor(R.color.white));
                binding.image1.setColorFilter(R.color.white, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image2.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image3.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image4.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.tv1.setTextColor(getResources().getColor(R.color.white));
                binding.tv2.setTextColor(getResources().getColor(R.color.second));
                binding.tv3.setTextColor(getResources().getColor(R.color.second));
                goes = "go";
                //   dialog.dismiss();
            }

        });
        binding.cardreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.cardreturn.setBackgroundColor(getResources().getColor(R.color.second));
                binding.cardgoreturn.setBackgroundColor(getResources().getColor(R.color.white));
                binding.cardgo.setBackgroundColor(getResources().getColor(R.color.white));
                binding.image2.setColorFilter(R.color.white, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image1.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image3.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image4.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.tv2.setTextColor(getResources().getColor(R.color.white));
                binding.tv1.setTextColor(getResources().getColor(R.color.second));
                binding.tv3.setTextColor(getResources().getColor(R.color.second));
                // dialog.dismiss();
                goes = "return";

            }
        });
        binding.cardgoreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.cardgoreturn.setBackgroundColor(getResources().getColor(R.color.second));
                binding.cardgo.setBackgroundColor(getResources().getColor(R.color.white));
                binding.cardreturn.setBackgroundColor(getResources().getColor(R.color.white));
                binding.image3.setColorFilter(R.color.white, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image2.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image1.setColorFilter(R.color.second, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.image4.setColorFilter(R.color.white, android.graphics.PorterDuff.Mode.MULTIPLY);
                binding.tv3.setTextColor(getResources().getColor(R.color.white));
                binding.tv2.setTextColor(getResources().getColor(R.color.second));
                binding.tv1.setTextColor(getResources().getColor(R.color.second));
                //dialog.dismiss();
                goes = "goreturn";

            }
        });
        binding.cardViewclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateDialogMesaage(context, polygonList);
                dialog.dismiss();

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    public void CreateDialogMesaage(Context context, List<LatLng> polygonList) {
        TimesAdapter timesAdapter;
        List<TimesModel> timesModels = new ArrayList<>();
        countnum = 1;
        hidden = 0;
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();
        DialogMessageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_message, null, false);
        timesModels.add(new TimesModel("1"));
        timesModels.add(new TimesModel("2"));
        timesModels.add(new TimesModel("3"));
        timesModels.add(new TimesModel("4"));
        timesModels.add(new TimesModel("5"));
        timesModels.add(new TimesModel("6"));
        timesModels.add(new TimesModel("7"));
        timesModels.add(new TimesModel("15"));

        timesAdapter = new TimesAdapter(timesModels, context);

        binding.recViewtime.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, true));
        binding.recViewtime.setAdapter(timesAdapter);
        binding.rdshow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hidden = 0;
                }
            }
        });
        binding.rdhiddeen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hidden = 1;


                }
            }
        });
        binding.rdtshow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hidden2 = 0;
                }
            }
        });
        binding.rdthiddeen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hidden2 = 1;
                }
            }
        });
        binding.rdseconed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.llseconed.setVisibility(View.VISIBLE);
                    binding.edtnum.setVisibility(View.VISIBLE);
                    binding.llll.setVisibility(View.GONE);
                    binding.tv4.setVisibility(View.GONE);
                    open = 0;

                }
            }
        });
        binding.rdhour.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.llseconed.setVisibility(View.GONE);
                    binding.edtnum.setVisibility(View.GONE);
                    binding.llll.setVisibility(View.VISIBLE);
                    binding.tv4.setVisibility(View.VISIBLE);
                    open = 1;
                }
            }
        });
        binding.calenderfrom.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                if (Build.VERSION.SDK_INT < 23) {
                    binding.calenderto.setCurrentHour(binding.calenderfrom.getCurrentHour());
                    binding.calenderto.setCurrentMinute(binding.calenderfrom.getCurrentMinute() + 1);
                } else {
                    binding.calenderto.setHour(binding.calenderfrom.getHour());
                    binding.calenderto.setMinute(binding.calenderfrom.getMinute() + 1);

                }
            }
        });
        binding.calenderto.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                if (Build.VERSION.SDK_INT < 23) {
                    if (binding.calenderfrom.getCurrentHour() > binding.calenderto.getCurrentHour()) {
                        binding.calenderto.setCurrentHour(binding.calenderfrom.getCurrentHour());

                    }
                    if (binding.calenderfrom.getCurrentHour() == binding.calenderto.getCurrentHour() && binding.calenderfrom.getCurrentMinute() > binding.calenderto.getCurrentMinute()) {
                        binding.calenderto.setCurrentMinute(binding.calenderfrom.getCurrentMinute() + 1);
                    }

                } else {
                    if (binding.calenderfrom.getHour() > binding.calenderto.getHour()) {

                        binding.calenderto.setHour(binding.calenderfrom.getHour());
                    }
                    if (binding.calenderfrom.getHour() == binding.calenderto.getHour() && binding.calenderfrom.getMinute() > binding.calenderto.getMinute()) {

                        binding.calenderto.setMinute(binding.calenderfrom.getMinute() + 1);
                    }

                }
            }
        });
        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.rdhiddeen.isChecked()) {

                    for (Marker marker : markers) {
                        marker.remove();
                    }

                   /* hidden = 1;
                    polygonOptionsList.get(polygonOptionsList.size() - 1).visible(false);
*/
                } else {
                    drawPolygon(polygonList);
                    // hidden=0;
                }
                title.add(binding.edtName.getText().toString() + "");
                content.add(binding.edtContent.getText().toString() + "");
                hiddens.add(hidden);
                hiddenstimer.add(hidden2);
                if (files.size() < hiddens.size()) {
                    files.add(null);
                }
                if (binding.rdhour.isChecked()) {
                    open = 1;
                } else {
                    open = 0;
                }
                opens.add(open);
                if (hidden ==  1) {

                }
                go.add(goes);
                try {
                    time = binding.edtnum.getText().toString();

                } catch (Exception e) {
                    time = "0";
                }
                if (time.isEmpty()) {
                    time = "0";
                }
                if (Build.VERSION.SDK_INT < 23) {

                    from.add(binding.calenderfrom.getCurrentHour() + ":" + binding.calenderfrom.getCurrentMinute());
                    to.add(binding.calenderto.getCurrentHour() + ":" + binding.calenderto.getCurrentMinute());

                } else {
                    from.add(binding.calenderfrom.getHour() + ":" + binding.calenderfrom.getMinute());
                    to.add(binding.calenderto.getHour() + ":" + binding.calenderto.getMinute());

                }
                times.add(Integer.parseInt(time));
                countsnum.add(countnum);
                dialog.dismiss();
            }
        });
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b) {


                    mediaPlayer.seekTo(i);


                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.imagePlay.setOnClickListener(view -> {

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
                mediaPlayer.pause();
                binding.imagePlay.setImageResource(R.drawable.ic_play);

            } else {

                if (mediaPlayer != null) {
                    binding.imagePlay.setImageResource(R.drawable.ic_pause);

                    mediaPlayer.start();
                    updateProgress(binding);


                }
            }

        });
        binding.imgIncrease2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countnum += 1;
                binding.tvCounter.setText(countnum + "");
            }
        });
        binding.imgDecrease2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countnum > 1) {
                    countnum -= 1;
                    binding.tvCounter.setText(countnum + "");
                }
            }
        });
        binding.cardaudiorecord.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (files.size() > title.size()) {
                    files.remove(files.size() - 1);
                }

                if (isPermissionGranted) {
                    if (recorder != null) {
                        recorder.release();
                        recorder = null;

                    }
                    initRecorder(binding);
                } else {
                    Toast.makeText(this, "Cannot access mic", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (isPermissionGranted) {

                    try {
                        recorder.stop();
                        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                        mediaPlayer = null;
                        initAudio(binding);

                    } catch (Exception e) {
                    }


                } else {
                    Toast.makeText(this, "Cannot access mic", Toast.LENGTH_SHORT).show();
                }


            }


            return true;
        }
        );
        binding.cardaudioselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (files.size() > title.size()) {
                    files.remove(files.size() - 1);
                }
                checkReadPermission();
            }
        });
        binding.cardViewclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    public void CreateDialogshowMesaage(Context context, String title, String Contentdata) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new AlertDialog.Builder(context)
                .create();
        DialogShowmessageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_showmessage, null, false);

        binding.edtName.setText(title);
        binding.edtContent.setText(Contentdata);

        binding.cardViewclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    public void CreateDialogDisplay(Context context, String path) {
        if (path != null && !path.equals(null)) {
            final AlertDialog dialog = new AlertDialog.Builder(context)
                    .create();
            DialogDisplayBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_display, null, false);
            initAudio(path);
            binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (mediaPlayer != null && b) {


                        mediaPlayer.seekTo(i);


                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            binding.imagePlay.setOnClickListener(view -> {

                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
                    mediaPlayer.pause();
                    binding.imagePlay.setImageResource(R.drawable.ic_play);

                } else {

                    if (mediaPlayer != null) {
                        binding.imagePlay.setImageResource(R.drawable.ic_pause);

                        mediaPlayer.start();
                        updateProgress(binding);


                    }
                }

            });
            binding.cardViewclose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.setView(binding.getRoot());
            dialog.show();
        }
    }

    private void initRecorder(DialogMessageBinding binding) {

        Calendar calendar = Calendar.getInstance();
        binding.cardViewaudio.setVisibility(View.GONE);
        isPermissionGranted = true;
        String audioName = "AUD" + calendar.getTimeInMillis() + ".mp3";

        File folder_done = new File(Tags.local_folder_path);

        if (!folder_done.exists()) {
            folder_done.mkdir();
        }

        String path = folder_done.getAbsolutePath() + "/" + audioName;


        recorder = new MediaRecorder();
        File file = new File(path);
        files.add(file);
        Log.e(";;;;;;", files.size() + "");

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        recorder.setOutputFile(path);
        try {
            recorder.prepare();
            recorder.start();


        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Failed", "Failed");
            binding.cardViewaudio.setVisibility(View.GONE);

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
        }

    }

    private void initAudio(DialogMessageBinding binding) {
        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(files.get(files.size() - 1).getPath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(100.0f, 100.0f);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            binding.recordDuration.setText(getDuration(mediaPlayer.getDuration()));
            binding.cardViewaudio.setVisibility(View.VISIBLE);

            mediaPlayer.setOnPreparedListener(mediaPlayer -> {
                binding.cardViewaudio.setVisibility(View.VISIBLE);
                binding.seekBar.setMax(mediaPlayer.getDuration());
                binding.imagePlay.setImageResource(R.drawable.ic_play);
            });

            mediaPlayer.setOnCompletionListener(mediaPlayer -> {

                binding.recordDuration.setText(getDuration(mediaPlayer.getDuration()));
                binding.imagePlay.setImageResource(R.drawable.ic_play);
                binding.seekBar.setProgress(0);
                handler.removeCallbacks(runnable);

            });

        } catch (Exception e) {
            Log.e("eeeex", e.getLocalizedMessage() + e.toString());
            mediaPlayer.release();
            mediaPlayer = null;
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
            binding.cardViewaudio.setVisibility(View.GONE);

        }
    }

    private void updateProgress(DialogMessageBinding binding) {
        binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
        binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
        handler = new Handler();
        runnable = () -> updateProgress(binding);

        handler.postDelayed(runnable, 1000);


    }

    private void initAudio(String path) {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(100.0f, 100.0f);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                // binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
                mediaPlayer.pause();
                ///binding.imagePlay.setImageResource(R.drawable.ic_play);

            } else {

                if (mediaPlayer != null) {
                    // binding.imagePlay.setImageResource(R.drawable.ic_pause);

                    mediaPlayer.start();
                    //updateProgress(binding);


                }
            }

        } catch (Exception e) {
            Log.e("eeeex", e.getLocalizedMessage() + e.toString());
            mediaPlayer.release();
            mediaPlayer = null;
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }

        }
    }

    private void updateProgress(DialogDisplayBinding binding) {
        binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
        binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
        handler = new Handler();
        runnable = () -> updateProgress(binding);

        handler.postDelayed(runnable, 1000);


    }

    private void checkWritePermission() {

        if (ContextCompat.checkSelfPermission(this, audio_perm) != PackageManager.PERMISSION_GRANTED) {


            isPermissionGranted = false;

            ActivityCompat.requestPermissions(this, new String[]{write_perm, audio_perm}, write_req);


        } else {
            isPermissionGranted = true;
        }
    }

    private String getDuration(long duration) {

        String total_duration = "00:00";

        if (mediaPlayer != null) {
            total_duration = String.format(Locale.ENGLISH, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );


        }

        return total_duration;

    }

    public void settime(String title) {
        time = title;
    }
}

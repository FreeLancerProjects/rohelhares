package com.rohelhares.activity_map;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import androidx.appcompat.app.AppCompatActivity;
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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityMapBinding binding;
    private String lang;
    private HashMap<Integer, List<Double>> markerlist;
    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 6.0f;
    private int count = 0;
    private boolean aceept = false;
    private int pos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map);
        updateUI();
        initView();
    }


    private void initView() {
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
            mMap.setTrafficEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setIndoorEnabled(true);
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


    private void addMarker(List<Double> branchs) {
        Log.e("ldlld", branchs.get(0) + "");
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentPadding(15, 15, 15, 15);
        // iconGenerator.setTextAppearance(R.style.iconGenText);
        // iconGenerator.setBackground(ContextCompat.getDrawable(activity, android:R.drawable.ic_map));
        // iconGenerator.setColor(R.color.black);

        Marker marker;
        Marker marker1;
        getDirection(branchs.get(0) + "", branchs.get(1) + "", branchs.get(2) + "", branchs.get(3) + "");

        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(0), branchs.get(1))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        marker1 = mMap.addMarker(new MarkerOptions().position(new LatLng(branchs.get(0), branchs.get(1))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


    }

    private void getDirection(String client_lat, String client_lng, String driver_lat, String driver_lng) {
        Log.e("sssss", client_lat);

        String origin = "", dest = "";
        origin = client_lat + "," + client_lng;
        dest = driver_lat + "," + driver_lng;
        Api.getService("https://maps.googleapis.com/maps/api/")
                .getDirection(origin, dest, "rail", getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDirectionModel>() {
                    @Override
                    public void onResponse(Call<PlaceDirectionModel> call, Response<PlaceDirectionModel> response) {
                        if (response.body() != null && response.body().getRoutes().size() > 0) {

                            drawRoute(PolyUtil.decode(response.body().getRoutes().get(0).getOverview_polyline().getPoints()));

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

    private void drawRoute(List<LatLng> latLngList) {
        PolylineOptions options = new PolylineOptions();
        options.geodesic(true);
        options.color(ContextCompat.getColor(this, R.color.colorPrimary));
        options.width(8.0f);
        options.addAll(latLngList);
        mMap.addPolyline(options);


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

package com.starglare.accasy.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.starglare.accasy.R;
import com.starglare.accasy.adapter.ReportHistoryAdapter;
import com.starglare.accasy.core.Logger;
import com.starglare.accasy.models.ReportModel;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;


public class HistoryFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener, ReportHistoryAdapter.HistoryItemSelectCallback {


    private int mColumnCount = 1;
    private List<ReportModel> reportModels;
    private RecyclerView recyclerView;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private final String LAYER_ID = "LAYER_ID";
    private final String SOURCE_ID = "SOURCE_ID";
    private final String ICON_ID = "ICON_ID";
    Logger logger;
    Cursor cursor;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.TOKEN));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        reportModels = ReportModel.listAll(ReportModel.class);

        recyclerView = view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), mColumnCount));
        }
        recyclerView.setAdapter(new ReportHistoryAdapter(reportModels, this));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getActivity().setTitle(getActivity().getResources().getString(R.string.title_history));

        View view = getView();

        mapView = view.findViewById(R.id.historyMap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.addOnMapClickListener(this);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.addImage(ICON_ID, BitmapFactory.decodeResource(
                        getActivity().getResources(), R.drawable.blue_marker_view));
//                setStyle(style);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onSelect(ReportModel reportModel) {
        String latlng[] = reportModel.getCoordinates().split(",");
        double longitude = Double.parseDouble(latlng[0]);
        double latitude = Double.parseDouble(latlng[1]);
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0));

        mapboxMap.getStyle().removeLayer(LAYER_ID);
        mapboxMap.getStyle().removeSource(SOURCE_ID);
        mapboxMap.getStyle().addSource(new GeoJsonSource(SOURCE_ID, Feature.fromGeometry(Point.fromLngLat(longitude, latitude))));
        mapboxMap.getStyle().addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(PropertyFactory.iconImage(ICON_ID),
                        iconAllowOverlap(true),
                        iconOffset(new Float[] {0f, -9f})));
    }
}

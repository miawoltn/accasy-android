package com.starglare.accasy.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnLocationStaleListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.starglare.accasy.R;
import com.starglare.accasy.activity.HelpActivity;
import com.starglare.accasy.adapter.CategoryAdapter;
import com.starglare.accasy.core.Helper;
import com.starglare.accasy.core.HttpClient;
import com.starglare.accasy.core.Logger;
import com.starglare.accasy.models.ReportModel;
import com.starglare.accasy.models.SubcategoryModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.starglare.accasy.fragments.BaseFragment.RESOURCE_SUFFIX;
import static com.starglare.accasy.fragments.BaseFragment.TYPE_ARRAY;
import static com.starglare.accasy.fragments.BaseFragment.TYPE_DRAWABLE;

public class ReportMapFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener, CategoryAdapter.CategorySelectCallback {

    RotationGestureOverlay mRotationGestureOverlay;
    Logger logger;
    Cursor cursor;
    Marker marker;
    MapView mapView;
    private NestedScrollView bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private CardView toolbar_layout;
    private FloatingActionButton locationIcon, helpIcon;
    private LinearLayout streetIcon, trafficIcon, othersIcon;
    private RecyclerView subcategory;
    private CategoryAdapter categoryAdapter;
    private AppBarLayout appBarLayout;
    private Button submitButton;
    private AppCompatImageView photo_1, photo_2, photo_3;
    private EditText searchTextView, commentEditText;
    private TextView selectedLocation;
    private ImageView slideUp1, slideUp2;
    private List<SubcategoryModel> subcategoryModels;
    private MapboxMap mapboxMap;
    private Style style;
    private CategoryAdapter.CategorySelectCallback changeCallback;
    private final int REQUEST_CODE_AUTOCOMPLETE = 100;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private final String LAYER_ID = "LAYER_ID";
    private final String SOURCE_ID = "SOURCE_ID";
    private final String ICON_ID = "ICON_ID";
    private int CAMERA_START_REQUEST = 1;
    private ReportModel reportModel;

    public ReportMapFragment() {
        // Required empty public constructor
        subcategoryModels = new ArrayList<>();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.TOKEN));
        reportModel = ReportModel.getModelInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        View view = getView();
        toolbar_layout = view.findViewById(R.id.toolbar_layout);
        locationIcon = view.findViewById(R.id.location);
        helpIcon = view.findViewById(R.id.help);
        streetIcon = view.findViewById(R.id.wsf);
        trafficIcon = view.findViewById(R.id.ti);
        othersIcon = view.findViewById(R.id.ei);
        commentEditText = view.findViewById(R.id.comment);
        searchTextView = view.findViewById(R.id.searchTextView);
        submitButton = view.findViewById(R.id.submit);
        slideUp1 = view.findViewById(R.id.slideUp1);
        slideUp2 = view.findViewById(R.id.slideUp2);
        photo_1 = view.findViewById(R.id.photo_1);
        photo_2 = view.findViewById(R.id.photo_2);
        photo_3 = view.findViewById(R.id.photo_3);
        selectedLocation = view.findViewById(R.id.selectedLocation);
        appBarLayout = view.findViewById(R.id.appBarLayout);
        appBarLayout.setVisibility(View.VISIBLE);
        appBarLayout.setAlpha(0);

        helpIcon.setOnClickListener(iconClick);
        locationIcon.setOnClickListener(iconClick);
        photo_1.setOnClickListener(iconClick);
        photo_2.setOnClickListener(iconClick);
        photo_3.setOnClickListener(iconClick);
        submitButton.setOnClickListener(iconClick);

        Animation animShake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        //animShake.setInterpolator(new LinearInterpolator());
        slideUp1.startAnimation(animShake);
        slideUp2.startAnimation(animShake);

        searchTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                searchLocation();
                return true;
            }
        });

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                    streetIcon.setOnClickListener(iconClick);
                    trafficIcon.setOnClickListener(iconClick);
                    othersIcon.setOnClickListener(iconClick);
                    slideUp1.setVisibility(View.INVISIBLE);
                    slideUp2.setVisibility(View.INVISIBLE);
                    return;
                }

                if(newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    streetIcon.setOnClickListener(null);
                    trafficIcon.setOnClickListener(null);
                    othersIcon.setOnClickListener(null);
                    slideUp1.setVisibility(View.VISIBLE);
                    slideUp2.setVisibility(View.VISIBLE);
                    return;
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float fadeIn = slideOffset;
                float fadeOut = 1 - slideOffset;
                toolbar_layout.setAlpha(fadeOut);
                appBarLayout.setAlpha(fadeIn);
                locationIcon.setAlpha(fadeOut);
                helpIcon.setAlpha(fadeOut);

                if(fadeOut == 0){
                    locationIcon.hide();
                    helpIcon.hide();
                }
                else{
                    locationIcon.show();
                    helpIcon.show();
                }
            }
        });

        subcategory = view.findViewById(R.id.subcat_list);

        // Set the adapter
        initialiseAdapterModel();
        categoryAdapter = new CategoryAdapter(subcategoryModels, this);
        subcategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        subcategory.setAdapter(categoryAdapter);
    }

    private void initialiseAdapterModel() {
        for (String category : getStringArrayValue("category")) {
            for (String subcategory : getStringArrayValue(RESOURCE_SUFFIX+category.toLowerCase())) {
                SubcategoryModel subcategoryModel = new SubcategoryModel();
                subcategoryModel.setCategory(category);
                subcategoryModel.setSubcategory(subcategory.replace('_', ' '));
                subcategoryModel.setImage(Helper.getResourceId("history", TYPE_DRAWABLE,getContext()));
                subcategoryModels.add(subcategoryModel);
                Log.println(Log.DEBUG, ReportMapFragment.class.getCanonicalName(), category + subcategory);
            }
        }
    }

    private View.OnClickListener iconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.wsf:
                    //showToast("street");
                    categoryAdapter.filterSubcategory("street");
                    break;
                case R.id.ti:
                    //showToast("utility");
                    categoryAdapter.filterSubcategory("utility");
                    break;
                case R.id.ei:
                    //showToast("others");
                    categoryAdapter.filterSubcategory("others");
                    break;
                case R.id.location:
                    enableLocationComponent();
                    break;
                case R.id.help:
                    showHelp();
                    break;
                case R.id.photo_1:
                    //showToast("Photo 1");
                    startCamera();
                    break;
                case R.id.photo_2:
                    break;
                case R.id.photo_3:
                    break;
                case R.id.submit :
                    //showToast("send");
                    saveAndSendReport();
                    break;
            }
        }
    };

    private void showHelp() {
        Intent intent= new Intent(getActivity(), HelpActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        getActivity().startActivity(intent);
    }

    private void saveAndSendReport() {

        if(!Helper.isNetworkAvailable(getContext())) {
            showToast("no connection");
            return;
        }
        String comment = commentEditText.getText().toString();
        if(comment.trim().length() == 0) {
            commentEditText.setError("Required");
            commentEditText.setFocusable(true);
            return;
        }
        reportModel.setComment(comment);

        if(reportModel.getCoordinates().length() == 0) {
            showToast("No locationIcon");
            return;
        }

        if(reportModel.getSubCategory().length() == 0) {
            showToast("No report type");
            return;
        }

        if(reportModel.getImage() == null) {
            showToast("No image");
            return;
        }
        sendReport();
        //save();

    }

    private void sendReport() {
        HttpClient httpClient = new HttpClient();
        httpClient.url(getResources().getString(R.string.host)+getResources().getString(R.string.endpoint));
        httpClient.body(getRequestBody());
        httpClient.mediaType(HttpClient.JSONMediaType);
        httpClient.buildRequest();

        final ProgressDialog mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(true);
        mDialog.show();

        final Handler mHandler = new Handler(Looper.getMainLooper());
        httpClient.newRequest().enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                mDialog.dismiss();
                final String message = e.getMessage();
                e.printStackTrace();
                //Log.d("HttpClient", e.getMessage());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(message);
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String mMessage = response.body().string();
                Log.d("HttpClient", mMessage);

                try
                {
                    final JSONObject jsonObject = new JSONObject(mMessage);
                    String status = jsonObject.getString("status");
                    final String message = jsonObject.getString("message");
                    if(status.equals("success")) {
                        Log.d("HttpClient", message);
                        mDialog.dismiss();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showToast(message);
                                save();
                            }
                        });
                    } else if(status.equals("error")) {
                        final JSONObject jsonArray = new JSONObject(jsonObject.getString("message"));
                        Log.d("HttpClient", jsonArray.toString());
                        mDialog.dismiss();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showToast(jsonArray.toString());
                            }
                        });
                    }
                }catch (JSONException je) {
                    final String error = je.getMessage();
                    je.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast(error);
                        }
                    });
                }
            }
        });
    }

    private String getRequestBody() {
        String _category = reportModel.getSubCategory();
        String _location = reportModel.getCoordinates();
        String _comment = reportModel.getComment();
        String _image = Helper.convertImageToString(BitmapFactory.decodeByteArray(reportModel.getImage(), 0, reportModel.getImage().length));

        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("category", _category);
            jsonObject.put("location", _location);
            jsonObject.put("description", _comment);
            jsonObject.put("picture", _image);

        }
        catch (JSONException je) {

        }

        Log.d("HttpClient::RequestBody", jsonObject.toString());

        return jsonObject.toString();
    }

    private void save() {

        Log.i(ReportMapFragment.class.getName(), reportModel.toString());
        try {

        } catch (Exception e) {

        }
        reportModel.save();

        categoryAdapter.resetAdapter();
        commentEditText.setText("");
        unSetLocationText();
        photo_1.setImageResource(R.drawable.ic_image_black_24dp);
        submitButton.setEnabled(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        reportModel = ReportModel.getModelInstance();
    }

    private void searchLocation() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken())
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .country("ng")
                        .build(PlaceOptions.MODE_CARDS))
                .build(getActivity());
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
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
                setStyle(style);
            }
        });
    }

    private void setStyle(Style style) {
        this.style = style;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected locationIcon's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected locationIcon's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected locationIcon
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(15)
                                    .build()), 4000);
                    searchTextView.setText(selectedCarmenFeature.text());
                }
            }
        }

        if(requestCode == CAMERA_START_REQUEST) {
            if(resultCode == RESULT_OK) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                photo_1.setImageBitmap(image);
                reportModel.setImage(Helper.convertImageToByte(image));
                Log.i(ReportModel.class.getName(), "Image set!");
//                modelChangeCallback.setImage(Helper.convertImageToByte(image));
//                imageCaptured = true;
            }
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            if(Helper.isGPSEnabled(getContext())) {

                // Get an instance of the component
                final LocationComponent locationComponent = mapboxMap.getLocationComponent();

                // Activate with options
                locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(getContext(), style).build());

                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);

                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);

                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.COMPASS);

                locationComponent.addOnLocationStaleListener(new OnLocationStaleListener() {
                    @Override
                    public void onStaleStateChange(boolean isStale) {
                        if (!isStale) {
                            Location location = locationComponent.getLastKnownLocation();
                            if(location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                 setLocationText();
                                //makeGeocodeSearch(latitude, longitude);
                                reportModel.setCoordinates(String.format("%s,%s", longitude, latitude));
                                Log.i(ReportMapFragment.class.getName(), "Location set!     GPS");
                                submitButton.setEnabled(true);
                            }
                        }
                    }
                });
            }
            else {
                Helper.showSettingsAlert(getContext(), "Location request", "In order to determine you current locationIcon, " +
                        "please enable GPS in settings", "Settings", "Cancel").show();
            }
        } else {
            Helper.requestForPermission(getActivity(), Helper.GPS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final List<String> permissionsNotGrantedWithRationale = new ArrayList<>();
        final List<String> permissionsNotGrantedWithNoRationale = new ArrayList<>();
        final List<String> permissionsGranted = new ArrayList<>();
        boolean requestAgain = true;
        if(grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i])) {
                    permissionsNotGrantedWithRationale.add(permissions[i]);
                } else if(grantResults[i] != PackageManager.PERMISSION_GRANTED && !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i])) {
                    permissionsNotGrantedWithNoRationale.add(permissions[i]);
                }else if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted.add(permissions[i]);
                }
            }
            if(permissionsNotGrantedWithNoRationale.size() > 0) {
                showToast("Please enable the required permissions under settings.");
            } else if(permissionsGranted.size() == Helper.PERMISSIONS.length) {

            }
        }
    }

    private void makeGeocodeSearch(double lat, double lng) {
        try {
            // Build a Mapbox geocoding request
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .query(Point.fromLngLat(lng, lat))
                    .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                    .mode(GeocodingCriteria.MODE_PLACES)
                    .build();
            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call,
                                       Response<GeocodingResponse> response) {
                    List<CarmenFeature> results = response.body().features();
                    if (results.size() > 0) {

                        // Get the first Feature from the successful geocoding response
                        CarmenFeature feature = results.get(0);
                        selectedLocation.setText(feature.placeName());
                        //animateCameraToNewPosition(latLng);
                    } else {
                        showToast("Unable to resolve locationIcon");
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Timber.e("Geocoding Failure: " + throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Timber.e("Error geocoding: " + servicesException.toString());
            servicesException.printStackTrace();
        }
    }

    private void setLocationText() {
        selectedLocation.setText("Set");
        selectedLocation.setTextColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        showToast("Location set");
    }

    private void unSetLocationText() {
        selectedLocation.setText("not set");
        selectedLocation.setTextColor(getActivity().getResources().getColor(R.color.grey_font));
    }

    private void startCamera() {
        if(Helper.hasPermissions(getContext(), Helper.PERMISSIONS)) {
            Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(cameraIntent, CAMERA_START_REQUEST);
        } else {
            Helper.requestForPermission(getActivity(), Helper.CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        changeCallback = this;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(changeCallback != null)
            changeCallback = null;
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

    public void changeBottomSheetState(int state) {
        bottomSheetBehavior.setState(state);
    }

    private float map(float value, float min, float max) {
        return min + value * (max - min);
    }

    public String[] getStringArrayValue(String stringArrayName) {
        int resourceId = Helper.getResourceId(stringArrayName,TYPE_ARRAY,getContext());
        return getResources().getStringArray(resourceId);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if(!Helper.isNetworkAvailable(getContext()))
            return false;
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()), 15.0));
        mapboxMap.getStyle().removeLayer(LAYER_ID);
        mapboxMap.getStyle().removeSource(SOURCE_ID);
        mapboxMap.getStyle().addSource(new GeoJsonSource(SOURCE_ID, Feature.fromGeometry(Point.fromLngLat( point.getLongitude(), point.getLatitude()))));
        mapboxMap.getStyle().addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(PropertyFactory.iconImage(ICON_ID),
                        iconAllowOverlap(true),
                        iconOffset(new Float[] {0f, -9f})));

        double latitude =  point.getLatitude();
        double longitude = point.getLongitude();
        setLocationText();
        reportModel.setCoordinates(String.format("%s,%s", longitude, latitude));
        Log.i(ReportMapFragment.class.getName(), "Location set      User click");
        submitButton.setEnabled(true);

        return false;
    }

    @Override
    public void setCategory(String category) {
        reportModel.setCategory(category);
    }

    @Override
    public void setSubCategory(String subCategory) {
        reportModel.setSubCategory(subCategory);
    }
}

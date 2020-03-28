package com.starglare.accasy.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.starglare.accasy.activity.BaseActivity;
import com.starglare.accasy.adapter.CategoryAdapter;
import com.starglare.accasy.core.Helper;
import com.starglare.accasy.R;
import com.starglare.accasy.core.HttpClient;
import com.starglare.accasy.core.Logger;
import com.starglare.accasy.core.MyLocation;
import com.starglare.accasy.models.ReportModel;
import com.starglare.accasy.models.SubcategoryModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.droidsonroids.gif.GifImageView;

import static android.app.Activity.RESULT_OK;
import static com.starglare.accasy.models.FragmentNames.CATEGORY_FRAGMENT;
import static com.starglare.accasy.models.FragmentNames.FORM_FRAGMENT;
import static com.starglare.accasy.models.FragmentNames.SUB_CATEGORY_FRAGMENT;


public class FormFragment extends BaseFragment implements LocationListener, Helper.onVolleyRequestCompleteCallBack, CategoryAdapter.CategorySelectCallback {

    private static final String TAG = "FORM_FRAGMENT";
    private TextView selectedLocationText;
    private EditText commentEditText;
//    private EditText phoneNumber;
    private Button findmeButton, submitButton;
    private ImageView photo;
    private LinearLayout streetIcon, trafficIcon, othersIcon, tapToUpdate;
    private GifImageView refresh;
    private long minTime = 0;
    private float minDistance = 0;
    BroadcastReceiver gpsReceiver;
    LocationManager locationManager;
    private int CAMERA_START_REQUEST = 1;
    Logger logger;
    boolean gettingCoordinates = false;
    boolean locationfound = false;
    boolean canSendReport = true;
    boolean imageCaptured = false;
    SweetAlertDialog progressDialog;
    private ReportModel reportModel;
    private CategoryAdapter categoryAdapter;
    private List<SubcategoryModel> subcategoryModels;
    private CategoryAdapter.CategorySelectCallback changeCallback;
    private RecyclerView categoryRecylerView;
    public FormFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateNavigationStack();
        reportModel = ReportModel.getModelInstance();
        subcategoryModels = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_form, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getActivity().getResources().getString(R.string.title_report));
//        logger = Logger.getInstance(getContext());
//        progressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
//        progressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//        progressDialog.setTitleText("Please wait...");
//        progressDialog.setCancelable(false);
        view = getView();


        init();
//        gpsReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//              //if(!locationfound) {
//                  getcoordinates();
//                  changeRefreshIcon();
//              //}
//
//            }
//        };
//        getContext().registerReceiver(gpsReceiver,new IntentFilter("android.location.PROVIDERS_CHANGED"));
//
//        // if(!locationfound) {
//        //    getcoordinates();
//        //    changeRefreshIcon();
//        // }

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                Log.d("gotLocation", location.toString());
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getContext(), locationResult);
    }

    private void init() {
        // coordinates = view.findViewById(R.id.coordinates);
        // coordinates.setText("9.071856,7.486240");
        // modelChangeCallback.setCoordinates("9.071856,7.486240");
        // phoneNumber = view.findViewById(R.id.phone_number);
        selectedLocationText = view.findViewById(R.id.selectedLocation);
        findmeButton = view.findViewById(R.id.findme_button);
        commentEditText = view.findViewById(R.id.comment);
        photo = view.findViewById(R.id.photo_1);
        refresh = view.findViewById(R.id.refresh);
        submitButton = view.findViewById(R.id.submit);
        streetIcon = view.findViewById(R.id.wsf);
        trafficIcon = view.findViewById(R.id.ti);
        othersIcon = view.findViewById(R.id.ei);
        tapToUpdate = view.findViewById(R.id.tap_to_update);
        categoryRecylerView = view.findViewById(R.id.subcat_list);
        photo.setOnClickListener(onClickListener);
        findmeButton.setOnClickListener(onClickListener);
        submitButton.setOnClickListener(onClickListener);
        streetIcon.setOnClickListener(onClickListener);
        trafficIcon.setOnClickListener(onClickListener);
        othersIcon.setOnClickListener(onClickListener);
        tapToUpdate.setOnClickListener(onClickListener);

        // Set the adapter
        initialiseAdapterModel();
        categoryAdapter = new CategoryAdapter(subcategoryModels, this);
        categoryRecylerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryRecylerView.setAdapter(categoryAdapter);
    }

    private void initialiseAdapterModel() {
        String[] categories = {"wsf", "ti", "ei"};
        for (String category : categories) {
            for (String subcategory : getStringArrayValue(category)) {
                SubcategoryModel subcategoryModel = new SubcategoryModel();
                subcategoryModel.setCategory(category);
                subcategoryModel.setSubcategory(subcategory.replace('_', ' '));
                subcategoryModel.setImage(Helper.getResourceId("history", TYPE_DRAWABLE,getContext()));
                subcategoryModels.add(subcategoryModel);
                Log.println(Log.DEBUG, ReportMapFragment.class.getCanonicalName(), category + subcategory);
            }
        }
    }

    private String[] getStringArrayValue(String stringArrayName) {
        int resourceId = Helper.getResourceId(stringArrayName,TYPE_ARRAY,getContext());
        return getResources().getStringArray(resourceId);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tap_to_update:
                    getcoordinates();
                    changeRefreshIcon();
                    break;
                case R.id.wsf:
                    //showToast("street");
                    categoryAdapter.filterSubcategory("wsf");
                    break;
                case R.id.ti:
                    //showToast("utility");
                    categoryAdapter.filterSubcategory("ti");
                    break;
                case R.id.ei:
                    //showToast("others");
                    categoryAdapter.filterSubcategory("ei");
                    break;
                case R.id.photo_1:
                    startCamera();
                    break;
                case R.id.submit:
                    saveAndSendReport();
                    break;
            }
        }
    };

//    private void processReport() {
//        if(requiredFieldsProvided()){
//            progressDialog.show();
//            modelChangeCallback.setComment(comment.getText().toString());
//            modelChangeCallback.setPhoneNumber(phoneNumber.getText().toString());
//            modelChangeCallback.setTime(System.currentTimeMillis()/1000);
//            logger.insertReport(model);
//            if(Helper.isNetworkAvailable(getContext()))
//                sendReport();
//        }
//    }

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
        save();

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
        photo.setImageResource(R.drawable.ic_image_black_24dp);
        submitButton.setEnabled(false);

        reportModel = ReportModel.getModelInstance();
    }

    private void changeRefreshIcon() {
        if(gettingCoordinates) {
            //selectedLocationText.setText("Finding location...");
            refresh.setImageResource(R.drawable.loading);
        }else {
            refresh.setImageResource(R.drawable.load);
        }
    }

    private void unSetLocationText() {
        selectedLocationText.setText("Location unknown");
        selectedLocationText.setTextColor(getActivity().getResources().getColor(R.color.grey_font));
    }

//    private void sendReport() {
//        Helper.sendReport(getContext(),model,this);
//    }

    private boolean requiredFieldsProvided() {
        //reset flag
        canSendReport = canSendReport == canSendReport;
        if(selectedLocationText.getText().equals("not set")) {
            Toast.makeText(getContext(),"Location not set", Toast.LENGTH_SHORT).show();
            canSendReport = false;
        }
        if(commentEditText.getText().toString().trim().length() == 0) {
            commentEditText.setError("Required");
            canSendReport = false;
        }
        if(!imageCaptured) {
            Toast.makeText(getContext(),"Image is required",Toast.LENGTH_SHORT).show();
            canSendReport = false;
        }
        return canSendReport;
    }

    private void startCamera() {
        if(Helper.hasPermissions(getContext(), Helper.PERMISSIONS)) {
            Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(cameraIntent, CAMERA_START_REQUEST);
        } else {
            Helper.requestForPermission(getActivity(), Helper.CAMERA_PERMISSION_CODE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
    }

    private void showAlert() {
        Helper.sendTestRequest(getContext());
    }


    @SuppressWarnings("MissingPermission")
    public void getcoordinates() {
        Log.d("Step 1", "Getting coordinates");
       if(Helper.hasPermissions(getContext(),Helper.PERMISSIONS)) {
           Log.d("Step 2", "Checking for permission");
           if(Helper.isGPSEnabled(getContext())) {
               Log.d("Step 3", "Checking if GPS is enabled");
               locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
               Log.d("Step 4", "Getting the location manager");
//               Log.i("Last known location", String.valueOf(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)));
               locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
               Log.d("Step 5", "Requesting for location update");
               gettingCoordinates = true;
           }else {
               Log.d("Step 2.1", "Requesting for gps access");
               new AlertDialog.Builder(getContext(), SweetAlertDialog.NORMAL_TYPE)
                       .setTitle("Settings")
                       .setMessage("You need to enable your gps. Goto settings?")
                       .setNegativeButton(getString(R.string.permissionCancelText), new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               Log.d("Step 2.1.1", "Request denied");
                               dialogInterface.cancel();
                           }
                       })
                       .setPositiveButton(getString(R.string.permissionConfirmText), new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               Log.d("Step 2.1.2", "Request granted");
                               Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                               startActivity(intent);
                               dialogInterface.cancel();
                           }
                       })
                       .setCancelable(true)
                       .show();
           }
       }else {
           requestPermissions(Helper.PERMISSIONS,Helper.GPS_PERMISSION_CODE);
           Log.d("Step 1.1", "Requesting for location permission");
       }
    }

    private void updateNavigationStack() {
        CURRENT_FRAGMENT = FORM_FRAGMENT;
        NEXT_FRAGMENT = CATEGORY_FRAGMENT;
        PREVIOUS_FRAGMENT = SUB_CATEGORY_FRAGMENT;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final List<String> permissionsNotGrantedWithRationale = new ArrayList<>();
        final List<String> permissionsNotGrantedWithNoRationale = new ArrayList<>();
        final List<String> permissionsGranted = new ArrayList<>();
        boolean requestAgain = true;
        if(grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i])) {
                    permissionsNotGrantedWithRationale.add(permissions[i]);
                } else if(grantResults[i] != PackageManager.PERMISSION_GRANTED &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i])) {
                    permissionsNotGrantedWithNoRationale.add(permissions[i]);
                }else if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted.add(permissions[i]);
                }
            }
            if(permissionsNotGrantedWithRationale.size() > 0) {
                new AlertDialog.Builder(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitle(getString(R.string.permissionTitle))
                        .setMessage(getString(R.string.permissionContent))
                        .setNegativeButton(getString(R.string.permissionCancelText), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setPositiveButton(getString(R.string.permissionConfirmText), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(Helper.PERMISSIONS,Helper.GPS_PERMISSION_CODE);
                                dialogInterface.cancel();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
            else if(permissionsNotGrantedWithNoRationale.size() > 0) {
                Toast.makeText(getContext(),
                        "Please enable the required permissions under settings.",
                        Toast.LENGTH_LONG).show();
            } else if(permissionsGranted.size() == Helper.PERMISSIONS.length) {

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_START_REQUEST) {
            if(resultCode == RESULT_OK) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                photo.setImageBitmap(image);
                reportModel.setImage(Helper.convertImageToByte(image));
                Log.d("onActivityResult", "Image captured");
                //modelChangeCallback.setImage(Helper.convertImageToByte(image));
                //imageCaptured = true;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ModelChangeCallback) {
            modelChangeCallback = (ModelChangeCallback) context;
        } else {

        }

        if (gpsReceiver != null) {
            getContext().registerReceiver(gpsReceiver,new IntentFilter("android.location.PROVIDERS_CHANGED"));
            Log.d("onAttach", "Location broadcast receiver registered");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (gpsReceiver != null) {
            getContext().unregisterReceiver(gpsReceiver);
            Log.d("onDetach", "Location broadcast receiver unregistered");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNavigationStack();
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocationText();
        reportModel.setCoordinates(String.format("%s,%s", location.getLatitude(),location.getLongitude()));
        Log.i(ReportMapFragment.class.getName(), "Location set!     GPS");
        locationManager.removeUpdates(this);
        gettingCoordinates = false;
        locationfound = true;
        submitButton.setEnabled(true);
        changeRefreshIcon();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(getClass().getSimpleName(),"status changed");
        Log.i(getClass().getSimpleName(),extras.toString());
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(getClass().getSimpleName(),"provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(getClass().getSimpleName(),"provider disabled");
        gettingCoordinates = false;
        changeRefreshIcon();
    }

    private void setLocationText() {
        selectedLocationText.setText("Location found");
        selectedLocationText.setTextColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        Toast.makeText(getContext(), "Location set", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReportPostSuccess(String id) {
        progressDialog.dismiss();
        logger.updateReport(String.valueOf(logger.lastInsertId));
        new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Report sent!")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        ((BaseActivity)getActivity()).loadFragment(Helper.getCurrentFragment(NEXT_FRAGMENT,null,null),NEXT_FRAGMENT,true);
                    }
                })
                .show();
    }

    @Override
    public void onReportPostError() {
        progressDialog.dismiss();
        Helper.showAlert(getContext(),SweetAlertDialog.ERROR_TYPE,"Oops!","Something went wrong.");
//        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                sweetAlertDialog.dismiss();
//                ((BaseActivity)getActivity()).loadFragment(Helper.getCurrentFragment(NEXT_FRAGMENT,null,null),NEXT_FRAGMENT,true);
//            }
//        });
    }

    @Override
    public void onSuccessPing() {
        sendReport();
    }

    @Override
    public void onErrorPing() {
        progressDialog.dismiss();
            progressDialog.dismiss();
            Helper.showAlert(getContext(),SweetAlertDialog.ERROR_TYPE,"Oops!","It seems you are offline. Your report was not sent. We will send it when you come online.");
//                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                        @Override
//                        public void onClick(SweetAlertDialog sweetAlertDialog) {
//                            sweetAlertDialog.dismiss();
//                            ((BaseActivity)getActivity()).loadFragment(Helper.getCurrentFragment(NEXT_FRAGMENT,null,null),NEXT_FRAGMENT,true);
//                        }
//                    });
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

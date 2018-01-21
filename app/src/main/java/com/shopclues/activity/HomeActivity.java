package com.shopclues.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shopclues.bean.LocationObj;
import com.shopclues.carpool.R;
import com.shopclues.constant.Constant;
import com.shopclues.location.LocationUtils;
import com.shopclues.util.MapUtil;

import java.util.List;

import static android.media.CamcorderProfile.get;


/**
 * Created by manish on 1/8/17.
 */
public class HomeActivity extends CarPoolBaseActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ResultCallback<LocationSettingsResult>, PlaceSelectionListener, OnMapReadyCallback {
    private String TAG = "HomeActivity";
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    private static final int REQUEST_SELECT_PLACE = 1000;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE};
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    protected boolean mRequestingLocationUpdates;
    private EditText editTextSource, edtplace;
    private EditText editTextDest;
    PlaceAutocompleteFragment autocompleteFragment;
    private SupportMapFragment mapViewFragment;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
   // LatLng souceLatLng;
    Marker mCurrLocation;
    private LocationObj locationObj = new LocationObj();
    private Button btnConfirm;
    private TextView tvDirection;
    StringBuffer sourcebuff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
//        setUpDestination();
    }

    private void initViews() {
        editTextSource = (EditText) findViewById(R.id.edit_source);
        tvDirection = (TextView) findViewById(R.id.direction);
        findViewById(R.id.edit_Dest).setVisibility(View.GONE);
        editTextSource.setSingleLine();
        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_fragment);
        autocompleteFragment.setUserVisibleHint(false);
        edtplace = ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input));
        edtplace.setWidth(editTextSource.getWidth());
        edtplace.setTextSize(14.0f);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setBoundsBias(BOUNDS_MOUNTAIN_VIEW);
        mapViewFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapViewFragment.getMapAsync(this);
        tvDirection.setOnClickListener(directionclick);
    }

    protected void checkLocationSettings() {
        Log.d(this.getClass().getName(), "checkLocationSettings");
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(this);
    }


    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d(this.getClass().getName(), "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        Log.d(this.getClass().getName(), "status code===========" + status.getStatusCode());
        switch (status.getStatusCode()) {

            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG,
                        "Location settings are not satisfied. Show the user a dialog to"
                                + "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(), and
                    // check the result
                    // in onActivityResult().
                    status.startResolutionForResult(HomeActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG,
                        "Location settings are inadequate, and cannot be fixed here. Dialog "
                                + "not created.");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case Constant.MapCredential.GPS_PERMS_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGetiingLocation();
                    // permission was granted, yay! Do the task you need to do.
                } else {

                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                break;
            case Constant.MapCredential.MAP_PERMS_CODE:

                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to
            // startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG,
                                "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG,
                                "User chose not to make required location settings changes.");
                        break;


                    case REQUEST_SELECT_PLACE:
                        Place place = PlaceAutocomplete.getPlace(this, data);
                        this.onPlaceSelected(place);
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        Status status = PlaceAutocomplete.getStatus(this, data);
                        this.onError(status);
                        break;
                }

                break;
        }
    }

    protected void startLocationUpdates() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(HomeActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                Constant.MapCredential.GPS_PERMS_CODE);

                    }
            } else {
                startGetiingLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void startGetiingLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
//                ActivityCompat#requestPermissions
//             here to request the missing permissions, and then overriding
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                      int[] grantResults)
//             to handle the case where the user grants the permission. See the documentation
//             for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mRequestingLocationUpdates = true;
                        // setButtonsEnabledState();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact. Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        if (mGoogleApiClient != null) {
            checkLocationSettings();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the
        // GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity
        // is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mRequestingLocationUpdates = false;
                        // setButtonsEnabledState();
                    }
                });
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
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
            mCurrentLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            Log.d(this.getClass().getName(), "on connected!!!!!!!1");
             updateLocationUI();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(this.getClass().getName(), "on location changed!!!!!!");
//        Toast.makeText(HomeActivity.this, "Address==" + LocationUtils.getAddress(HomeActivity.this, mCurrentLocation), Toast.LENGTH_LONG).show();
        updateLocationUI();
//        MapUtil.getIntance().drawMarker(mGoogleMap,location.getLatitude(), location.getLongitude());
        locationObj.sourceLat = location.getLatitude();
        locationObj.sourceLong = location.getLongitude();
        //souceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        drawMap(locationObj);
        // Toast.makeText(this,
        // getResources().getString(R.string.location_updated_message),
        // Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes
        // might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    private void updateLocationUI() {
        List<Address> addressList = LocationUtils.getAddress(HomeActivity.this, mCurrentLocation);
        if (addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);

            editTextSource.setText(address.getFeatureName() + "," + address.getThoroughfare() + "," + address.getLocality() + "," + address.getSubAdminArea() + "," + address.getAdminArea() + "," + address.getPostalCode());
            if (mCurrentLocation != null) {
                if (mGoogleApiClient.isConnected()) {
                    stopLocationUpdates();
                }

            }
        }

    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i("", "Place Selected: " + place.getViewport());
        edtplace.setText(place.getAddress());


      /*  LatLng destlatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

//        MapUtil.getIntance().drawMarker(mGoogleMap,latLng.latitude, latLng.longitude);
//        locationObj.destLat=latLng.latitude;
//        locationObj.destLong=latLng.longitude;
        if(souceLatLng ==null || destlatLng==null){
            return;
        }*/
        MapUtil.getIntance().drawRoute(HomeActivity.this, mGoogleMap, mCurrentLocation, place,place.getViewport());
      //  destlatLng = null;
//        try {
//            JSONObject object = new JSONObject();
//            object.put("xxx", place.getLatLng().latitude);
//            BookingUtil.getInstance().getNearByCabs(HomeActivity.this, object.toString(), new ServerResponseLisners<NearByCabsbean>() {
//                @Override
//                public void onResponse(NearByCabsbean response) {

//
//
//                }
//
//                @Override
//                public void onError(VolleyError error) {
//
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


//        if (!TextUtils.isEmpty(place.getAttributions())){
//            attributionsTextView.setText(Html.fromHtml(place.getAttributions().toString()));
//        }
    }

    @Override
    public void onError(Status status) {
        Log.e("", "onError: Status = " + status.toString());
        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    private void setUpDestination() {
        editTextDest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                autocompleteFragment.setOnPlaceSelectedListener(HomeActivity.this);
                return false;
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
//        initializeMap();

    }

    private void drawMap(LocationObj locationObj) {
        LatLng sydney = new LatLng(locationObj.sourceLat, locationObj.sourceLong);
        mGoogleMap.addMarker(new MarkerOptions().position(sydney));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }
    private View.OnClickListener directionclick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            directionclick();
        }
    };
    private void directionclick(){
        LocationUtils.launchMap(HomeActivity.this,locationObj);
    }

}

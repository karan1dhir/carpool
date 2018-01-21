package com.shopclues.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shopclues.bean.LocationObj;
import com.shopclues.carpool.R;
import com.shopclues.location.LocationUtils;
import com.shopclues.network.PoolNetworkRequest;
import com.shopclues.parser.RouteDateParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manish on 3/8/17.
 */

public class MapUtil {

    LatLngBounds latLngBounds;
    Location source;
    private MapUtil() {

    }

    private static class MapHelper {
        private static final MapUtil INSTANCE = new MapUtil();
    }

    public static MapUtil getIntance() {
        return MapHelper.INSTANCE;
    }

    public void drawRoute(Context context, GoogleMap googleMap, Location source, Place destination, LatLngBounds viewport) {

        this.source=source;
        this.latLngBounds=viewport;
        googleMap.clear();
        getRouteFromGoogle(context, googleMap, getUrl(source, destination));
        drawMarker(googleMap,destination.getLatLng().latitude,destination.getLatLng().longitude);
    }


    private void getRouteFromGoogle(Context context, final GoogleMap googleMap, String url) {

        PoolNetworkRequest.ResponseListener responseListener = new PoolNetworkRequest.ResponseListener<String>() {
            @Override
            public String parseData(String json) {
                return json;
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    List<List<HashMap<String, String>>> rList = new ArrayList<>();
                    rList = RouteDateParser.parseData(jsonObject);
                    if (rList != null && rList.size() > 0) {
                        drawRouteLine(googleMap, rList);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(VolleyError error) {

            }
        };

        PoolNetworkRequest<String> request = new PoolNetworkRequest<>(context, String.class, responseListener);
        request.setRequestMethod(Request.Method.GET);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json; charset=UTF-8");
        request.setHeader(header);
        request.execute(url);


    }

    private String getUrl(Location origin, Place dest) {

        // Origin of route
        String str_origin = "origin=" + origin.getLatitude() + "," + origin.getLongitude();

        // Destination of route
        String str_dest = "destination=" + dest.getLatLng().latitude + "," + dest.getLatLng().longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    public void drawMarker(GoogleMap mGoogleMap, double lat, double lng) {

        LatLng currentLoc = new LatLng(lat, lng);
        double d= setZoomLevel(currentLoc,source);
        Log.d("distance:",""+d);
        if (Utils.objectvalidator(currentLoc))
            return;
        mGoogleMap.addMarker(new MarkerOptions().position(currentLoc).title("Marker in India"));
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom((currentLoc),10));
    }

    private void drawRouteLine(GoogleMap googleMap, List<List<HashMap<String, String>>> rList) {

        if (rList == null) {
            return;
        }

        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        Polyline polyline;

        // Traversing through all the routes
        for (int i = 0; i < rList.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = rList.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(5);
            lineOptions.color(Color.BLUE);

            Log.d("onPostExecute", "onPostExecute lineoptions decoded");

        }
        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            googleMap.addPolyline(lineOptions);
//            googleMap.setLatLngBoundsForCameraTarget(latLngBounds);

//            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        } else {
            Log.d("onPostExecute", "without Polylines drawn");
        }
    }

    private void addMultipleMarker(GoogleMap mGooglemap, ArrayList<LocationObj> listMarkers) {
        if (!Utils.objectvalidator(listMarkers)) {
            return;
        }
        for (int i = 0; i < listMarkers.size(); i++) {
            LatLng currentLoc = new LatLng(listMarkers.get(i).destLat, listMarkers.get(i).destLong);
            if (Utils.objectvalidator(currentLoc))
                return;
            mGooglemap.addMarker(new MarkerOptions().position(currentLoc).title("Marker in India"));
            mGooglemap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(listMarkers.get(i).destLat, listMarkers.get(i).destLong), 10);
        }
    }
    private double setZoomLevel(LatLng current,Location source){

        double R = 6371000f;
        double dLat = (current.latitude - source.getLatitude()) * Math.PI / 180f;
        double dLon = (current.longitude - source.getLongitude()) * Math.PI / 180f;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(current.latitude * Math.PI / 180f) * Math.cos(source.getLongitude() * Math.PI / 180f) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2f * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d;
    }
}

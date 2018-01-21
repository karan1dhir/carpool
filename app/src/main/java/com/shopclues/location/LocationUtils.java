package com.shopclues.location;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;


import com.shopclues.bean.LocationObj;
import com.shopclues.carpool.R;
import com.shopclues.constant.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.wearable.DataMap.TAG;
/**
 * Created by manish on 1/8/17.
 */

public final class LocationUtils {

	// Debugging tag for the application
	public static final String APPTAG = "ShopcluesCarPool";

	// Name of shared preferences repository that stores persistent state
	public static final String SHARED_PREFERENCES = "com.shopclues.location.SHARED_PREFERENCES";

	// Key for storing the "updates requested" flag in shared preferences
	public static final String KEY_UPDATES_REQUESTED = "com.shopclues.location.KEY_UPDATES_REQUESTED";

	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	/*
	 * Constants for location update parameters
	 */
	// Milliseconds per second
	public static final int MILLISECONDS_PER_SECOND = 1000;

	// The update interval
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

	// A fast interval ceiling
	public static final int FAST_CEILING_IN_SECONDS = 1;

	// Update interval in milliseconds
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;

	// A fast ceiling of update intervals, used when the app is visible
	public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* FAST_CEILING_IN_SECONDS;

	// Create an empty string for initializing strings
	public static final String EMPTY_STRING = new String();




	/**
	 * Get the latitude and longitude from the Location object returned by
	 * Location Services.
	 *
	 * @param currentLocation
	 *            A Location object containing the current location
	 * @return The latitude and longitude of the current location, or null if no
	 *         location is available.
	 */
	public static String getLatLng(Context context, Location currentLocation) {
		// If the location is valid
		if (currentLocation != null) {

			// Return the latitude and longitude as strings
			return context.getString(R.string.latitude_longitude,
					currentLocation.getLatitude(),
					currentLocation.getLongitude());
		} else {

			// Otherwise, return the empty string
			return EMPTY_STRING;
		}
	}
	public static List<Address> getAddress(Context context,Location currentLocation) {

		Geocoder gCoder = new Geocoder(context);
		List<Address> addresses=null;
		try {
			addresses = gCoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
			if (addresses != null && addresses.size() > 0) {
				Log.d("", "address======>> " +addresses.toString());
				return addresses;
//			return addresses.get(0).getAddressLine(0)+addresses.get(0).getAddressLine(1) + ","
//					+ addresses.get(0).getAddressLine(2)+" , "+addresses.get(0).getAddressLine(3);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return addresses;
	}
	public static ArrayList<String> getSuggestedLocationList (String input) {
		ArrayList<String> resultList = null;

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();

		try {
			StringBuilder sb = new StringBuilder(Constant.MapCredential.PLACES_API_BASE + Constant.MapCredential.TYPE_AUTOCOMPLETE + Constant.MapCredential.OUT_JSON);
			sb.append("?key=" + Constant.MapCredential.MAP_API_KEY);
			sb.append("&types=(cities)");
			sb.append("&input=" + URLEncoder.encode(input, "utf8"));

			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error processing Places API URL", e);
			return resultList;
		} catch (IOException e) {
			Log.e(TAG, "Error connecting to Places API", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			// Log.d(TAG, jsonResults.toString());

			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			// Extract the Place descriptions from the results
			resultList = new ArrayList<String>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
			}
		} catch (JSONException e) {
			Log.e("", "Cannot process JSON results", e);
		}

		return resultList;
	}

	public static void launchMap(Context context, LocationObj locationObj){
		String query = "google.navigation:q="+String.valueOf( locationObj.destLat)+","+String.valueOf(locationObj.destLong);
		Uri gmmIntentUri = Uri.parse(query);
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
		mapIntent.setPackage("com.google.android.apps.maps");
		context.startActivity(mapIntent);

	}
}

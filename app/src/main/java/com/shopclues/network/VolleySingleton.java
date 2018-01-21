package com.shopclues.network;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by manish on 4/9/16.
 */
public class VolleySingleton {

	private static VolleySingleton instance;
	private RequestQueue requestQueue;
	private ImageLoader imageLoader;
	private Context mContext;
	ImageLoader mImageLoader;

	private VolleySingleton(Context context) {
		this.mContext = context;
		this.requestQueue = Volley.newRequestQueue(context);

	}

	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this.requestQueue,
					new LruBitmapCache());
		}
		return this.mImageLoader;
	}

	public static VolleySingleton getInstance(Context context) {
		if (instance == null) {
			instance = new VolleySingleton(context);
		}
		return instance;
	}

	public RequestQueue getRequestQueue() {

		if (requestQueue == null) {
			Cache cache = new DiskBasedCache(mContext.getCacheDir(), 1024 * 1024 * 16); // 16mb
			// cap
			Network network = new BasicNetwork(new HurlStack());
			requestQueue = new RequestQueue(cache, network);
			requestQueue.start();

		}

		return requestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag("App");
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (requestQueue != null) {
			requestQueue.cancelAll(tag);
		}
	}



	public static void removeCacheData(String url, Activity activity) {

		VolleySingleton.getInstance(activity).getRequestQueue().getCache().remove(url);

	}

	private static long getMinutesDifference(long timeStart, long timeStop) {
		// TODO Auto-generated method stub

		long diff = timeStop - timeStart;
		// int hours = (int) ((diff / (1000*60*60)) % 24);
		long diffMinutes = diff / (60 * 1000);
		return diffMinutes;

	}


}

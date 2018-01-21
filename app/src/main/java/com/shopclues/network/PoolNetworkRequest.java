package com.shopclues.network;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by manish on 4/9/16.
 */
public class PoolNetworkRequest<T> {

    protected final Class<T> clazz;
    private ResponseListener<T> listener;
    private String jsonBody;
    private Context context;
    private Map<String, String> header = null;
    private boolean isSecureRequest = false;
    private int requestMethod = Request.Method.GET;
    private String url;
    private Map<String, String> params;
    private int retryCountForTtl = 0;
    private int retryCountForNetworkError = 0;
    private static final int REQUEST_TIMEOUT = 10000;
    private static final int MAX_RETRIES = 2;
    private static final int BACKOFF_MULTIPLIER = 2;

    public interface ResponseListener<T> {
        T parseData(String json);
        void onResponse(T response);
        void onError(VolleyError error);
    }

    /**
     * Construct a new @PoolNetworkRequest
     *
     * @param context  activity context
     * @param clazz    bean class type
     * @param listener response listener
     */
    public PoolNetworkRequest(Context context, Class<T> clazz, ResponseListener<T> listener) {
        this.clazz = clazz;
        this.context = context;
        this.listener = listener;
    }

    public void setSecureRequest(boolean isSecureRequest) {
        this.isSecureRequest = isSecureRequest;
    }

    /**
     * set HTTP request method
     *
     * @param requestMethod Request.Method.POST, Request.Method.GET, Request.Method.PUT, Request.Method.DELETE etc.
     */
    public void setRequestMethod(int requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * @param header additional header for request
     */
    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    /**
     * execute http request to fetch data from server
     *
     * @param url api url to fetch data
     */
    public void execute(String url) {

        this.url = url;

        Log.d(this.getClass().getName(), "Request URL: " + url);
        Log.d(this.getClass().getName(), "Request Body: " + jsonBody);

        ErrorListener errorListener = new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (retryCountForNetworkError < 3) {
                    retryCountForNetworkError++;
                    Log.d("", "retryCountForNetworkError: " + retryCountForNetworkError + "");
                    execute(PoolNetworkRequest.this.url);
                } else if (listener != null) {
                    listener.onError(error);
                }
            }
        };

        Request<String> request = new Request<String>(requestMethod, url, errorListener) {

            @Override
            public byte[] getBody() throws AuthFailureError {
                if (jsonBody != null) {
                    return jsonBody.getBytes();
                }

                return super.getBody();
            }

            @Override
            protected void deliverResponse(String response) {
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    Log.d("", "Response: " + json);

                    if (listener != null) {
                        final T parseData = listener.parseData(json);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onResponse(parseData);
                                }
                            }
                        });

                        return Response.success("success", HttpHeaderParser.parseCacheHeaders(response));
                    }
                } catch (Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    return Response.error(new VolleyError(response));
                }
                return Response.success("success", HttpHeaderParser.parseCacheHeaders(response));

            }

            @Override
            public Priority getPriority() {
                Priority mPriority = Priority.HIGH;
                return mPriority;
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                if (params != null) {
                    return params;
                } else {
                    return super.getParams();
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();

                if (headers == null || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<>();
                }

                headers.put("Content-Type", "application/json");
                if (header != null) {
                    headers.putAll(header);
                }

                setLoggerHeader(headers);

                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT, MAX_RETRIES, BACKOFF_MULTIPLIER));
        request.setShouldCache(false);
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * set request body
     *
     * @param body request body
     */

    public void setBody(String body) {
        this.jsonBody = body;
    }

    /**
     * @param params key value parameter for request
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    private void setLoggerHeader(Map<String, String> header) {
        try {
            header.put("device_model", Build.MODEL);

        } catch (Exception exc) {
            exc.printStackTrace();
        }

        try {
            header.put("os_version", Build.VERSION.RELEASE);

        } catch (Exception exc) {
            exc.printStackTrace();
        }


    }
}

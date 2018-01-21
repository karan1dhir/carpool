package com.shopclues.lisners;

import com.android.volley.VolleyError;

/**
 * Created by root on 2/8/17.
 */

public interface ServerResponseLisners <T>{
    public void onResponse(T response);
    public void onError(VolleyError error);
}

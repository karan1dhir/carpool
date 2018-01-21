package com.shopclues.util;

import android.content.Context;

import com.android.volley.VolleyError;
import com.shopclues.bean.NearByCabsbean;
import com.shopclues.lisners.ServerResponseLisners;
import com.shopclues.network.PoolNetworkRequest;

/**
 * Created by manish on 2/8/17.
 */

public class BookingUtil {

    private static class BookingHelper {

        private static final BookingUtil INSTANCE = new BookingUtil();
    }

    public static BookingUtil getInstance() {

        return BookingHelper.INSTANCE;
    }

    public void getNearByCabs(Context context, String requestJson, ServerResponseLisners serverResponseLisners) {


        PoolNetworkRequest.ResponseListener responseListener = new PoolNetworkRequest.ResponseListener<NearByCabsbean>() {

            @Override
            public NearByCabsbean parseData(String json) {
                return null;
            }

            @Override
            public void onResponse(NearByCabsbean response) {

            }

            @Override
            public void onError(VolleyError error) {

            }
        };

    }


}

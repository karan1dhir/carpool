package com.shopclues.location;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by manish on 1/8/17.
 */

public class GetLocationService extends IntentService {


    public GetLocationService() {
        super("GetLocationService");
    }

    public GetLocationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {



    }


}

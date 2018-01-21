package com.shopclues.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Manish on 2/8/17.
 */

public class LocationObj implements Parcelable {


    public double sourceLat;
    public double sourceLong;

    public double destLat;
    public double destLong;



    public LocationObj() {

    }

    protected LocationObj(Parcel in) {
        this.sourceLat = in.readDouble();
        this.sourceLong = in.readDouble();
        this.destLat = in.readDouble();
        this.destLong = in.readDouble();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.sourceLat);
        dest.writeDouble(this.sourceLong);
        dest.writeDouble(this.destLat);
        dest.writeDouble(this.destLong);


    }

    public static final Parcelable.Creator<LocationObj> CREATOR = new Parcelable.Creator<LocationObj>() {
        @Override
        public LocationObj createFromParcel(Parcel source) {
            return new LocationObj(source);
        }

        @Override
        public LocationObj[] newArray(int size) {
            return new LocationObj[size];
        }
    };

}

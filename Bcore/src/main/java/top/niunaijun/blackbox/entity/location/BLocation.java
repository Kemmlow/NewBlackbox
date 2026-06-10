package top.niunaijun.blackbox.entity.location;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class BLocation implements Parcelable {

    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private double mAltitude = 0.0f;
    private float mSpeed = 0.0f;
    private float mBearing = 0.0f;
    private float mAccuracy = 0.0f;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeDouble(this.mAltitude);
        dest.writeFloat(this.mSpeed);
        dest.writeFloat(this.mBearing);
        dest.writeFloat(this.mAccuracy);
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public BLocation() {
    }

    public BLocation(double latitude, double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public BLocation(Parcel in) {
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mAltitude = in.readDouble();
        this.mAccuracy = in.readFloat();
        this.mSpeed = in.readFloat();
        this.mBearing = in.readFloat();
    }

    public boolean isEmpty() {
        return mLatitude == 0 && mLongitude == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BLocation location = (BLocation) o;
        return Double.compare(location.mLatitude, mLatitude) == 0 &&
                Double.compare(location.mLongitude, mLongitude) == 0 &&
                Double.compare(location.mAltitude, mAltitude) == 0 &&
                Float.compare(location.mSpeed, mSpeed) == 0 &&
                Float.compare(location.mBearing, mBearing) == 0 &&
                Float.compare(location.mAccuracy, mAccuracy) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mLatitude, mLongitude, mAltitude, mSpeed, mBearing, mAccuracy);
    }

    public static final Parcelable.Creator<BLocation> CREATOR = new Parcelable.Creator<BLocation>() {
        @Override
        public BLocation createFromParcel(Parcel source) {
            return new BLocation(source);
        }

        @Override
        public BLocation[] newArray(int size) {
            return new BLocation[size];
        }
    };

    public Location convert2SystemLocation() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(mLatitude);
        location.setLongitude(mLongitude);
        location.setAltitude(mAltitude);
        location.setSpeed(mSpeed);
        location.setBearing(mBearing);
        location.setAccuracy(mAccuracy > 0 ? mAccuracy : 10.0f);
        location.setTime(System.currentTimeMillis());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(android.os.SystemClock.elapsedRealtimeNanos());
        }
        Bundle extraBundle = new Bundle();
        int satelliteCount = 12;
        extraBundle.putInt("satellites", satelliteCount);
        location.setExtras(extraBundle);
        return location;
    }
}

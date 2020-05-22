package gr.auth.csd.firstresponder.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;

public class Alert implements Parcelable {
    public String notes;
    public HashMap<String, Boolean> requiredSkills; // STOP_HEAVY_BLEEDING, TREATING_SHOCK, CPR, AED
    public GeoPoint coordinates;

    Alert() {}

    protected Alert(Parcel in) {
        coordinates = new GeoPoint(in.readDouble(), in.readDouble());
        notes = in.readString();
        requiredSkills = (HashMap<String, Boolean>) in.readSerializable();
    }

    public static final Creator<Alert> CREATOR = new Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel in) {
            return new Alert(in);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(coordinates.getLatitude());
        dest.writeDouble(coordinates.getLongitude());
        dest.writeString(notes);
        dest.writeSerializable(requiredSkills);
    }
}

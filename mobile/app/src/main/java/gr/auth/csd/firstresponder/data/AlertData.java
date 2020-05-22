package gr.auth.csd.firstresponder.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AlertData implements Parcelable {
    public Alert alert;
    public int secondsOfDrivingRequired;

    public AlertData() {}

    protected AlertData(Parcel in) {
        alert = in.readParcelable(Alert.class.getClassLoader());
        secondsOfDrivingRequired = in.readInt();
    }

    public static final Creator<AlertData> CREATOR = new Creator<AlertData>() {
        @Override
        public AlertData createFromParcel(Parcel in) {
            return new AlertData(in);
        }

        @Override
        public AlertData[] newArray(int size) {
            return new AlertData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(alert, flags);
        dest.writeInt(secondsOfDrivingRequired);
    }
}

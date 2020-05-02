package gr.auth.csd.firstresponder.data;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;

public class Responder {

    private String firstName;
    private String lastName;
    private String token;
    private HashMap<String, Boolean> skills = new HashMap<>(); // STOP_HEAVY_BLEEDING, TREATING_SHOCK, CPR, AED
    private HashMap<String, Object> lastKnownLocation = new HashMap<>();
    private boolean busy;


    public Responder() {}

    public Responder(String firstName, String lastName, HashMap<String, Boolean> skills) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = "";
        this.skills = skills;
        this.lastKnownLocation.put("location", new GeoPoint(0.0, 0.0));
        this.lastKnownLocation.put("time", "0:0");
        this.busy = false;
    }

    public Responder(String firstName, String lastName, String token, HashMap<String, Boolean> skills, HashMap<String , Object> lastKnownLocation, boolean busy) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.skills = skills;
        this.lastKnownLocation = lastKnownLocation;
        this.busy = busy;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getToken() {
        return token;
    }

    public HashMap<String, Boolean> getSkills() {
        return skills;
    }

    public HashMap<String, Object> getLastKnownLocation() {
        return lastKnownLocation;
    }

    public boolean getBusy() {
        return busy;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setSkills(HashMap<String, Boolean> skills) {
        this.skills = skills;
    }

    public void setLastKnownLocation(HashMap<String, Object> lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }
}

package gr.auth.csd.firstresponder.helpers;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseFirestoreInstance {

    public static FirebaseFirestore Create() {

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setHost("10.0.2.2:8080")
                .setSslEnabled(false)
                .setPersistenceEnabled(false)
                .build();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        return db;
    }
}

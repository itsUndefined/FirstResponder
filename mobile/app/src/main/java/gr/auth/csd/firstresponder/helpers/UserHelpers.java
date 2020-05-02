package gr.auth.csd.firstresponder.helpers;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserHelpers {
    public static void UpdateFirebaseInstanceId(String token) {
        String uid = FirebaseAuth.getInstance().getUid();
        if(uid != null) {
            FirebaseFirestore db = FirebaseFirestoreInstance.Create();
            db.collection("users").document(uid)
                .update("token", token);
        }
    }
}

package gr.auth.csd.firstresponder.helpers;

import com.google.firebase.functions.FirebaseFunctions;

/**
 * Gets the cloud functions instance.
 */
public class FirebaseFunctionsInstance {
    public static FirebaseFunctions Create() {
        FirebaseFunctions functionsInstance = FirebaseFunctions.getInstance("europe-west3");
        //functionsInstance.useFunctionsEmulator("http://10.0.2.2:5001");
        return functionsInstance;
    }
}

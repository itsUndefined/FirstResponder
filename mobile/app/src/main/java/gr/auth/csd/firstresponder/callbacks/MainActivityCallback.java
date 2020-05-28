package gr.auth.csd.firstresponder.callbacks;

public interface MainActivityCallback {
    void phoneVerification(String string);
    void phoneCodeVerification(String string);
    void verificationAborted();
}

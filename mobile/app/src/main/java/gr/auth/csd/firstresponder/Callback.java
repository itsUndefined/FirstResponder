package gr.auth.csd.firstresponder;

public interface Callback {
    void phoneVerification(String string);
    void phoneCodeVerification(String string);
    void reSendCode();
}

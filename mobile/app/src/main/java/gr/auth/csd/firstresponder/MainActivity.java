package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import gr.auth.csd.firstresponder.callbacks.MainActivityCallback;
import gr.auth.csd.firstresponder.fragments.CodeSubmitFragment;
import gr.auth.csd.firstresponder.fragments.LogInFragment;
import gr.auth.csd.firstresponder.fragments.RegisterFragment;
import gr.auth.csd.firstresponder.fragments.StartFragment;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * The class contains the login and register functionality.
 */
public class MainActivity extends AppCompatActivity implements MainActivityCallback {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private boolean mVerificationInProgress = false;
    private String mCurrentlyVerifyingPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestoreInstance.Create();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                signInWithPhoneAuthCredential(phoneAuthCredential);
                mVerificationInProgress = false;
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(MainActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(MainActivity.this, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseNetworkException) {
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                mVerificationId = verificationId;
                mResendToken = token;
                Log.d(TAG, "onCodeSent:" + verificationId);
                if(!mVerificationInProgress) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_activity_fragment_container, new CodeSubmitFragment());
                    fragmentTransaction.commit();
                }
                mVerificationInProgress = true;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String verificationId) {
                super.onCodeAutoRetrievalTimeOut(verificationId);
                Log.d(TAG, "onCodeAutoRetrievalTimeOut:" + verificationId);
                mResendToken = null;
                if (mVerificationInProgress) {
                    mVerificationInProgress = false;
                    mCurrentlyVerifyingPhone = null;
                    Toast.makeText(MainActivity.this, "Verification timed out", Toast.LENGTH_SHORT).show();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_activity_fragment_container, new LogInFragment());
                    fragmentTransaction.commit();
                }
            }
        };

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else {
            updateUI(currentUser);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("key_resend_token", mResendToken);
        outState.putBoolean("key_verify_in_progress", mVerificationInProgress);
        outState.putString("key_currently_verifying_phone", mCurrentlyVerifyingPhone);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mResendToken = savedInstanceState.getParcelable("key_resend_token");
        mVerificationInProgress = savedInstanceState.getBoolean("key_verify_in_progress");
        mCurrentlyVerifyingPhone = savedInstanceState.getString("key_currently_verifying_phone");
        if (mVerificationInProgress) {
           startPhoneNumberVerification(mCurrentlyVerifyingPhone);
        }
    }

    public void verifyPhoneNumberWithCode(String code) {
        if(mVerificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void logIn(String phoneNumber){
        if (mResendToken == null) {
            startPhoneNumberVerification(phoneNumber);
        } else {
            forceStartPhoneNumberVerification(phoneNumber, mResendToken);
        }
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
        mCurrentlyVerifyingPhone = phoneNumber;
    }

    private void forceStartPhoneNumberVerification(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        );
        mCurrentlyVerifyingPhone = phoneNumber;
    }

    /**
     * If the user is sign-in but he does not have a document to the database then the app navigates
     * to the register fragment.
     * If the user is sign-in and has a document to the database then the app navigates to the
     * dashboard activity.
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    mVerificationInProgress = false;
                    mCurrentlyVerifyingPhone = null;
                    currentUser = mAuth.getCurrentUser();

                    db.collection("users").document(currentUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (Objects.requireNonNull(documentSnapshot).exists()) {
                                        Log.d(TAG, "Document exists!");
                                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.d(TAG, "Document does not exist!");
                                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                        fragmentTransaction.replace(R.id.main_activity_fragment_container, new RegisterFragment());
                                        fragmentTransaction.commit();
                                    }
                                } else {
                                    Log.d(TAG, "Failed with: ", task.getException());
                                }
                            }
                        });
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(MainActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * If the user is sign-in but he does not have a document to the database then the app navigates
     * to the register fragment.
     * If the user is sign-in and has a document to the database then the app navigates to the
     * dashboard activity.
     */
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (Objects.requireNonNull(documentSnapshot).exists()) {
                                    Log.d(TAG, "Document exists!");
                                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.d(TAG, "Document does not exist!");
                                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.main_activity_fragment_container, new RegisterFragment());
                                    fragmentTransaction.commit();
                                }
                            } else {
                                Log.d(TAG, "Failed with: ", task.getException());
                            }
                        }
                    });
        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.main_activity_fragment_container, new StartFragment());
            fragmentTransaction.commit();
        }
    }

    @Override
    public void phoneVerification(String phoneNumber) {
        logIn(phoneNumber);
    }

    @Override
    public void phoneCodeVerification(String code) {
        verifyPhoneNumberWithCode(code);
    }

    @Override
    public void verificationAborted() {
        mVerificationInProgress = false;
        mCurrentlyVerifyingPhone = null;
    }
}

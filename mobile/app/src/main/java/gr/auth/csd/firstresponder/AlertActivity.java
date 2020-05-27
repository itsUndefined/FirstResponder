package gr.auth.csd.firstresponder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gr.auth.csd.firstresponder.data.AlertData;
import gr.auth.csd.firstresponder.helpers.FirebaseFunctionsInstance;
import gr.auth.csd.firstresponder.services.IncomingAlertService;
import gr.auth.csd.firstresponder.services.OngoingMissionService;

import static gr.auth.csd.firstresponder.services.OngoingMissionService.START_MISSION;

/**
 * Creates the alert screen and the user can see the mission. Also user has the choice to accept or
 * reject the mission, to see the mission details, requirements and the location in a gps app. After
 * user accepts the mission, the activity will be active until the user is near to the location.
 */
public class AlertActivity extends AppCompatActivity implements IncomingAlertService.Callback, OngoingMissionService.Callback {

    private AlertData alertData;
    private Boolean accepted = false;

    private IncomingAlertService incomingAlertService;
    private OngoingMissionService ongoingMissionService;

    private ServiceConnection connection1;
    private ServiceConnection connection2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
            alertData = savedInstanceState.getParcelable(DISPLAY_ALERT);
            accepted = savedInstanceState.getBoolean("accepted");
        } else {
            alertData = getIntent().getParcelableExtra(DISPLAY_ALERT);
            accepted = getIntent().getBooleanExtra("accepted", false);
        }

        if (accepted) {
            Intent ongoingIntent = new Intent(getApplicationContext(), OngoingMissionService.class);
            startService(ongoingIntent);
            bindToOngoingService(ongoingIntent);
        }

        KeyguardManager manager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(Objects.requireNonNull(manager).isKeyguardLocked()) {
            turnScreenOnAndKeyguardOff();
        }

        setContentView(R.layout.activity_alert);

        final IncomingAlertService.Callback callback = this;

        if (!accepted) {
            connection1 = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IncomingAlertService.LocalBinder binding = (IncomingAlertService.LocalBinder) service;
                    incomingAlertService = binding.getService();
                    incomingAlertService.registerClient(callback);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    incomingAlertService = null;
                }
            };
            Intent alertIntent = new Intent(this, IncomingAlertService.class);
            bindService(alertIntent, connection1, 0);
        }


        RadioGroup heavyBleedingRadio = this.findViewById(R.id.heavy_bleeding_radio_group);
        RadioGroup treatShockRadio = this.findViewById(R.id.treat_shock_radio_group);
        RadioGroup cprRadio = this.findViewById(R.id.cpr_radio_group);
        RadioGroup aedRadio = this.findViewById(R.id.aed_radio_group);

        TextInputLayout estimatedTimeText = this.findViewById(R.id.estimated_time_value_text);
        Objects.requireNonNull(estimatedTimeText.getEditText()).setEnabled(false);
        estimatedTimeText.getEditText().setText(Integer.toString(alertData.secondsOfDrivingRequired) + " δευτερόλεπτα");

        TextInputLayout alertAddressText = this.findViewById(R.id.alert_address);
        Objects.requireNonNull(alertAddressText.getEditText()).setEnabled(false);
        alertAddressText.getEditText().setText(alertData.alert.address);

        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("STOP_HEAVY_BLEEDING"))) {
            heavyBleedingRadio.check(R.id.heavy_bleeding_yes);
        } else {
            heavyBleedingRadio.check(R.id.heavy_bleeding_no);
        }
        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("TREATING_SHOCK"))) {
            treatShockRadio.check(R.id.treat_shock_yes);
        } else {
            treatShockRadio.check(R.id.treat_shock_no);
        }
        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("CPR"))) {
            cprRadio.check(R.id.cpr_yes);
        } else {
            cprRadio.check(R.id.cpr_no);
        }
        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("AED"))) {
            aedRadio.check(R.id.aed_yes);
        } else {
            aedRadio.check(R.id.aed_no);
        }

        Button goToMapsButton = this.findViewById(R.id.alert_maps);
        goToMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String coordinates = alertData.alert.coordinates.getLatitude() + "," + alertData.alert.coordinates.getLongitude();
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + coordinates + "&mode=c");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(getApplicationContext(),"No maps app found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button acceptMissionButton = this.findViewById(R.id.button_accept_mission);
        final Button rejectMissionButton = this.findViewById(R.id.button_reject_mission);

        if (accepted) {
            acceptMissionButton.setVisibility(View.GONE);
            rejectMissionButton.setVisibility(View.GONE);
        }


        Button missionEndButton = findViewById(R.id.button_end_mission);
        missionEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(connection2);
                connection2 = null;
                Intent ongoingIntent = new Intent(getApplicationContext(), OngoingMissionService.class);
                stopService(ongoingIntent);
                finishAffinity();
            }
        });

        acceptMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFunctions functions = FirebaseFunctionsInstance.Create();

                Map<String, Object> data = new HashMap<>();
                data.put("alertId", alertData.alertId);
                data.put("status", "accepted");

                FirebaseFunctions functionsInstance = FirebaseFunctionsInstance.Create();

                functionsInstance.getHttpsCallable("updateUserStatus").call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if(!task.isSuccessful()) {
                            return;
                        }
                        if(task.getResult().getData().equals(Boolean.FALSE)) {
                            Toast.makeText(getApplicationContext(), "Η αποστολή δεν υπάρχει πλέον", Toast.LENGTH_SHORT).show();
                            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            Objects.requireNonNull(manager).cancelAll();
                            finishAffinity();
                            return;
                        }

                        incomingAlertService.terminateIncomingCall(false);
                        unbindService(connection1);
                        connection1 = null;

                        Intent ongoingIntent = new Intent(getApplicationContext(), OngoingMissionService.class);
                        ongoingIntent.setAction(START_MISSION);
                        ongoingIntent.putExtra(DISPLAY_ALERT, alertData);
                        startService(ongoingIntent);

                        bindToOngoingService(ongoingIntent);

                        accepted = true;
                        acceptMissionButton.setVisibility(View.GONE);
                        rejectMissionButton.setVisibility(View.GONE);
                    }
                });
            }
        });

        rejectMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFunctions functions = FirebaseFunctionsInstance.Create();

                Map<String, Object> data = new HashMap<>();
                data.put("alertId", alertData.alertId);
                data.put("status", "rejected");

                FirebaseFunctions functionsInstance = FirebaseFunctionsInstance.Create();

                functionsInstance.getHttpsCallable("updateUserStatus").call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        Toast.makeText(getApplicationContext(), "Η αποστολή απορρίφθηκε.", Toast.LENGTH_SHORT).show();
                        incomingAlertService.terminateIncomingCall(true);
                    }
                });
            }
        });

        Button alertDetailsButton = this.findViewById(R.id.alert_details_button);
        alertDetailsButton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AlertActivity.this);
                builder.setTitle("Λεπτομέρειες Αποστολής");
                builder.setMessage(alertData.alert.notes);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }));
    }

    public void turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }  else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable(DISPLAY_ALERT, alertData);
        outState.putBoolean("accepted", accepted);
    }

    @Override
    public void terminate() {
        unbindService(connection1);
        connection1 = null;
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection1 != null) {
            unbindService(connection1);
        }

        if (connection2 != null) {
            unbindService(connection2);
        }

    }

    public static String DISPLAY_ALERT = "gr.auth.csd.firstresponder.DisplayAlert";


    @Override
    public void onArrival() {
        Button missionEndButton = findViewById(R.id.button_end_mission);
        missionEndButton.setVisibility(View.VISIBLE);
    }

    private void bindToOngoingService(Intent intent) {
        final OngoingMissionService.Callback callback = this;
        connection2 = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                OngoingMissionService.LocalBinder binding = (OngoingMissionService.LocalBinder) service;
                ongoingMissionService = binding.getService();
                ongoingMissionService.registerClient(callback);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                ongoingMissionService = null;
            }
        };
        bindService(intent, connection2, 0);
    }
}

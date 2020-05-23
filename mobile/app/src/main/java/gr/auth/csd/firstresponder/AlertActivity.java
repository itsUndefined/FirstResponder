package gr.auth.csd.firstresponder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import gr.auth.csd.firstresponder.data.AlertData;

public class AlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
            return;
        }

        if (getIntent().hasExtra(DISPLAY_ALERT)) {
            KeyguardManager manager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if(Objects.requireNonNull(manager).isKeyguardLocked()) {
                turnScreenOnAndKeyguardOff();
            }
        } else {
            finishAffinity(); // Activity cannot work without display_alert data
        }

        setContentView(R.layout.activity_alert);

        final AlertData alertData = getIntent().getParcelableExtra(DISPLAY_ALERT);

        RadioGroup heavyBleedingRadio = this.findViewById(R.id.heavy_bleeding_radio_group);
        RadioGroup treatShockRadio = this.findViewById(R.id.treat_shock_radio_group);
        RadioGroup cprRadio = this.findViewById(R.id.cpr_radio_group);
        RadioGroup aedRadio = this.findViewById(R.id.aed_radio_group);

        TextView estimatedTimeText = this.findViewById(R.id.estimated_time_value_text);
        estimatedTimeText.setText(Integer.toString(alertData.secondsOfDrivingRequired) + " δευτερόλεπτα");

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

        Button rejectMissionButton = this.findViewById(R.id.button_reject_mission);
        rejectMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Η αποστολή απορρίφθηκε.", Toast.LENGTH_SHORT).show();
                NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                Objects.requireNonNull(manager).cancelAll();
                finishAffinity();
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
        }
        else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    public static String DISPLAY_ALERT = "gr.auth.csd.firstresponder.DisplayAlert";

}

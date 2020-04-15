package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import gr.auth.csd.firstresponder.data.Responder;

public class SettingsActivity extends AppCompatActivity {

    Responder user = new Responder("John", "Doe", "00302310123456");
    private String userName = user.getName();
    private String userSurname = user.getSurname();
    private String userPhoneNumber = user.getPhoneNumber();

    private Button saveChangesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ImageView returnToLogInActivityArrow = findViewById(R.id.returnToLogInFromSettings);
        returnToLogInActivityArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });

        initializePlainTexts();
        initializeButton();

    }

    private void initializePlainTexts(){
        TextView name = (TextView) findViewById(R.id.namePlainText);
        TextView surname = (TextView) findViewById(R.id.surnamePlainText);
        TextView phoneNumber = (TextView) findViewById(R.id.phoneNumberPhone);

        name.setText(user.getName());
        surname.setText(user.getSurname());
        phoneNumber.setText(user.getPhoneNumber());

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveChangesButton.setVisibility(View.VISIBLE);
            }
            //public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            //public void afterTextChanged(Editable s) { saveChangesButton.setVisibility(View.VISIBLE); }
            public void afterTextChanged(Editable s) { }
        });

        surname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveChangesButton.setVisibility(View.VISIBLE);
            }
            //public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            //public void afterTextChanged(Editable s) { saveChangesButton.setVisibility(View.VISIBLE); }
            public void afterTextChanged(Editable s) { }
        });

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveChangesButton.setVisibility(View.VISIBLE);
            }
            //public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            //public void afterTextChanged(Editable s) { saveChangesButton.setVisibility(View.VISIBLE); }
            public void afterTextChanged(Editable s) { }
        });
    }

    // Checkboxes' Event Listeners.
    public void reverseHeavyBleedingCheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.STOP_HEAVY_BLEEDING);
    }
    public void reverseTreatShockCheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.TREATING_SHOCK);
    }
    public void reverseCPRcheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.CPR);
    }
    public void reverseAEDcheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.USE_AED);
    }

    // Save Changes Button's event listeners.
    public void initializeButton(){
        saveChangesButton = (Button) findViewById(R.id.settingActivitySaveChangesButton);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // Save Changes button's "OnClick" Event Listener
            public void onClick(View v) {
                if (!userName.equals(user.getName())){
                    user.setName(userName);
                }
                if(!userSurname.equals(user.getSurname())){
                    user.setSurname(userSurname);
                }
                if(!userPhoneNumber.equals(user.getPhoneNumber())) {
                    user.setPhoneNumber(userPhoneNumber);
                }
            }
        });

        if(!userName.equals(user.getName()) || !userSurname.equals(user.getSurname()) || !userPhoneNumber.equals(user.getPhoneNumber())){
            saveChangesButton.setVisibility(View.VISIBLE);
            Log.i("SettingsActivity", "An attribute has changed.");
        }
    }

}
package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import static androidx.constraintlayout.widget.Constraints.TAG;

import gr.auth.csd.firstresponder.data.Responder;

public class SettingsActivity extends AppCompatActivity {

    private Responder user; // = new Responder("John", "Doe", "00302310123456");

    private FirebaseUser currentUser;
    private FirebaseFirestore database;

    private String userName;
    private String userSurname;
    private Boolean stopHeavyBleeding;
    private Boolean treatShock;
    private Boolean cpr;
    private Boolean aed;

    private Button saveChangesButton;
    private final TextView name = (TextView) findViewById(R.id.namePlainText);
    private final TextView surname = (TextView) findViewById(R.id.surnamePlainText);
    private final CheckBox heavyBleedingCheckbox = (CheckBox) findViewById(R.id.heavyBleedingCheckBox);
    private final CheckBox treatShockCheckbox = (CheckBox) findViewById(R.id.treatShockCheckbox);
    private final CheckBox cprCheckbox = (CheckBox) findViewById(R.id.cprCheckbox);
    private final CheckBox aedCheckbox = (CheckBox) findViewById(R.id.aesCheckbox);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ImageView returnToLogInActivityArrow = findViewById(R.id.returnToLogInFromSettings);
        returnToLogInActivityArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AlertsActivity.class);
                startActivity(intent);
            }
        });



        readFieldsFromDatabase();
        initializeViews();
        initializeButton();

    }

    private void readFieldsFromDatabase(){
        database = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        final DocumentReference docRef = database.collection("users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        user = new Responder(document.get("firstName").toString(), document.get("lastName").toString());
                        //TODO: change database checkbox values (skills) from numbers to booleans.
                        //user.setKnownSkill(Responder.skill.STOP_HEAVY_BLEEDING, document.get("skills[0]"));
                        //user.setKnownSkill(Responder.skill.TREATING_SHOCK, document.get("skills[1]"));
                        //user.setKnownSkill(Responder.skill.CPR, document.get("skills[2]"));
                        //user.setKnownSkill(Responder.skill.AED, document.get("skills[3]"));

                        //Saving the same values in back up variables, so as to check whether
                        //at least one of the attributes has changed.
                        userName = user.getName();
                        userSurname = user.getSurname();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void saveChangesToDatabase(){

        if (name.getText().toString().equals("") || surname.getText().toString().equals("")) {
            if (name.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Invalid first name", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Invalid last name", Toast.LENGTH_SHORT).show();
            }
        } else {
            HashMap<String, Object> userMap = new HashMap<>();
            ArrayList<Boolean> skills = new ArrayList<>();
            userMap.put("firstName", user.getName());
            userMap.put("lastName", user.getSurname());

            skills.add(user.canStopHeavyBleeding());
            skills.add(user.canTreatShock());
            skills.add(user.canPerformCPR());
            skills.add(user.canUseAED());

            userMap.put("skills", skills);
            database.collection("users").document(currentUser.getUid())
                    .update(userMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            Intent intent = new Intent(SettingsActivity.this, AlertsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }

    }

    private void initializeViews(){

        name.setText(user.getName());
        surname.setText(user.getSurname());
        heavyBleedingCheckbox.setSelected(user.canStopHeavyBleeding());
        treatShockCheckbox.setSelected(user.canTreatShock());
        cprCheckbox.setSelected(user.canPerformCPR());
        aedCheckbox.setSelected(user.canUseAED());

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




    }

    // Checkboxes' Event Listeners.
    public void reverseHeavyBleedingCheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.STOP_HEAVY_BLEEDING);
        saveChangesButton.setVisibility(View.VISIBLE);
    }
    public void reverseTreatShockCheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.TREATING_SHOCK);
        saveChangesButton.setVisibility(View.VISIBLE);
    }
    public void reverseCPRcheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.CPR);
        saveChangesButton.setVisibility(View.VISIBLE);
    }
    public void reverseAEDcheckbox(View view) {
        user.changeKnownSkillStatus(Responder.skill.AED);
        saveChangesButton.setVisibility(View.VISIBLE);
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

                //Changes for each checkbox are handled by its corresponding event listener above.


                saveChangesToDatabase();

            }
        });

    }

}
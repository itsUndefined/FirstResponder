package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import gr.auth.csd.firstresponder.data.Responder;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;

public class BasicSettingsFragment extends Fragment {

    private Responder user;

    private FirebaseUser currentUser;
    private FirebaseFirestore database;

    private TextView name;
    private TextView surname;
    private CheckBox heavyBleedingCheckbox;
    private CheckBox treatShockCheckbox;
    private CheckBox cprCheckbox;
    private CheckBox aedCheckbox;
    private Button saveChangesButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic_settings, container, false);

        database = FirebaseFirestoreInstance.Create();

        name = view.findViewById(R.id.namePlainText);
        surname = view.findViewById(R.id.surnamePlainText);
        heavyBleedingCheckbox = view.findViewById(R.id.heavyBleedingCheckBox);
        treatShockCheckbox = view.findViewById(R.id.treatShockCheckbox);
        cprCheckbox = view.findViewById(R.id.cprCheckbox);
        aedCheckbox = view.findViewById(R.id.aesCheckbox);
        saveChangesButton = view.findViewById(R.id.settingSaveChangesButton);

        user = new Responder();

        ImageView returnToLogInActivityArrow = view.findViewById(R.id.basicSettingsBack);
        returnToLogInActivityArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                startActivity(intent);
            }
        });

        readFieldsFromDatabase();
        initializeButton();

        return view;
    }

    private void readFieldsFromDatabase(){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference docRef = database.collection("users").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(Responder.class);
                initializeViews();
            }
        });
    }

    private void initializeViews(){
        name.setText(user.getFirstName());
        surname.setText(user.getLastName());
        heavyBleedingCheckbox.setChecked(user.getSkills().get("STOP_HEAVY_BLEEDING"));
        treatShockCheckbox.setChecked(user.getSkills().get("TREATING_SHOCK"));
        cprCheckbox.setChecked(user.getSkills().get("CPR"));
        aedCheckbox.setChecked(user.getSkills().get("AED"));
    }

    private void initializeButton() {
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setFirstName(name.getText().toString());
                user.setLastName(surname.getText().toString());
                HashMap<String, Boolean> skills = new HashMap<>();
                if (heavyBleedingCheckbox.isChecked()) {
                    skills.put("STOP_HEAVY_BLEEDING", true);
                } else {
                    skills.put("STOP_HEAVY_BLEEDING", false);
                }
                if (treatShockCheckbox.isChecked()) {
                    skills.put("TREATING_SHOCK", true);
                } else {
                    skills.put("TREATING_SHOCK", false);
                }
                if (cprCheckbox.isChecked()) {
                    skills.put("CPR", true);
                } else {
                    skills.put("CPR", false);
                }
                if (aedCheckbox.isChecked()) {
                    skills.put("AED", true);
                } else {
                    skills.put("AED", false);
                }
                user.setSkills(skills);
                database.collection("users").document(currentUser.getUid())
                        .set(user);
                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                startActivity(intent);
            }
        });
    }
}

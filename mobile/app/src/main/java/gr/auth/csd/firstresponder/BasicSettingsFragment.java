package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import gr.auth.csd.firstresponder.data.Responder;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic_settings, container, false);

        name = view.findViewById(R.id.namePlainText);
        surname = view.findViewById(R.id.surnamePlainText);
        heavyBleedingCheckbox = view.findViewById(R.id.heavyBleedingCheckBox);
        treatShockCheckbox = view.findViewById(R.id.treatShockCheckbox);
        cprCheckbox = view.findViewById(R.id.cprCheckbox);
        aedCheckbox = view.findViewById(R.id.aesCheckbox);

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

        return view;
    }

    private void readFieldsFromDatabase(){
        database = FirebaseFirestore.getInstance();
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

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                database.collection("users").document(currentUser.getUid())
                        .update("firstName", name.getText().toString());
                Toast.makeText(getActivity(), "Name changed!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        surname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                database.collection("users").document(currentUser.getUid())
                        .update("lastName", surname.getText().toString());
                Toast.makeText(getActivity(), "Surname changed!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        heavyBleedingCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (heavyBleedingCheckbox.isChecked()) {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.STOP_HEAVY_BLEEDING", true);
                } else {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.STOP_HEAVY_BLEEDING", false);
                }
                Toast.makeText(getActivity(), "Skill changed!", Toast.LENGTH_SHORT).show();
            }
        });

        treatShockCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treatShockCheckbox.isChecked()) {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.TREATING_SHOCK", true);
                } else {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.TREATING_SHOCK", false);
                }
                Toast.makeText(getActivity(), "Skill changed!", Toast.LENGTH_SHORT).show();
            }
        });

        cprCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cprCheckbox.isChecked()) {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.CPR", true);
                } else {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.CPR", false);
                }
                Toast.makeText(getActivity(), "Skill changed!", Toast.LENGTH_SHORT).show();
            }
        });

        aedCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aedCheckbox.isChecked()) {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.AED", true);
                } else {
                    database.collection("users").document(currentUser.getUid())
                            .update("skills.AED", false);
                }
                Toast.makeText(getActivity(), "Skill changed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

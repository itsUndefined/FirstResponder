package gr.auth.csd.firstresponder.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

import gr.auth.csd.firstresponder.Callback;
import gr.auth.csd.firstresponder.R;

public class LogInFragment extends Fragment {

    private Callback callback;
    private AutoCompleteTextView countryCode;
    private TextInputLayout phoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        callback = (Callback) getActivity();
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        Button login = view.findViewById(R.id.logInButton);

        countryCode = view.findViewById(R.id.filled_exposed_dropdown);
        phoneNumber = view.findViewById(R.id.phoneInput);

        List<String> COUNTRIES = Arrays.asList(getResources().getStringArray(R.array.logIn_code));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_menu_popup_item,
                COUNTRIES);
        countryCode.setAdapter(adapter);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String validPhoneNumber = countryCode.getEditableText().toString() + phoneNumber.getEditText().getText().toString();
                callback.phoneVerification(validPhoneNumber);
            }
        });

        Button back = view.findViewById(R.id.logInBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new StartFragment());
                fragmentTransaction.commit();
            }
        });

        if (savedInstanceState != null) {
            phoneNumber.getEditText().setText((savedInstanceState.getString("phoneNumber")));
        }

        OnBackPressedCallback returnToPreviousFragment = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new StartFragment());
                fragmentTransaction.commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), returnToPreviousFragment);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("phoneNumber", phoneNumber.getEditText().getText().toString());
    }
}

package gr.auth.csd.firstresponder;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class LogInFragment extends Fragment {

    private Callback callback;
    private Spinner countryCode;
    private EditText phoneNumber;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        callback = (Callback) getActivity();
        view = inflater.inflate(R.layout.fragment_log_in, container, false);
        Button login = view.findViewById(R.id.logInButton);

        countryCode = view.findViewById(R.id.phoneCodeInput);
        phoneNumber = view.findViewById(R.id.phoneInput);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String validPhoneNumber = countryCode.getSelectedItem().toString() + phoneNumber.getText().toString();
                callback.phoneVerification(validPhoneNumber);
            }
        });

        ImageView back = view.findViewById(R.id.logInBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new StartFragment());
                fragmentTransaction.commit();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_activity_fragment_container, new LogInFragment());
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

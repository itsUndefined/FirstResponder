package gr.auth.csd.firstresponder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class LogInFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        Button login = view.findViewById(R.id.logInButton);

        final Spinner countryCode = view.findViewById(R.id.phoneCodeInput);
        final EditText phoneNumber = view.findViewById(R.id.phoneInput);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String validPhoneNumber = countryCode.getSelectedItem().toString() + phoneNumber.getText().toString();

                // MainActivity mainActivity = (MainActivity) getActivity();

                CodeSubmitFragment codeSubmitFragment = new CodeSubmitFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                Bundle bundle = new Bundle();
                bundle.putString("phoneNumber", validPhoneNumber);
                codeSubmitFragment.setArguments(bundle);

                fragmentTransaction.replace(R.id.main_activity_fragment_container, codeSubmitFragment);
                fragmentTransaction.commit();
            }
        });

        ImageView back = view.findViewById(R.id.logInBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new StartFragment());
                fragmentTransaction.commit();
            }
        });
        return view;
    }
}

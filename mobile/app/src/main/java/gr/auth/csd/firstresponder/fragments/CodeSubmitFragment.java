package gr.auth.csd.firstresponder.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;

import gr.auth.csd.firstresponder.callbacks.MainActivityCallback;
import gr.auth.csd.firstresponder.R;

/**
 * Creates the submit code screen.
 */
public class CodeSubmitFragment extends Fragment {

    private MainActivityCallback mainActivityCallback;
    private TextInputLayout code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        mainActivityCallback = (MainActivityCallback) getActivity();
        View view = inflater.inflate(R.layout.fragment_code_submit, container, false);

        code = view.findViewById(R.id.codeInput);
        Button login = view.findViewById(R.id.submitCodeButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivityCallback.phoneCodeVerification(code.getEditText().getText().toString());
            }
        });

        Button back = view.findViewById(R.id.backR);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivityCallback.verificationAborted();
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new LogInFragment());
                fragmentTransaction.commit();
            }
        });

        if (savedInstanceState != null) {
            code.getEditText().setText(savedInstanceState.getString("code"));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("code", code.getEditText().getText().toString());
    }
}

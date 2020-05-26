package gr.auth.csd.firstresponder.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;

import gr.auth.csd.firstresponder.Callback;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.fragments.LogInFragment;

public class CodeSubmitFragment extends Fragment {

    private Callback callback;
    private TextInputLayout code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        callback = (Callback) getActivity();
        View view = inflater.inflate(R.layout.fragment_code_submit, container, false);

        code = view.findViewById(R.id.codeInput);
        Button login = view.findViewById(R.id.submitCodeButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.phoneCodeVerification(code.getEditText().getText().toString());
            }
        });

        Button back = view.findViewById(R.id.backR);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.reSendCode();
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new LogInFragment());
                fragmentTransaction.commit();
            }
        });

        if (savedInstanceState != null) {
            code.getEditText().setText(savedInstanceState.getString("code"));
        }

        /*OnBackPressedCallback returnToPreviousFragment = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new LogInFragment());
                fragmentTransaction.commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), returnToPreviousFragment);*/

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("code", code.getEditText().getText().toString());
    }
}

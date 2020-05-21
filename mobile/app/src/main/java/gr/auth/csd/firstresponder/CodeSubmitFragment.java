package gr.auth.csd.firstresponder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class CodeSubmitFragment extends Fragment {

    private Callback callback;
    private EditText code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        callback = (Callback) getActivity();
        View view = inflater.inflate(R.layout.fragment_code_submit, container, false);

        code = view.findViewById(R.id.codeInput);
        Button login = view.findViewById(R.id.submitCodeButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.phoneCodeVerification(code.getText().toString());
            }
        });

        ImageButton back = view.findViewById(R.id.backR);
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
            code.setText(savedInstanceState.getString("code"));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("code", code.getText().toString());
    }
}

package gr.auth.csd.firstresponder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class CodeSubmitFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_code_submit, container, false);
        Button login = view.findViewById(R.id.submitCodeButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new RegisterFragment());
                fragmentTransaction.commit();
            }
        });

        ImageView back = view.findViewById(R.id.submitCodeBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_activity_fragment_container, new LogInFragment());
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}

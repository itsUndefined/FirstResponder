package gr.auth.csd.firstresponder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;


import gr.auth.csd.firstresponder.fragments.DashboardFragment;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new DashboardFragment());
        fragmentTransaction.commit();
    }

}

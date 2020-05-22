package gr.auth.csd.firstresponder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import gr.auth.csd.firstresponder.fragments.BasicSettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.settings_activity_fragment_container, new BasicSettingsFragment());
            fragmentTransaction.commit();
        } else {
            onRestoreInstanceState(savedInstanceState);
        }
    }
}

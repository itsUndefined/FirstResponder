package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import gr.auth.csd.firstresponder.data.Alert;
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
        Toast.makeText(this, "On Intent", Toast.LENGTH_LONG).show();
        if (getIntent().hasExtra(DISPLAY_ALERT)) {
            Toast.makeText(this, "On Intent With valid data", Toast.LENGTH_LONG).show();
            Alert alert = (Alert) Objects.requireNonNull(getIntent().getExtras()).get(DISPLAY_ALERT);

        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.dashboard_activity_fragment_container, new DashboardFragment());
        fragmentTransaction.commit();
    }

    public static String DISPLAY_ALERT = "gr.auth.csd.firstresponder.DisplayAlert";
}

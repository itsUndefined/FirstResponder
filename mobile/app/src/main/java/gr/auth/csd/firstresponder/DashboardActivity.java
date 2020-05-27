package gr.auth.csd.firstresponder;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;


import gr.auth.csd.firstresponder.callbacks.DashboardFragmentCallback;
import gr.auth.csd.firstresponder.fragments.DashboardFragment;
import gr.auth.csd.firstresponder.helpers.PermissionRequest;

/**
 * This activity has the main menu of the app and the button to accept the permissions and to enable
 * the GPS service of the device.
 */
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        DashboardFragmentCallback fragment = (DashboardFragmentCallback) getSupportFragmentManager().getFragments().get(0);
        if(requestCode == PermissionRequest.LOCATION_ONLY) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragment.onPermissionsEnabled();
            } else {
                showAccessDeniedWarningMessage();
            }
        }
        if(requestCode == PermissionRequest.LOCATION_WITH_BACKGROUND) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                fragment.onPermissionsEnabled();
            } else {
                showAccessDeniedWarningMessage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            DashboardFragmentCallback fragment = (DashboardFragmentCallback) getSupportFragmentManager().getFragments().get(0);
            if (resultCode == RESULT_OK) {
                fragment.onLocationEnabled();
            } else if (resultCode == RESULT_CANCELED) {
                fragment.onLocationRequestIgnored();
            }
        }
    }

    private void showAccessDeniedWarningMessage(){
        AlertDialog.Builder fineLocationAlertBuilder = new AlertDialog.Builder(this);
        fineLocationAlertBuilder.setTitle("Warning!");
        fineLocationAlertBuilder.setMessage("Background location permission is required for the use of this application." +
                " Application will now exit");
        fineLocationAlertBuilder.setCancelable(false);
        fineLocationAlertBuilder.setPositiveButton(
                "I understand!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                }
        );

        AlertDialog alertDialog = fineLocationAlertBuilder.create();
        alertDialog.show();
    }

    public static int REQUEST_CHECK_SETTINGS = 17;
}

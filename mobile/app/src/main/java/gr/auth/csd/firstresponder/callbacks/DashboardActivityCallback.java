package gr.auth.csd.firstresponder.callbacks;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

public interface DashboardActivityCallback {
    void getPermissions(Context context);
    void setPermissionsGUI(Button button, TextView textView);
}

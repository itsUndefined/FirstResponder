package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {

    ImageView back;
    Button settings;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        back = findViewById(R.id.logInBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        settings = findViewById(R.id.settingsBtn);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        login = findViewById(R.id.logInActivityButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, PhoneCodeSubmitActivity.class);
                startActivity(intent);
            }
        });
    }
}

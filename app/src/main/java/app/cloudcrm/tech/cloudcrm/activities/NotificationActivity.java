package app.cloudcrm.tech.cloudcrm.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.loopj.android.airbrake.AirbrakeNotifier;

import app.cloudcrm.tech.cloudcrm.R;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_notification);

        setTitle(getIntent().getStringExtra("title"));

    }
}

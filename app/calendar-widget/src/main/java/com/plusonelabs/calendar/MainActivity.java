package com.plusonelabs.calendar;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.plusonelabs.calendar.util.PermissionsUtil;

/**
 * @author yvolk@yurivolkov.com
 */
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    boolean permissionsGranted = false;
    @NonNull
    int[] appWidgetIds = new int[]{};

    @NonNull
    public static Intent newIntentToStartMe(Context context) {
        return new Intent(context.getApplicationContext(), MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissionsAndRequestThem();
        findWidgets();
        updateScreen();
    }

    private void checkPermissionsAndRequestThem() {
        checkPermissions();
        if (!permissionsGranted) {
            Log.d(this.getLocalClassName(), "Requesting permission: " + PermissionsUtil.PERMISSION);
            ActivityCompat.requestPermissions(this, new String[]{PermissionsUtil.PERMISSION}, 1);
        }
    }

    private void checkPermissions() {
        permissionsGranted = PermissionsUtil.arePermissionsGranted(this);
    }

    /**
     * TODO: For now we only check the fact that at least one AppWidget exists.
     */
    private void findWidgets() {
        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, EventAppWidgetProvider.class));
        if (ids != null) {
            appWidgetIds = ids;
        }
    }

    private void updateScreen() {
        int messageResourceId = R.string.permissions_justification;
        if (permissionsGranted) {
            if (appWidgetIds.length > 0 ) {
                Intent intent = new Intent(this.getApplicationContext(), WidgetConfigurationActivity.class);
                startActivity(intent);
                finish();
                return;
            } else {
                messageResourceId = R.string.no_widgets_found;
            }
        }
        TextView message = ((TextView)this.findViewById(R.id.message));
        if (message != null) {
            message.setText(messageResourceId);
        }
        Button grantPermissionsButton = (Button) findViewById(R.id.grant_permissions);
        if (grantPermissionsButton != null) {
            grantPermissionsButton.setVisibility(permissionsGranted ? View.GONE : View.VISIBLE);
        }
    }

    public void grantPermissions(View view) {
        checkPermissionsAndRequestThem();
        updateScreen();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
        updateScreen();
    }

    public void onCloseButtonClick(View view) {
        finish();
    }
}

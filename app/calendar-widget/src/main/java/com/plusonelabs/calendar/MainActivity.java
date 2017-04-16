package com.plusonelabs.calendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.plusonelabs.calendar.prefs.InstanceSettings;
import com.plusonelabs.calendar.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yvolk@yurivolkov.com
 */
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    protected static final String KEY_VISIBLE_NAME = "visible_name";
    protected static final String KEY_ID = "id";

    boolean permissionsGranted = false;
    ListView listView = null;

    @NonNull
    public static Intent intentToConfigure(Context context, int widgetId) {
        return intentToStartMe(context).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
    }

    @NonNull
    public static Intent intentToStartMe(Context context) {
        return new Intent(context.getApplicationContext(), MainActivity.class).
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(android.R.id.list);
        checkPermissions();
        if (preparedToOpen()) {
            updateScreen();
        }
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

    private boolean preparedToOpen() {
        int newWidgetId = 0;
        if (permissionsGranted) {
            newWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            if (newWidgetId == 0 && InstanceSettings.getInstances(this).size() == 1) {
                newWidgetId = InstanceSettings.getInstances(this).keySet().iterator().next();
            }
            if (newWidgetId != 0) {
                startActivity(WidgetConfigurationActivity.intentToStartMe(this, newWidgetId));
                finish();
            }
        }
        return newWidgetId == 0;
    }

    private void updateScreen() {
        int messageResourceId = R.string.permissions_justification;
        if (permissionsGranted) {
            if (InstanceSettings.getInstances(this).isEmpty()) {
                messageResourceId = R.string.no_widgets_found;
            } else {
                messageResourceId = R.string.select_a_widget_to_configure;
            }
        }
        TextView message = ((TextView) this.findViewById(R.id.message));
        if (message != null) {
            message.setText(messageResourceId);
        }

        if (!InstanceSettings.getInstances(this).isEmpty() && permissionsGranted) {
            fillWidgetList();
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
        }

        Button goToHomeScreenButton = (Button) findViewById(R.id.go_to_home_screen_button);
        if (goToHomeScreenButton != null) {
            goToHomeScreenButton.setVisibility(permissionsGranted &&
                    InstanceSettings.getInstances(this).isEmpty() ? View.VISIBLE : View.GONE);
        }

        Button grantPermissionsButton = (Button) findViewById(R.id.grant_permissions);
        if (grantPermissionsButton != null) {
            grantPermissionsButton.setVisibility(permissionsGranted ? View.GONE : View.VISIBLE);
        }
    }

    private void fillWidgetList() {
        final List<Map<String, String>> data = new ArrayList<>();
        for (InstanceSettings settings : InstanceSettings.getInstances(this).values()) {
            Map<String, String> map = new HashMap<>();
            map.put(KEY_VISIBLE_NAME, settings.getWidgetInstanceName());
            map.put(KEY_ID, Integer.toString(settings.getWidgetId()));
            data.add(map);
        }

        listView.setAdapter(new SimpleAdapter(this, data, R.layout.widget_list_item,
                new String[]{KEY_VISIBLE_NAME}, new int[]{R.id.widget_name}));

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> stringStringMap = data.get(position);
                String widgetId = stringStringMap.get(KEY_ID);
                Intent intent = WidgetConfigurationActivity.intentToStartMe(
                        MainActivity.this, Integer.valueOf(widgetId));
                startActivity(intent);
                finish();
            }
        });
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

    public void onHomeButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

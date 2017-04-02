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
    public static Intent newIntentToStartMe(Context context) {
        return new Intent(context.getApplicationContext(), MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(android.R.id.list);
        checkPermissionsAndRequestThem();
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

    private void updateScreen() {
        int messageResourceId = R.string.permissions_justification;
        if (permissionsGranted) {
            if (InstanceSettings.getInstances(this).size() == 1 ) {
                Intent intent = WidgetConfigurationActivity.newIntentToStartMe(
                        this, InstanceSettings.getInstances(this).keySet().iterator().next());
                startActivity(intent);
                finish();
                return;
            } else if (InstanceSettings.getInstances(this).isEmpty()) {
                messageResourceId = R.string.no_widgets_found;
            } else {
                messageResourceId = R.string.select_a_widget_to_configure;
            }
        }
        TextView message = ((TextView)this.findViewById(R.id.message));
        if (message != null) {
            message.setText(messageResourceId);
        }

        if (!InstanceSettings.getInstances(this).isEmpty() && permissionsGranted) {
            fillWidgetList();
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
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

        SimpleAdapter adapter = new SimpleAdapter(this,
                data,
                R.layout.widget_list_item,
                new String[] {KEY_VISIBLE_NAME, KEY_ID},
                new int[] {R.id.visible_name, R.id.id});
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.id);
                if (textView != null) {
                    Intent intent = WidgetConfigurationActivity.newIntentToStartMe(
                            MainActivity.this, Integer.valueOf(textView.getText().toString()));
                    startActivity(intent);
                    finish();
                }
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

    public void onCloseButtonClick(View view) {
        finish();
    }
}

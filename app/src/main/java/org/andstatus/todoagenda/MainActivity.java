package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return intentToStartMe(context)
                .setData(Uri.parse("intent:configureMain" + widgetId))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
    }

    @NonNull
    public static Intent intentToStartMe(Context context) {
        return new Intent(context.getApplicationContext(), MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            Log.w("onCreate", "Failed to find layout", e);
            finish();
            return;
        }
        listView = findViewById(android.R.id.list);
        checkPermissions();
        if (openThisActivity()) {
            updateScreen();
        }
    }

    private void checkPermissionsAndRequestThem() {
        checkPermissions();
        if (!permissionsGranted) {
            Set<String> neededPermissions = EventProviderType.getNeededPermissions();
            Log.d(this.getLocalClassName(), "Requesting permissions: " + neededPermissions);
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[]{}), 1);
        }
    }

    private void checkPermissions() {
        permissionsGranted = PermissionsUtil.arePermissionsGranted(this);
    }

    private boolean openThisActivity() {
        int widgetIdToConfigure = 0;
        if (permissionsGranted) {
            widgetIdToConfigure = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            if (widgetIdToConfigure == 0 && AllSettings.getInstances(this).size() == 1) {
                widgetIdToConfigure = AllSettings.getInstances(this).keySet().iterator().next();
            }
            if (widgetIdToConfigure != 0) {
                startActivity(WidgetConfigurationActivity.intentToStartMe(this, widgetIdToConfigure));
                finish();
            }
        }
        return widgetIdToConfigure == 0;
    }

    private void updateScreen() {
        int messageResourceId = R.string.permissions_justification;
        if (permissionsGranted) {
            if (AllSettings.getInstances(this).isEmpty()) {
                messageResourceId = R.string.no_widgets_found;
            } else {
                messageResourceId = R.string.select_a_widget_to_configure;
            }
        }
        TextView message = this.findViewById(R.id.message);
        if (message != null) {
            message.setText(messageResourceId);
        }

        if (!AllSettings.getInstances(this).isEmpty() && permissionsGranted) {
            fillWidgetList();
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
        }

        Button goToHomeScreenButton = findViewById(R.id.go_to_home_screen_button);
        if (goToHomeScreenButton != null) {
            goToHomeScreenButton.setVisibility(permissionsGranted &&
                    AllSettings.getInstances(this).isEmpty() ? View.VISIBLE : View.GONE);
        }

        Button grantPermissionsButton = findViewById(R.id.grant_permissions);
        if (grantPermissionsButton != null) {
            grantPermissionsButton.setVisibility(permissionsGranted ? View.GONE : View.VISIBLE);
        }
        EnvironmentChangedReceiver.updateAllWidgets(this);
    }

    private void fillWidgetList() {
        final List<Map<String, String>> data = new ArrayList<>();
        for (InstanceSettings settings : AllSettings.getInstances(this).values()) {
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
        AllSettings.ensureLoadedFromFiles(this, true);
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

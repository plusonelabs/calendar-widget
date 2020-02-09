package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.RootFragment;
import org.andstatus.todoagenda.provider.WidgetData;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class WidgetConfigurationActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();
    public static final String FRAGMENT_TAG = "settings_fragment";

    public static final int REQUEST_ID_RESTORE_SETTINGS = 1;
    public static final int REQUEST_ID_BACKUP_SETTINGS = 2;
    private int widgetId = 0;
    private boolean saveOnPause = true;

    @NonNull
    public static Intent intentToStartMe(Context context, int widgetId) {
        Intent intent = new Intent(context, WidgetConfigurationActivity.class)
            .setData(Uri.parse("intent:configure" + widgetId))
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (saveOnPause) {
            ApplicationPreferences.save(this, widgetId);
            EnvironmentChangedReceiver.updateWidget(this, widgetId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartIfNeeded();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!openThisActivity(getIntent())) return;

        setContentView(R.layout.activity_settings);
        super.onCreate(savedInstanceState);

        setTitle(ApplicationPreferences.getWidgetInstanceName(this));

        if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new RootFragment();
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.settings_container, fragment, FRAGMENT_TAG);
            ft.commit();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        setTitle(pref.getTitle() + " - " + ApplicationPreferences.getWidgetInstanceName(this));
        return false;
    }

    private boolean openThisActivity(Intent newIntent) {
        int newWidgetId = newIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        if (newWidgetId == 0) {
            newWidgetId = ApplicationPreferences.getWidgetId(this);
        }
        Intent restartIntent = null;
        if (newWidgetId == 0 || !PermissionsUtil.arePermissionsGranted(this)) {
            restartIntent = MainActivity.intentToStartMe(this);
        } else if (widgetId != 0 && widgetId != newWidgetId) {
            restartIntent = MainActivity.intentToConfigure(this, newWidgetId);
        } else if (widgetId == 0) {
            widgetId = newWidgetId;
            ApplicationPreferences.fromInstanceSettings(this, widgetId);
        }
        if (restartIntent != null) {
            widgetId = 0;
            startActivity(restartIntent);
            finish();
        }
        return restartIntent == null;
    }

    private void restartIfNeeded() {
        if (widgetId != ApplicationPreferences.getWidgetId(this) ||
                !PermissionsUtil.arePermissionsGranted(this)) {
            widgetId = 0;
            startActivity(MainActivity.intentToStartMe(this));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ID_BACKUP_SETTINGS:
                if (resultCode == RESULT_OK && data != null) {
                    backupSettings(data.getData());
                }
                break;
            case REQUEST_ID_RESTORE_SETTINGS:
                if (resultCode == RESULT_OK && data != null) {
                    restoreSettings(data.getData());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void backupSettings(Uri uri) {
        if (uri == null) return;

        InstanceSettings settings = AllSettings.instanceFromId(this, widgetId);
        String jsonSettings = WidgetData.fromSettings(this, settings).toJsonString();
        ParcelFileDescriptor pfd = null;
        FileOutputStream out = null;
        try {
            pfd = this.getContentResolver().openFileDescriptor(uri, "w");
            out = new FileOutputStream(pfd.getFileDescriptor());
            out.write(jsonSettings.getBytes());
        } catch (Exception e) {
            String msg = "Error while writing " + getText(R.string.app_name) +
                    " settings to " + uri + "\n" + e.getMessage();
            Log.w(TAG, msg, e);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                    Log.w(TAG, "Error while closing stream", e2);
                }
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException e2) {
                    Log.w(TAG, "Error while closing file descriptor", e2);
                }
            }
        }
        Toast.makeText(this, getText(R.string.backup_settings_title), Toast.LENGTH_LONG).show();
    }

    private void restoreSettings(Uri uri) {
        if (uri == null) return;

        JSONObject jsonObject = readJson(uri);
        if (jsonObject.length() == 0) return;

        if (!AllSettings.restoreWidgetSettings(this, jsonObject, widgetId).isEmpty()) {
            saveOnPause = false;
            int duration = 3000;
            final WidgetConfigurationActivity context = WidgetConfigurationActivity.this;
            Toast.makeText(context, context.getText(R.string.restore_settings_title), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> {
                startActivity(intentToStartMe(context, widgetId));
                context.finish();
            }, duration);
        }
    }

    private JSONObject readJson(Uri uri) {
        final int BUFFER_LENGTH = 10000;
        InputStream in = null;
        Reader reader = null;
        try {
            in = getContentResolver().openInputStream(uri);
            char[] buffer = new char[BUFFER_LENGTH];
            StringBuilder builder = new StringBuilder();
            int count;
            reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            while ((count = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, count);
            }
            return new JSONObject(builder.toString());
        } catch (IOException | JSONException e) {
            String msg = "Error while reading " + getText(R.string.app_name) +
                    " settings from " + uri + "\n" + e.getMessage();
            Log.w(TAG, msg, e);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.w(TAG, "Error while closing stream", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "Error while closing reader", e);
                }
            }
        }
        return new JSONObject();
    }
}
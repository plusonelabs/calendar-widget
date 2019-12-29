package org.andstatus.todoagenda.prefs;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.provider.WidgetData;

import static android.content.Intent.ACTION_CREATE_DOCUMENT;
import static org.andstatus.todoagenda.WidgetConfigurationActivity.REQUEST_ID_BACKUP_SETTINGS;
import static org.andstatus.todoagenda.WidgetConfigurationActivity.REQUEST_ID_RESTORE_SETTINGS;
import static org.andstatus.todoagenda.util.DateUtil.formatLogDateTime;

public class FeedbackPreferencesFragment extends PreferenceFragment {
    private static final String TAG = FeedbackPreferencesFragment.class.getSimpleName();

    private static final String KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging";
    private static final String KEY_BACKUP_SETTINGS = "backupSettings";
    private static final String KEY_RESTORE_SETTINGS = "restoreSettings";

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        int widgetId = ApplicationPreferences.getWidgetId(getActivity());
        ApplicationPreferences.save(getActivity(), widgetId);
        switch (preference.getKey()) {
            case KEY_SHARE_EVENTS_FOR_DEBUGGING:
                QueryResultsStorage.shareEventsForDebugging(getActivity(), widgetId);
                break;
            case KEY_BACKUP_SETTINGS:
                InstanceSettings settings = AllSettings.instanceFromId(getActivity(), widgetId);
                String fileName = (settings.getWidgetInstanceName() + "-" + getText(R.string.app_name))
                        .replaceAll("\\W+", "-") +
                        "-backup-" + formatLogDateTime(System.currentTimeMillis()) +
                        ".json";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent intent = new Intent(ACTION_CREATE_DOCUMENT);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_TITLE, fileName);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    getActivity().startActivityForResult(intent, REQUEST_ID_BACKUP_SETTINGS);
                } else {
                    String jsonSettings = WidgetData.fromSettings(settings.getContext(), settings).toJsonString();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                    intent.putExtra(Intent.EXTRA_TEXT, jsonSettings);
                    getActivity().startActivity(
                            Intent.createChooser(intent, getActivity().getText(R.string.backup_settings_title)));
                    Log.i(TAG, "onPreferenceTreeClick; Backed up \n" + jsonSettings);
                }
                break;
            case KEY_RESTORE_SETTINGS:
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT)
                        .addCategory(Intent.CATEGORY_OPENABLE);
                Intent withChooser = Intent.createChooser(intent,
                        getActivity().getText(R.string.restore_settings_title));
                getActivity().startActivityForResult(withChooser, REQUEST_ID_RESTORE_SETTINGS);
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_feedback);
    }
}
package org.andstatus.todoagenda.prefs;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.QueryResultsStorage;

import static android.content.Intent.ACTION_CREATE_DOCUMENT;
import static org.andstatus.todoagenda.WidgetConfigurationActivity.REQUEST_ID_BACKUP_SETTINGS;
import static org.andstatus.todoagenda.WidgetConfigurationActivity.REQUEST_ID_RESTORE_SETTINGS;
import static org.andstatus.todoagenda.util.DateUtil.formatLogDateTime;

public class FeedbackPreferencesFragment extends PreferenceFragmentCompat {
    private static final String TAG = FeedbackPreferencesFragment.class.getSimpleName();

    private static final String KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging";
    private static final String KEY_BACKUP_SETTINGS = "backupSettings";
    private static final String KEY_RESTORE_SETTINGS = "restoreSettings";

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
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
                Intent intent = new Intent(ACTION_CREATE_DOCUMENT);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                getActivity().startActivityForResult(intent, REQUEST_ID_BACKUP_SETTINGS);
                break;
            case KEY_RESTORE_SETTINGS:
                Intent intent2 = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT)
                        .addCategory(Intent.CATEGORY_OPENABLE);
                Intent withChooser = Intent.createChooser(intent2,
                        getActivity().getText(R.string.restore_settings_title));
                getActivity().startActivityForResult(withChooser, REQUEST_ID_RESTORE_SETTINGS);
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_feedback);
    }
}
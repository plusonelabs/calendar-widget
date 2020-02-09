package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.preference.PreferenceDialogFragmentCompat;

import org.andstatus.todoagenda.R;

public class DateFormatDialog extends PreferenceDialogFragmentCompat {
    private final DateFormatPreference preference;
    private LinearLayout dialogView;
    private Spinner typeSpinner;

    public DateFormatDialog(DateFormatPreference preference) {
        this.preference = preference;

        final Bundle b = new Bundle();
        b.putString(ARG_KEY, preference.getKey());
        setArguments(b);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        dialogView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dateformat_preference, null);

        typeSpinner = dialogView.findViewById(R.id.date_format_type);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, DateFormatType.getSpinnerEntryList(context));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setSelection(preference.getValue().type.getSpinnerPosition());

        return dialogView;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DateFormatValue value = getValue();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }
    }

    private DateFormatValue getValue() {
        int position = typeSpinner.getSelectedItemPosition();
        return position >= 0 ? DateFormatType.values()[position].defaultValue() : DateFormatType.UNKNOWN.defaultValue();
    }
}

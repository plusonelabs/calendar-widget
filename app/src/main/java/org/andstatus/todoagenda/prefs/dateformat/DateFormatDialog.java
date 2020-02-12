package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.prefs.InstanceSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author yvolk@yurivolkov.com
 */
public class DateFormatDialog extends PreferenceDialogFragmentCompat implements AdapterView.OnItemSelectedListener, View.OnKeyListener, TextWatcher {
    private final DateFormatPreference preference;
    private Spinner typeSpinner;
    private EditText customPatternText;
    private EditText sampleDateText;
    private TextView resultText;
    private DateFormatValue sampleDateFormatValue = DateFormatValue.of(DateFormatType.CUSTOM, "yyyy-MM-dd");

    public DateFormatDialog(DateFormatPreference preference) {
        this.preference = preference;

        final Bundle b = new Bundle();
        b.putString(ARG_KEY, preference.getKey());
        setArguments(b);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        LinearLayout dialogView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dateformat_preference, null);

        typeSpinner = dialogView.findViewById(R.id.date_format_type);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, DateFormatType.getSpinnerEntryList(context));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setSelection(preference.getValue().type.getSpinnerPosition());
        typeSpinner.setOnItemSelectedListener(this);

        customPatternText = dialogView.findViewById(R.id.custom_pattern);
        customPatternText.setText(preference.getValue().getPattern());
        customPatternText.addTextChangedListener(this);

        sampleDateText = dialogView.findViewById(R.id.sample_date);
        sampleDateText.setText(getSampleDateText());
        sampleDateText.addTextChangedListener(this);

        resultText = dialogView.findViewById(R.id.result);

        return dialogView;
    }

    private CharSequence getSampleDateText() {
        return new DateFormatter(getContext(), sampleDateFormatValue, getSettings().clock().now())
                .formatMillis(getSettings().clock().now().getMillis());
    }

    @Override
    public void onResume() {
        super.onResume();
        calcResult();
    }

    // Two methods to listen for the Spinner changes
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (getValue().hasPattern()) {
            customPatternText.setText(getValue().getPattern());
        }
        calcResult();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        calcResult();
    }

    // Four methods to listen to the Custom pattern text changes
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override
    public void afterTextChanged(Editable s) {
        calcResult();
    }
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
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
        return position >= 0
            ? DateFormatValue.of(DateFormatType.values()[position], customPatternText.getText().toString())
            : DateFormatType.UNKNOWN.defaultValue();
    }

    private void calcResult() {
        SimpleDateFormat sampleFormat = getSampleDateFormat();
        CharSequence result;
        try {
            Date sampleDate = sampleFormat.parse(sampleDateText.getText().toString());
            result = sampleDate == null
                    ? "null"
                    : new DateFormatter(this.getContext(), getValue(), getSettings().clock().now())
                        .formatMillis(sampleDate.getTime());
        } catch (ParseException e) {
            result = e.getLocalizedMessage();
        }
        resultText.setText(result);
    }

    private SimpleDateFormat getSampleDateFormat() {
        return new SimpleDateFormat(sampleDateFormatValue.getPattern(), Locale.ENGLISH);
    }

    private InstanceSettings getSettings() {
        int widgetId = ApplicationPreferences.getWidgetId(getActivity());
        return AllSettings.instanceFromId(getActivity(), widgetId);
    }
}

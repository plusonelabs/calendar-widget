/*
 * Copyright (c) 2019 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
                .formatDate(getSettings().clock().now());
    }

    @Override
    public void onResume() {
        super.onResume();
        calcResult();
    }

    // Two methods to listen for the Spinner changes
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (getValue().type.hasPattern()) {
            customPatternText.setText(getValue().type.pattern);
        } else if (!getValue().hasPattern() && getValue().type.isCustomPattern()) {
            customPatternText.setText(DateFormatType.DEFAULT_EXAMPLE.pattern);
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
            DateFormatValue value = getValue().toSave();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }
    }

    private DateFormatValue getValue() {
        int position = typeSpinner.getSelectedItemPosition();
        if (position >= 0) {
            DateFormatType selectedType = DateFormatType.values()[position];
            return DateFormatValue.of(selectedType, customPatternText.getText().toString());
        }
        return DateFormatType.UNKNOWN.defaultValue();
    }

    private void calcResult() {
        DateFormatValue dateFormatValue = getValue();
        SimpleDateFormat sampleFormat = getSampleDateFormat();
        CharSequence result;
        try {
            if (customPatternText.isEnabled() != dateFormatValue.type.isCustomPattern()) {
                customPatternText.setEnabled(dateFormatValue.type.isCustomPattern());
            }
            Date sampleDate = sampleFormat.parse(sampleDateText.getText().toString());
            result = sampleDate == null
                    ? "null"
                    : new DateFormatter(this.getContext(), dateFormatValue, getSettings().clock().now())
                        .formatDate(new DateTime(sampleDate.getTime(), getSettings().clock().getZone()));
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

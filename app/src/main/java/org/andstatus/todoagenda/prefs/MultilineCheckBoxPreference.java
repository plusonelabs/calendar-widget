package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

/**
 * See http://stackoverflow.com/questions/9220039/android-preferencescreen-title-in-two-lines
 */
public class MultilineCheckBoxPreference extends CheckBoxPreference {

    public MultilineCheckBoxPreference(Context context) {
        super(context);
    }

    public MultilineCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultilineCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        TextView textView = (TextView) viewHolder.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setSingleLine(false);
        }
    }

}

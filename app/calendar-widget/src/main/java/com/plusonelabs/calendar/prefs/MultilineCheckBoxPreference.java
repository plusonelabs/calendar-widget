package com.plusonelabs.calendar.prefs;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

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
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView textView = (TextView) view.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setSingleLine(false);
        }
    }

}

package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WidgetConfigurationActivity extends PreferenceActivity {

    private static final String PREFERENCES_PACKAGE_NAME = "com.plusonelabs.calendar.prefs";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (hasHeaders() && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            createAddButton();
        }
    }

    private void createAddButton() {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.dividerHorizontal, value, true);
        LinearLayout footer = new LinearLayout(this, null, android.R.attr.buttonBarStyle);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING);
        footer.setDividerPadding(0);
        footer.setDividerDrawable(getResources().getDrawable(value.resourceId));
        Button button = new Button(this, null, android.R.attr.buttonBarButtonStyle);
        button.setText(R.string.prefs_add_widget);
        button.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                EventAppWidgetProvider.updateWidget(WidgetConfigurationActivity.this,
                        appWidgetId);
                finish();
            }
        });
        footer.addView(button, MATCH_PARENT, MATCH_PARENT);
        setListFooter(footer);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_header, target);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected boolean isValidFragment(String fragmentName) {
        if (fragmentName.startsWith(PREFERENCES_PACKAGE_NAME)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }
}
package com.plusonelabs.calendar;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
			Button button = new Button(this);
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
			setListFooter(button);
		}
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences_header, target);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		if (fragmentName.startsWith(PREFERENCES_PACKAGE_NAME)) {
			return true;
		}
		return super.isValidFragment(fragmentName);
	}
}
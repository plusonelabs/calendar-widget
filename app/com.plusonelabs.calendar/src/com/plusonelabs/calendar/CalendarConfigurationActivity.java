package com.plusonelabs.calendar;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;

public class CalendarConfigurationActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Add a button to the header list.
		if (hasHeaders()) {
			Button button = new Button(this);
			button.setText("Add Widget");
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Intent intent = getIntent();
					Bundle extras = intent.getExtras();
					if (extras != null) {
						int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
								AppWidgetManager.INVALID_APPWIDGET_ID);
						CalendarConfigurationActivity context = CalendarConfigurationActivity.this;
						AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
						RemoteViews views = new RemoteViews(context.getPackageName(),
								R.layout.widget);
						appWidgetManager.updateAppWidget(widgetId, views);
						Intent resultValue = new Intent();
						resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
						setResult(RESULT_OK, resultValue);
						finish();
					}

				}
			});
			setListFooter(button);
		}
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences_header, target);
	}
}
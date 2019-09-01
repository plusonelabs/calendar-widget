package org.andstatus.todoagenda;

import android.content.Context;
import android.util.Log;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import androidx.test.core.app.ApplicationProvider;

/**
 * @author yvolk@yurivolkov.com
 */
    public class TestRunListener extends RunListener {

    public TestRunListener() {
        Log.i("testSuite",  this.getClass().getName() + " created");
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        super.testSuiteFinished(description);
        Log.i("testSuite", "Test Suite finished: " + description);
        if (description.toString().equals("null")) restoreWidgets();
    }

    private void restoreWidgets() {
        Context targetContext = ApplicationProvider.getApplicationContext();
        AllSettings.ensureLoadedFromFiles(targetContext, true);
        for (Integer widgetId: AllSettings.getInstances(targetContext).keySet()) {
            AppWidgetProvider.recreateWidget(targetContext, widgetId);
        }
        Log.i("testSuite", "Widgets restored");
    }
}

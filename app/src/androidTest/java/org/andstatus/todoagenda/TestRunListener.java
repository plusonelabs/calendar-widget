package org.andstatus.todoagenda;

import android.util.Log;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.provider.MockCalendarContentProvider;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTimeZone;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * @author yvolk@yurivolkov.com
 */
    public class TestRunListener extends RunListener {
    private final DateTimeZone storedZone;

    public TestRunListener() {
        Log.i("testSuite",  this.getClass().getName() + " created");
        storedZone = DateTimeZone.getDefault();
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        super.testSuiteFinished(description);
        Log.i("testSuite", "Test Suite finished: " + description);
        if (description.toString().equals("null")) restoreWidgets();
    }

    private void restoreWidgets() {
        MockCalendarContentProvider.tearDown();

        DateUtil.setNow(null);
        DateTimeZone.setDefault(storedZone);
        AllSettings.forget();
        EventProviderType.forget();
        EnvironmentChangedReceiver.forget();
        Log.i("testSuite", "App restored");
    }
}

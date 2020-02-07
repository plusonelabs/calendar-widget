package org.andstatus.todoagenda;

import android.util.Log;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.MockCalendarContentProvider;
import org.andstatus.todoagenda.util.LazyVal;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;

/**
 * @author yvolk@yurivolkov.com
 */
public class BaseWidgetTest {
    final static String TAG = BaseWidgetTest.class.getSimpleName();
    private static final int MAX_MILLIS_TO_WAIT_FOR_LAUNCHER = 2000;
    private static final int MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION = 40000;

    protected MockCalendarContentProvider provider = null;
    protected LazyVal<RemoteViewsFactory> factory = LazyVal.of(
            () -> new RemoteViewsFactory(provider.getContext(), provider.getWidgetId(), false));

    @Before
    public void setUp() throws Exception {
        provider = MockCalendarContentProvider.getContentProvider();
    }

    @After
    public void tearDown() throws Exception {
        MockCalendarContentProvider.tearDown();
        factory.reset();
    }

    DateTime dateTime(
            int year,
            int monthOfYear,
            int dayOfMonth) {
        return dateTime(year, monthOfYear, dayOfMonth, 0, 0);
    }

    DateTime dateTime(
            int year,
            int monthOfYear,
            int dayOfMonth,
            int hourOfDay,
            int minuteOfHour) {
        return new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0,
                provider.getSettings().clock().getZone());
    }

    protected void playResults(String tag) {
        Log.d(tag, provider.getWidgetId() + " playResults started");
        provider.updateAppSettings(tag);

        if (provider.usesActualWidget) {
            InstanceState.clear(provider.getWidgetId());
            EnvironmentChangedReceiver.updateWidget(provider.getContext(), provider.getWidgetId());
            if (!RemoteViewsFactory.factories.containsKey(provider.getWidgetId())) {
                waitForRemoteViewsFactoryCreation();
            }
            waitTillWidgetIsUpdated(tag);
            waitTillWidgetIsReloaded(tag);
            waitTillWidgetIsRedrawn(tag);
            EnvironmentChangedReceiver.sleep(1000);
            if (InstanceState.get(provider.getWidgetId()).listReloaded == 0) {
                Log.d(tag, provider.getWidgetId() + " was not reloaded by a Launcher");
                getFactory().onDataSetChanged();
            }
        } else {
            getFactory().onDataSetChanged();
        }
        getFactory().logWidgetEntries(tag);
        Log.d(tag, provider.getWidgetId() + " playResults ended");
    }

    private void waitForRemoteViewsFactoryCreation() {
        long start = System.currentTimeMillis();
        while (RemoteViewsFactory.factories.get(getSettings().getWidgetId()) == null &&
                Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION){
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    private void waitTillWidgetIsUpdated(String tag) {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState.get(provider.getWidgetId()).updated > 0) {
                Log.d(tag, provider.getWidgetId() + " updated");
                break;
            }
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    private void waitTillWidgetIsReloaded(String tag) {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER){
            if (InstanceState.get(provider.getWidgetId()).listReloaded > 0) {
                Log.d(tag, provider.getWidgetId() + " reloaded");
                break;
            }
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    private void waitTillWidgetIsRedrawn(String tag) {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER){
            if (InstanceState.get(provider.getWidgetId()).listRedrawn > 0) {
                Log.d(tag, provider.getWidgetId() + " redrawn");
                break;
            }
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    protected InstanceSettings getSettings() {
        return provider.getSettings();
    }

    public RemoteViewsFactory getFactory() {
        RemoteViewsFactory existingFactory = RemoteViewsFactory.factories.get(provider.getWidgetId());
        return existingFactory == null ? factory.get() : existingFactory;
    }
}

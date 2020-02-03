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
    private static final int MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION = 40000;

    protected MockCalendarContentProvider provider = null;
    protected LazyVal<RemoteViewsFactory> factory = LazyVal.of(
            () -> new RemoteViewsFactory(provider.getContext(), provider.getWidgetId(), false));

    @Before
    public void setUp() throws Exception {
        provider = MockCalendarContentProvider.getContentProvider();
        RemoteViewsFactory.setWaitingForRedraw(provider.getWidgetId(), false);
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
        Log.d(tag, provider.getWidgetId() + " playResults");
        provider.updateAppSettings(tag);

        EnvironmentChangedReceiver.updateWidget(provider.getContext(), provider.getWidgetId());

        if (provider.usesActualWidget) {
            waitForRemoteViewsFactoryCreation();
        }
        getFactory().onDataSetChanged();
        getFactory().logWidgetEntries(tag);

        if (provider.usesActualWidget) {
            waitTillWidgetIsRedrawn();
        }
    }

    private void waitForRemoteViewsFactoryCreation() {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) <
                RemoteViewsFactory.MIN_MILLIS_BETWEEN_RELOADS +
                        (RemoteViewsFactory.factories.get(getSettings().getWidgetId()) == null
                                ? MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION : 0)){
            EnvironmentChangedReceiver.sleep(20);
        }
        EnvironmentChangedReceiver.sleep(250);
    }

    private void waitTillWidgetIsRedrawn() {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) <
                RemoteViewsFactory.MIN_MILLIS_BETWEEN_RELOADS +
                    (getFactory().isWaitingForRedraw() ? RemoteViewsFactory.MAX_MILLIS_TO_WAIT_FOR_LAUNCHER_REDRAW : 0)){
            EnvironmentChangedReceiver.sleep(20);
        }
        EnvironmentChangedReceiver.sleep(250);
    }

    protected InstanceSettings getSettings() {
        return provider.getSettings();
    }

    public RemoteViewsFactory getFactory() {
        RemoteViewsFactory existingFactory = RemoteViewsFactory.factories.get(provider.getWidgetId());
        return existingFactory == null ? factory.get() : existingFactory;
    }
}

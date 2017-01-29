package com.plusonelabs.calendar.calendar;

import android.os.Bundle;
import android.test.mock.MockContentProvider;

/**
 * @author yvolk@yurivolkov.com
 */
public class MockSettingsProvider extends MockContentProvider {

    public Bundle call(String method, String request, Bundle args) {
        return new Bundle();
    }

}

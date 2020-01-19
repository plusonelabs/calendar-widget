/*
 * Copyright (c) 2019 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.LocaleList;
import android.text.TextUtils;

import java.util.Locale;

/**
 * @author yvolk@yurivolkov.com
 */
public class MyLocale {
    public static final String KEY_CUSTOM_LOCALE = "custom_locale";
    private static final String CUSTOM_LOCALE_DEFAULT = "default";

    private static volatile Locale mLocale = null;
    private static volatile Locale mDefaultLocale = null;

    private MyLocale() {
        // Non instantiable
    }

    public static boolean isEnLocale() {
        Locale locale = mLocale;
        if (locale == null) {
            locale = mDefaultLocale;
        }
        return  locale == null || locale.getLanguage().isEmpty() || locale.getLanguage().startsWith("en");
    }

    public static void setLocale(ContextWrapper contextWrapper) {
        setLocale(contextWrapper, ApplicationPreferences.getString(contextWrapper, KEY_CUSTOM_LOCALE, CUSTOM_LOCALE_DEFAULT));
    }

    public static void setLocale(ContextWrapper contextWrapper, String strLocale) {
        if (mDefaultLocale == null) {
            mDefaultLocale = contextWrapper.getBaseContext().getResources().getConfiguration().getLocales().get(0);
        }
        if (!strLocale.equals(CUSTOM_LOCALE_DEFAULT) || mLocale != null) {
            mLocale = strLocale.equals(CUSTOM_LOCALE_DEFAULT)
                    ? null
                    : new Locale(localeToLanguage(strLocale), localeToCountry(strLocale));
            Locale locale = mLocale == null ? mDefaultLocale : mLocale;
            Locale.setDefault(locale);
            updateConfiguration(contextWrapper, locale);
        }
    }

    public static String localeToLanguage(String locale) {
        if (TextUtils.isEmpty(locale)) {
            return "";
        }
        int indHyphen = locale.indexOf('-');
        if (indHyphen < 1) {
            return locale;
        }
        return locale.substring(0, indHyphen);
    }

    public static String localeToCountry(String locale) {
        if (TextUtils.isEmpty(locale)) {
            return "";
        }
        int indHyphen = locale.indexOf("-r");
        if (indHyphen < 0) {
            return "";
        }
        return locale.substring(indHyphen+2);
    }

    private static void updateConfiguration(ContextWrapper contextWrapper, Locale locale) {
        Configuration configIn = contextWrapper.getBaseContext().getResources().getConfiguration();
        if (!configIn.getLocales().get(0).equals(locale)) {
            Configuration configCustom = getCustomizeConfiguration(contextWrapper.getBaseContext(), locale);
            contextWrapper.getBaseContext().getResources().updateConfiguration(configCustom,
                    contextWrapper.getBaseContext().getResources().getDisplayMetrics());
        }
    }

    static Configuration onConfigurationChanged(ContextWrapper contextWrapper, Configuration newConfig) {
        if (mLocale == null || mDefaultLocale == null) {
            mDefaultLocale = newConfig.getLocales().get(0);
        }
        return mLocale == null ? newConfig : getCustomizeConfiguration(contextWrapper.getBaseContext(), mLocale);
    }

    // Based on https://stackoverflow.com/questions/39705739/android-n-change-language-programmatically/40849142
    public static Context wrap(Context context) {
        return mLocale == null ? context : wrap(context, mLocale);
    }

    private static ContextWrapper wrap(Context context, Locale newLocale) {
        Configuration configuration = getCustomizeConfiguration(context, newLocale);
        return new ContextWrapper(context.createConfigurationContext(configuration));
    }

    private static Configuration getCustomizeConfiguration(Context context, Locale newLocale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(newLocale);

        LocaleList localeList = new LocaleList(newLocale);
        LocaleList.setDefault(localeList);
        configuration.setLocales(localeList);
        return configuration;
    }
}

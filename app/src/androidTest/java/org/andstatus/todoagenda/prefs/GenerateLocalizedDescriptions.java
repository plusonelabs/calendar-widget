package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;

import org.andstatus.todoagenda.R;
import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;

/**
 * @author yvolk@yurivolkov.com
 */
public class GenerateLocalizedDescriptions {

    @Test
    public void localizedDescriptions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        Context context1 = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String[] languages = context1.getResources().getStringArray(R.array.custom_locale_entries);
        String[] locales = context1.getResources().getStringArray(R.array.custom_locale_values);

        for(int ind=0; ind < locales.length; ind++) {
            String language = languages[ind];
            String locale = locales[ind];
            ContextWrapper context = (ContextWrapper) context1.getApplicationContext();
            MyLocale.setLocale(context, locale);
            StringBuilder builder = new StringBuilder("---- " + language + ", " + locale + "\n");
            builder.append(context.getText(R.string.app_description_80_chars_max) + "\n\n");
            builder.append(context.getText(R.string.app_description_4000_chars_max_part01) + "\n");
            builder.append(context.getText(R.string.app_description_4000_chars_max_part02) + ":\n");
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part15);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part03);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part04);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part05);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part06);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part07);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part08);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part09);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part10);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part11);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part12);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part13);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part16);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part17);
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part14);

            Log.i("todoagenda", builder.toString());
        }
    }

    private void addBulleted(StringBuilder builder, Context context, int resId) {
        builder.append("* " + context.getText(resId) + "\n");
    }
}

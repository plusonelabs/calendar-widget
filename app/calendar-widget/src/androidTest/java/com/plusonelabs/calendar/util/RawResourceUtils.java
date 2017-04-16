/*
 * Based on the example: 
 * http://stackoverflow.com/questions/4087674/android-read-text-raw-resource-file
 */

package com.plusonelabs.calendar.util;

import android.content.Context;
import android.content.res.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class RawResourceUtils {

    public static String getString(Context context, int id) throws IOException {
        return new String(getBytes(id, context), Charset.forName("UTF-8"));
    }

    /**
     * reads resources regardless of their size
     */
    private static byte[] getBytes(int id, Context context) throws IOException {
        Resources resources = context.getResources();
        InputStream is = resources.openRawResource(id);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        byte[] readBuffer = new byte[4 * 1024];

        try {
            int read;
            do {
                read = is.read(readBuffer, 0, readBuffer.length);
                if (read == -1) {
                    break;
                }
                bout.write(readBuffer, 0, read);
            } while (true);

            return bout.toByteArray();
        } finally {
            is.close();
        }
    }
}

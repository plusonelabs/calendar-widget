package com.plusonelabs.calendar.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author yvolk@yurivolkov.com
 */
public class SettingsStorage {

    private static final int BUFFER_LENGTH = 4 * 1024;

    private SettingsStorage() {
        // Not instantiable
    }

    public static void saveJson(Context context, String key, JSONObject json) throws IOException {
        writeStringToFile(json.toString(), jsonFile(context, key));
    }

    @NonNull
    public static JSONObject loadJson(Context context, String key) throws IOException {
        return getJSONObject(jsonFile(context, key));
    }

    public static void delete(Context context, String key) {
        File file = jsonFile(context, key);
        if (file.exists()) {
            file.delete();
        }
    }

    private static File jsonFile(Context context, String key) {
        return new File(getExistingPreferencesDirectory(context), key + ".json");
    }

    private static File getExistingPreferencesDirectory(Context context) {
        File dir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static void writeStringToFile(String string, File file) throws IOException {
        FileOutputStream fileOutputStream = null;
        Writer out = null;
        try {
            fileOutputStream = new FileOutputStream(file.getAbsolutePath(), false);
            out = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
            out.write(string);
        } finally {
            closeSilently(out);
            closeSilently(fileOutputStream);
        }
    }

    @NonNull
    private static JSONObject getJSONObject(File file) throws IOException {
        String fileString = utf8File2String(file);
        if (!TextUtils.isEmpty(fileString)) {
            try {
                return new JSONObject(fileString);
            } catch (JSONException e) {
                Log.v("getJSONObject", file.getAbsolutePath(), e);
            }
        }
        return new JSONObject();
    }

    private static String utf8File2String(File file) throws IOException {
        return new String(getBytes(file), Charset.forName("UTF-8"));
    }

    /**
     * Reads the whole file
     */
    private static byte[] getBytes(File file) throws IOException {
        if (file != null) {
            return getBytes(new FileInputStream(file));
        }
        return new byte[0];
    }

    /**
     * Read the stream into an array and close the stream
     **/
    private static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        if (is != null) {
            byte[] readBuffer = new byte[BUFFER_LENGTH];
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
                closeSilently(is);
            }
        }
        return new byte[0];
    }

    /**
     * Reads up to 'size' bytes, starting from 'offset'
     */
    private static byte[] getBytes(File file, int offset, int size) throws IOException {
        if (file != null) {
            InputStream is = new FileInputStream(file);
            byte[] readBuffer = new byte[size];
            try {
                long bytesSkipped = is.skip(offset);
                if (bytesSkipped < offset) {
                    throw new FileNotFoundException("Skipped only " + bytesSkipped
                            + " of " + offset + " bytes in file='" + file.getAbsolutePath() + "'");
                }
                int bytesRead = is.read(readBuffer, 0, size);
                if (bytesRead == readBuffer.length) {
                    return readBuffer;
                } else if (bytesRead > 0) {
                    return Arrays.copyOf(readBuffer, bytesRead);
                }
            } finally {
                closeSilently(is);
            }
        }
        return new byte[0];
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignored
            }
        }
    }
}

package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.prefs.InstanceSettings;

import static java.lang.Float.parseFloat;

public class RemoteViewsUtil {

    private static final String METHOD_SET_TEXT_SIZE = "setTextSize";
    private static final String METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";
    private static final String METHOD_SET_SINGLE_LINE = "setSingleLine";
    private static final String METHOD_SET_ALPHA = "setAlpha";
    private static final String METHOD_SET_COLOR_FILTER = "setColorFilter";

    private RemoteViewsUtil() {
        // prohibit instantiation
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setPadding(InstanceSettings settings, RemoteViews rv, int viewId, int leftDimenId,
                                  int topDimenId, int rightDimenId, int bottomDimenId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int leftPadding = Math.round(getScaledValueInPixel(settings, leftDimenId));
            int topPadding = Math.round(getScaledValueInPixel(settings, topDimenId));
            int rightPadding = Math.round(getScaledValueInPixel(settings, rightDimenId));
            int bottomPadding = Math.round(getScaledValueInPixel(settings, bottomDimenId));
            rv.setViewPadding(viewId, leftPadding, topPadding, rightPadding, bottomPadding);
        }
    }

    public static void setAlpha(RemoteViews rv, int viewId, int alpha) {
        rv.setInt(viewId, METHOD_SET_ALPHA, alpha);
    }

    public static void setColorFilter(RemoteViews rv, int viewId, int color) {
        rv.setInt(viewId, METHOD_SET_COLOR_FILTER, color);
    }

    public static void setTextSize(InstanceSettings settings, RemoteViews rv, int viewId, int dimenId) {
        rv.setFloat(viewId, METHOD_SET_TEXT_SIZE, getScaledValue(settings, dimenId));
    }

    public static void setTextColorFromAttr(Context context, RemoteViews rv, int viewId, int colorAttrId) {
        rv.setTextColor(viewId, getColorValue(context, colorAttrId));
    }

    public static void setBackgroundColorFromAttr(Context context, RemoteViews rv, int viewId,
                                                  int colorAttrId) {
        setBackgroundColor(rv, viewId, getColorValue(context, colorAttrId));
    }

    public static void setBackgroundColor(RemoteViews rv, int viewId, int color) {
        rv.setInt(viewId, METHOD_SET_BACKGROUND_COLOR, color);
    }

    private static float getScaledValueInPixel(InstanceSettings settings, int dimenId) {
        float resValue = getDimension(settings.getContext(), dimenId);
        float prefTextScale = parseFloat(settings.getTextSizeScale());
        return resValue * prefTextScale;
    }

    private static float getScaledValue(InstanceSettings settings, int dimenId) {
        float resValue = getDimension(settings.getEntryThemeContext(), dimenId);
        float density = settings.getEntryThemeContext().getResources().getDisplayMetrics().density;
        float prefTextScale = parseFloat(settings.getTextSizeScale());
        return resValue * prefTextScale / density;
    }

    private static int getColorValue(Context context, int attrId) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, outValue, true);
        int colorId = outValue.resourceId;
        return context.getResources().getColor(colorId);
    }

    private static float getDimension(Context context, int dimenId) {
        try {
            return context.getResources().getDimension(dimenId);
        } catch (NotFoundException e) {
            // resource might not exist
            return 0f;
        }
    }

    public static void setMultiline(RemoteViews rv, int viewId, boolean multiLine) {
        rv.setBoolean(viewId, METHOD_SET_SINGLE_LINE, !multiLine);
    }

    public static void setImageFromAttr(Context context, RemoteViews rv, int viewId, int attrResId) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(attrResId, outValue, true);
        setImage(rv, viewId, outValue.resourceId);
    }

    public static void setImage(RemoteViews rv, int viewId, int resId) {
        rv.setImageViewResource(viewId, resId);
    }
}

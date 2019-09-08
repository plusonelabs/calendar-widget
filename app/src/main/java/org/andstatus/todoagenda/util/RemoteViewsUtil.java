package org.andstatus.todoagenda.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.prefs.InstanceSettings;

import androidx.annotation.DimenRes;

import static java.lang.Float.parseFloat;

public class RemoteViewsUtil {

    private static final String METHOD_SET_TEXT_SIZE = "setTextSize";
    private static final String METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";
    private static final String METHOD_SET_SINGLE_LINE = "setSingleLine";
    private static final String METHOD_SET_ALPHA = "setAlpha";
    private static final String METHOD_SET_COLOR_FILTER = "setColorFilter";
    private static final String METHOD_SET_WIDTH = "setWidth";

    private RemoteViewsUtil() {
        // prohibit instantiation
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setPadding(InstanceSettings settings, RemoteViews rv, int viewId,
          @DimenRes int leftDimenId, @DimenRes int topDimenId, @DimenRes int rightDimenId, @DimenRes int bottomDimenId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int leftPadding = getScaledValueInPixels(settings, leftDimenId);
            int topPadding = getScaledValueInPixels(settings, topDimenId);
            int rightPadding = getScaledValueInPixels(settings, rightDimenId);
            int bottomPadding = getScaledValueInPixels(settings, bottomDimenId);
            rv.setViewPadding(viewId, leftPadding, topPadding, rightPadding, bottomPadding);
        }
    }

    public static void setAlpha(RemoteViews rv, int viewId, int alpha) {
        rv.setInt(viewId, METHOD_SET_ALPHA, alpha);
    }

    public static void setColorFilter(RemoteViews rv, int viewId, int color) {
        rv.setInt(viewId, METHOD_SET_COLOR_FILTER, color);
    }

    public static void setViewWidth(InstanceSettings settings, RemoteViews rv, int viewId, int dimenId) {
        rv.setInt(viewId, METHOD_SET_WIDTH, getScaledValueInPixels(settings, dimenId));
    }

    public static void setTextSize(InstanceSettings settings, RemoteViews rv, int viewId, int dimenId) {
        rv.setFloat(viewId, METHOD_SET_TEXT_SIZE, getScaledValueInScaledPixels(settings, dimenId));
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

    private static int getScaledValueInPixels(InstanceSettings settings, int dimenId) {
        float resValue = getDimension(settings.getContext(), dimenId);
        float prefTextScale = parseFloat(settings.getTextSizeScale());
        return  Math.round(resValue * prefTextScale);
    }

    private static float getScaledValueInScaledPixels(InstanceSettings settings, int dimenId) {
        float resValue = getDimension(settings.getContext(), dimenId);
        float density = settings.getContext().getResources().getDisplayMetrics().density;
        float prefTextScale = parseFloat(settings.getTextSizeScale());
        return resValue * prefTextScale / density;
    }

    private static int getColorValue(Context context, int attrId) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrId, outValue, true)) {
            int colorResourceId = outValue.resourceId;
            try {
                return context.getResources().getColor(colorResourceId);
            } catch (Exception e) {
                Log.w(RemoteViewsUtil.class.getSimpleName(), "context.getResources() failed to resolve color for" +
                        " resource Id:" + colorResourceId +
                        " derived from attribute Id:" + attrId, e);
                return Color.GRAY;
            }
        }
        Log.w(RemoteViewsUtil.class.getSimpleName(),
                "getColorValue failed to resolve color for attribute Id:" + attrId);
        return Color.GRAY;
    }

    private static float getDimension(Context context, int dimensionResourceId) {
        try {
            return context.getResources().getDimension(dimensionResourceId);
        } catch (NotFoundException e) {
            Log.w(RemoteViewsUtil.class.getSimpleName(), "getDimension failed for dimension resource Id:" + dimensionResourceId);
            return 0f;
        }
    }

    public static void setMultiline(RemoteViews rv, int viewId, boolean multiLine) {
        rv.setBoolean(viewId, METHOD_SET_SINGLE_LINE, !multiLine);
    }

    public static void setImageFromAttr(Context context, RemoteViews rv, int viewId, int attrResId) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, outValue, true)) {
            setImage(rv, viewId, outValue.resourceId);
        } else {
            Log.w(RemoteViewsUtil.class.getSimpleName(),
            "setImageFromAttr: not found; attrResId:" + attrResId + ", resourceId:" + outValue.resourceId +
                    ", out:" + outValue + ", context:" + context);
        }
    }

    public static void setImage(RemoteViews rv, int viewId, int resId) {
        rv.setImageViewResource(viewId, resId);
    }
}

package org.andstatus.todoagenda.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.annotation.DimenRes;
import androidx.annotation.IdRes;

import org.andstatus.todoagenda.prefs.InstanceSettings;

public class RemoteViewsUtil {
    private static final String TAG = RemoteViewsUtil.class.getSimpleName();

    private static final String METHOD_SET_TEXT_SIZE = "setTextSize";
    private static final String METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";
    private static final String METHOD_SET_SINGLE_LINE = "setSingleLine";
    private static final String METHOD_SET_ALPHA = "setAlpha";
    private static final String METHOD_SET_COLOR_FILTER = "setColorFilter";
    private static final String METHOD_SET_WIDTH = "setWidth";
    private static final String METHOD_SET_HEIGHT = "setHeight";

    private RemoteViewsUtil() {
        // prohibit instantiation
    }

    public static void setPadding(InstanceSettings settings, RemoteViews rv, @IdRes int viewId,
          @DimenRes int leftDimenId, @DimenRes int topDimenId, @DimenRes int rightDimenId, @DimenRes int bottomDimenId) {
        int leftPadding = getScaledValueInPixels(settings, leftDimenId);
        int topPadding = getScaledValueInPixels(settings, topDimenId);
        int rightPadding = getScaledValueInPixels(settings, rightDimenId);
        int bottomPadding = getScaledValueInPixels(settings, bottomDimenId);
        rv.setViewPadding(viewId, leftPadding, topPadding, rightPadding, bottomPadding);
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

    public static void setViewHeight(InstanceSettings settings, RemoteViews rv, int viewId, int dimenId) {
        rv.setInt(viewId, METHOD_SET_HEIGHT, getScaledValueInPixels(settings, dimenId));
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
        return  Math.round(resValue * settings.getTextSizeScale().scaleValue);
    }

    private static float getScaledValueInScaledPixels(InstanceSettings settings, int dimenId) {
        float resValue = getDimension(settings.getContext(), dimenId);
        float density = settings.getContext().getResources().getDisplayMetrics().density;
        return resValue * settings.getTextSizeScale().scaleValue / density;
    }

    private static int getColorValue(Context context, int attrId) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrId, outValue, true)) {
            int colorResourceId = outValue.resourceId;
            try {
                return context.getResources().getColor(colorResourceId);
            } catch (Exception e) {
                Log.w(TAG, "context.getResources() failed to resolve color for" +
                        " resource Id:" + colorResourceId +
                        " derived from attribute Id:" + attrId, e);
                return Color.GRAY;
            }
        }
        Log.w(TAG, "getColorValue failed to resolve color for attribute Id:" + attrId);
        return Color.GRAY;
    }

    private static float getDimension(Context context, int dimensionResourceId) {
        try {
            return context.getResources().getDimension(dimensionResourceId);
        } catch (NotFoundException e) {
            Log.w(TAG, "getDimension failed for dimension resource Id:" + dimensionResourceId);
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
            Log.w(TAG,
            "setImageFromAttr: not found; attrResId:" + attrResId + ", resourceId:" + outValue.resourceId +
                    ", out:" + outValue + ", context:" + context);
        }
    }

    public static void setImage(RemoteViews rv, int viewId, int resId) {
        rv.setImageViewResource(viewId, resId);
    }
}

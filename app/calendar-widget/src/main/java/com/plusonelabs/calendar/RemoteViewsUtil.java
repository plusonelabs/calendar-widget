package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.RemoteViews;

import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_TEXT_SIZE_SCALE;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_TEXT_SIZE_SCALE_DEFAULT;
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
	public static void setPadding(Context context, RemoteViews rv, int viewId, int leftDimenId,
			int topDimenId, int rightDimenId, int bottomDimenId, SharedPreferences prefs) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int leftPadding = Math.round(getScaledValueInPixel(context, leftDimenId, prefs));
			int topPadding = Math.round(getScaledValueInPixel(context, topDimenId, prefs));
			int rightPadding = Math.round(getScaledValueInPixel(context, rightDimenId, prefs));
			int bottomPadding = Math.round(getScaledValueInPixel(context, bottomDimenId, prefs));
			rv.setViewPadding(viewId, leftPadding, topPadding, rightPadding, bottomPadding);
		}
	}

	public static void setAlpha(RemoteViews rv, int viewId, int alpha) {
		rv.setInt(viewId, METHOD_SET_ALPHA, alpha);
	}

	public static void setColorFilter(RemoteViews rv, int viewId, int color) {
		rv.setInt(viewId, METHOD_SET_COLOR_FILTER, color);
	}

	public static void setTextSize(Context context, RemoteViews rv, int viewId, int dimenId, SharedPreferences prefs) {
		rv.setFloat(viewId, METHOD_SET_TEXT_SIZE, getScaledValue(context, dimenId, prefs));
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


    private static float getScaledValueInPixel(Context context, int dimenId, SharedPreferences prefs) {
        float resValue = getDimension(context, dimenId);
        float prefTextScale = parseFloat(prefs.getString(PREF_TEXT_SIZE_SCALE,
				PREF_TEXT_SIZE_SCALE_DEFAULT));
		return resValue * prefTextScale;
	}

	private static float getScaledValue(Context context, int dimenId, SharedPreferences prefs) {
        float resValue = getDimension(context, dimenId);
        float density = context.getResources().getDisplayMetrics().density;
        float prefTextScale = parseFloat(prefs.getString(PREF_TEXT_SIZE_SCALE,
				PREF_TEXT_SIZE_SCALE_DEFAULT));
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

	public static void setSingleLine(RemoteViews rv, int viewId, boolean singleLine) {
		rv.setBoolean(viewId, METHOD_SET_SINGLE_LINE, singleLine);
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

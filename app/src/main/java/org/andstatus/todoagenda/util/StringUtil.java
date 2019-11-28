package org.andstatus.todoagenda.util;

/**
 * @author yvolk@yurivolkov.com
 */
public class StringUtil {

    public static boolean nonEmpty(CharSequence value) {
        return !isEmpty(value);
    }

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }

    public static String notNull(String value) {
        return value == null ? "" : value;
    }
}

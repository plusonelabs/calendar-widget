package org.andstatus.todoagenda.calendar;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.PatternSyntaxException;

/**
 * Filter String by keywords or phrases, enclosed in single or double quotes
 * Each keyword can be prefixed with "RE:", in which case the string
 *   "CAL=<calendar_of_event>;TITLE=<title_of_event>"
 * is matched against the remaining part of the keyword (interpreted as a regex).
 * Negation is possible to indicate on the whole set of keywords (by using "!"
 * as the first keyword), and on individual keywords (by prefixing without space
 * a keyword/phrase with "!").
 *
 * @author yvolk@yurivolkov.com
 * @author github@kjdf.de
 */
public class KeywordsFilter {

    protected final List<String> keywords = new ArrayList<>();
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final String RE_PREFIX = "RE:";
    private static final String NEG_PREFIX = "!";

    public KeywordsFilter(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        boolean inQuote = false;
        char quote = ' ';
        for (int atPos = 0; atPos < text.length(); ) {
            int separatorInd = inQuote ? nextQuote(text, quote, atPos) : nextSeparatorInd(text, atPos);
            if (atPos > separatorInd) {
                break;
            }
            String item = text.substring(atPos, separatorInd);
            if (!TextUtils.isEmpty(item) && !keywords.contains(item)) {
                keywords.add(item);
            }
            if (separatorInd < text.length() && isQuote(text, separatorInd)) {
                inQuote = !inQuote;
                quote = text.charAt(separatorInd);
            }
            atPos = separatorInd + 1;
        }
    }

    private boolean isQuote(String text, int index) {
        switch (text.charAt(index)) {
            case DOUBLE_QUOTE:
            case SINGLE_QUOTE:
                return true;
            default:
                return false;
        }
    }

    private int nextQuote(String text, char quote, int atPos) {
        for (int ind = atPos; ind < text.length(); ind++) {
            if (quote == text.charAt(ind)) {
                return ind;
            }
        }
        return text.length();
    }

    private int nextSeparatorInd(String text, int atPos) {
        final String SEPARATORS = ", " + DOUBLE_QUOTE + SINGLE_QUOTE;
        for (int ind = atPos; ind < text.length(); ind++) {
            if (SEPARATORS.indexOf(text.charAt(ind)) >= 0) {
                return ind;
            }
        }
        return text.length();
    }

    public boolean matched(String s, String cal_name) {
        if (keywords.isEmpty() || TextUtils.isEmpty(s)) {
            return false;
        }
        boolean negate_whole = keywords.get(0).equals(NEG_PREFIX);
        for (ListIterator<String> it = keywords.listIterator(negate_whole ? 1 : 0); it.hasNext(); ) {
            String keyword = it.next();
            boolean matches;
            boolean negate_this = keyword.startsWith(NEG_PREFIX);
            if (negate_this) {
                keyword = keyword.substring(NEG_PREFIX.length());
            }
            if (keyword.startsWith(RE_PREFIX)) {
                keyword = keyword.substring(RE_PREFIX.length());
                try {
                    matches = ("CAL=" + cal_name + ";TITLE=" + s).matches(keyword);
                } catch (PatternSyntaxException e) {
                    matches = false;
                }
            } else {
                matches = s.contains(keyword);
            }
            if (matches ^ negate_this) {
                return !negate_whole;
            }
        }
        return negate_whole;
    }

    public boolean isEmpty() {
        return keywords.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String keyword : keywords) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            char quote = keyword.contains(String.valueOf(DOUBLE_QUOTE)) ? SINGLE_QUOTE : DOUBLE_QUOTE;
            builder.append(quote + keyword + quote);
        }
        return builder.toString();
    }
}

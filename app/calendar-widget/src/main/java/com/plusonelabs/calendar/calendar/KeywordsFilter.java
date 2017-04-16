package com.plusonelabs.calendar.calendar;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter String by keywords or phrases, enclosed in single or double quotes
 *
 * @author yvolk@yurivolkov.com
 */
public class KeywordsFilter {

    protected final List<String> keywords = new ArrayList<>();
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';

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

    public boolean matched(String s) {
        if (keywords.isEmpty() || TextUtils.isEmpty(s)) {
            return false;
        }
        for (String keyword : keywords) {
            if (s.contains(keyword)) {
                return true;
            }
        }
        return false;
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

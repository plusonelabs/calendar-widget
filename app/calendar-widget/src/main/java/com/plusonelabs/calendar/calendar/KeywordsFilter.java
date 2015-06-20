package com.plusonelabs.calendar.calendar;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/** Filter String by keywords */
public class KeywordsFilter {
    private final List<String> keywords;

    public KeywordsFilter(String rawKeywords) {
        keywords = new ArrayList<>();
        if (TextUtils.isEmpty(rawKeywords)) {
            return;
        }
        for (String item0 : rawKeywords.split("[, ]")) {
            String item = item0.trim();
            if (!TextUtils.isEmpty(item) && !keywords.contains(item)) {
                keywords.add(item);
            }
        }
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
            builder.append("\"" + keyword + "\"");
        }
        return builder.toString();
    }
}

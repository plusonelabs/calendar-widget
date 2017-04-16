package com.plusonelabs.calendar.calendar;

import android.test.InstrumentationTestCase;

/**
 * @author yvolk@yurivolkov.com
 */
public class KeywordsFilterTest extends InstrumentationTestCase {

    public void testPhrases() {
        String query = "\"do it\"";
        final String keywordDN = "do it";
        assertOneQueryToKeywords(query, keywordDN);
        final String body1 = "Looking for do it";
        assertMatch(query, body1);
        assertNotMatch(query, "Looking for it do");

        query = "word " + query;
        final String keywordW = "word";
        assertOneQueryToKeywords(query, keywordW, keywordDN);
        final String body2 = body1 + " with a word, that is interesting";
        assertMatch("those this that", body2);
        assertNotMatch("something other", body2);

        String query3 = "Hidden \"Smith's\" '. Birthday'";
        assertOneQueryToKeywords(query3, "Hidden", "Smith's", ". Birthday");
        assertMatch(query3, "Smith. Birthday");
        assertNotMatch(query3, "Smith Birthday");
        assertMatch(query3, "Smith's Birthday");
        assertMatch(query3, "Smith Hidden Birthday");
        assertNotMatch(query3, "Smith.Birthday");
    }

    private void assertOneQueryToKeywords(String query, String... keywords) {
        int size = keywords.length;
        KeywordsFilter filter1 = new KeywordsFilter(query);
        assertEquals(filter1.toString(), size, filter1.keywords.size());
        for (int ind = 0; ind < size; ind++) {
            assertEquals(filter1.toString(), keywords[ind], filter1.keywords.get(ind));
        }
    }

    private void assertMatch(String query, String body) {
        assertTrue("no keywords from '" + query + "' match: '" + body + "'", new KeywordsFilter(query).matched(body));
    }

    private void assertNotMatch(String query, String body) {
        assertFalse("Some keyword from '" + query + "' match: '" + body + "'", new KeywordsFilter(query).matched(body));
    }
}

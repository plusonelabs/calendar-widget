package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.BaseWidgetTest;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatValue;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatter;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class DateFormatterTest extends BaseWidgetTest {

    @Test
    public void timeTimeZones() {
        InstanceSettings settings = getSettings();
        settings.clock().setSnapshotMode(SnapshotMode.SNAPSHOT_TIME);
        assertNow("2020-02-15T01:00:00.000+08:00");
        assertNow("2020-02-15T23:00:00.000+08:00");
        assertNow("2020-02-29T01:00:00.000+08:00");
        assertNow("2020-02-29T23:00:00.000+08:00");
        assertNow("2020-02-15T01:00:00.000-05:00");
        assertNow("2020-02-15T23:00:00.000-05:00");
        assertNow("2020-02-29T01:00:00.000-05:00");
        assertNow("2020-02-29T23:00:00.000-05:00");
        assertNow("2025-03-01T23:00:00.000-01:00");
    }

    private void assertNow(String pattern) {
        DateTime now = DateTime.parse(pattern);
        getSettings().clock().setSnapshotDate(now);

        assertPattern(now, "MM-dd b",  String.format("%02d-%02d", now.monthOfYear().get(),
                now.dayOfMonth().get()) + " 0");

        DateTime yesterday = now.minusDays(1);
        assertPattern(yesterday, "MM-dd b",  String.format("%02d-%02d", yesterday.monthOfYear().get(),
                yesterday.dayOfMonth().get()) + " -1");
        assertPattern(yesterday, "b MM.dd",  "-1 " + String.format("%02d.%02d", yesterday.monthOfYear().get(),
                yesterday.dayOfMonth().get()));
        assertPattern(yesterday, "MM.b.dd",  String.format("%02d.-1.%02d", yesterday.monthOfYear().get(),
                yesterday.dayOfMonth().get()));

        DateTime tomorrow = now.plusDays(1);
        assertPattern(tomorrow, "MM-dd b",  String.format("%02d-%02d", tomorrow.monthOfYear().get(),
                tomorrow.dayOfMonth().get()) + " 1");
    }

    @Test
    public void customPatterns() {
        InstanceSettings settings = getSettings();
        settings.clock().setSnapshotMode(SnapshotMode.SNAPSHOT_TIME);
        DateTime now = settings.clock().now().withTimeAtStartOfDay().plusHours(1);
        settings.clock().setSnapshotDate(now);

        assertPattern(now, "yyyy-MM-dd b",  String.format("%04d-%02d-%02d", now.yearOfEra().get(),
                now.monthOfYear().get(), now.dayOfMonth().get()) + " 0");
        assertPattern(now, "b", "0");
        assertPattern(now, "MM-dd bb",  String.format("%02d-%02d", now.monthOfYear().get(), now.dayOfMonth().get()) + " 00");
        assertPattern(now.plusDays(1), "", "");
        assertPattern(now.plusDays(1), "b", "1");
        assertPattern(now.plusDays(1), "bbb", "001");
        assertPattern(now.plusDays(5), "b", "5");
        assertPattern(now.plusDays(5), "bbb", "005");
        assertPattern(now.plusDays(5), "bbbb", "5");
        assertPattern(now.plusDays(1), "'begin' b", "begin 1");
        assertPattern(now.minusDays(5), "bbb", "-05");
        assertPattern(now.minusDays(5), "bbbb", "-5");
    }

    private void assertPattern(DateTime date, String pattern, String expected) {
        DateFormatValue format = DateFormatValue.of(DateFormatType.CUSTOM, pattern);
        DateTime now = getSettings().clock().now();
        DateFormatter formatter = new DateFormatter(getSettings().getContext(), format, now);
        assertEquals("Date: " + date + ", Now:" + now + ", Pattern: [" + pattern + "]", expected, formatter.formatDate(date));
    }
}

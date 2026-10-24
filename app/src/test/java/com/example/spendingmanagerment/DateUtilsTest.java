package com.example.spendingmanagerment;

import com.example.spendingmanagerment.core.utils.DateUtils;
import java.time.LocalDate;
import java.util.TimeZone;
import org.junit.Test;
import static org.junit.Assert.*;

public class DateUtilsTest {
    @Test public void dayBoundsFollowLocalTimezoneIncludingDst() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            LocalDate springTransition = LocalDate.of(2026, 3, 8);
            long start = DateUtils.startOfDay(springTransition);
            long end = DateUtils.startOfDay(springTransition.plusDays(1));
            assertEquals(23 * 60 * 60 * 1000L, end - start);
            assertEquals(springTransition, DateUtils.fromMillis(end - 1));
            assertEquals(springTransition.plusDays(1), DateUtils.fromMillis(end));
        } finally { TimeZone.setDefault(previous); }
    }

    @Test public void monthBoundsIncludeLeapDayAndExcludeNextMonth() {
        LocalDate start = LocalDate.of(2028, 2, 1);
        LocalDate end = start.plusMonths(1);
        assertTrue(DateUtils.startOfDay(LocalDate.of(2028, 2, 29)) < DateUtils.startOfDay(end));
        assertEquals(LocalDate.of(2028, 3, 1), end);
    }
}

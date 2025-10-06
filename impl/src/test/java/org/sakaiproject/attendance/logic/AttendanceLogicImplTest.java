/*
 * Lightweight tests for AttendanceLogicImpl methods that don't require DAO wiring.
 */
package org.sakaiproject.attendance.logic;

import org.junit.Test;
import org.sakaiproject.attendance.model.AttendanceStats;
import org.sakaiproject.attendance.model.Status;

import static org.junit.Assert.*;

public class AttendanceLogicImplTest {

    @Test
    public void getStatsForStatusReturnsCorrectField() {
        AttendanceStats stats = new AttendanceStats();
        stats.setPresent(7);
        stats.setUnexcused(3);
        stats.setExcused(2);
        stats.setLate(5);
        stats.setLeftEarly(1);

        AttendanceLogicImpl logic = new AttendanceLogicImpl();

        assertEquals(7, logic.getStatsForStatus(stats, Status.PRESENT));
        assertEquals(3, logic.getStatsForStatus(stats, Status.UNEXCUSED_ABSENCE));
        assertEquals(2, logic.getStatsForStatus(stats, Status.EXCUSED_ABSENCE));
        assertEquals(5, logic.getStatsForStatus(stats, Status.LATE));
        assertEquals(1, logic.getStatsForStatus(stats, Status.LEFT_EARLY));
    }
}


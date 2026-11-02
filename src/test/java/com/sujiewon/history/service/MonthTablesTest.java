package com.sujiewon.history.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthTablesTest {

    @Test
    void oneMonthUsesThatSuffix() {
        List<String> names = MonthTables.names(
                "lgsttlogtable_",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5),
                12);
        assertEquals(List.of("lgsttlogtable_202610"), names);
    }

    @Test
    void rangeAcrossMonthsListsEachTable() {
        List<String> names = MonthTables.names(
                "lgsttlogtable_",
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 7, 1),
                12);
        assertEquals(List.of(
                "lgsttlogtable_202605",
                "lgsttlogtable_202606",
                "lgsttlogtable_202607"), names);
    }

    @Test
    void rejectsEndBeforeStart() {
        InvalidSearchException exception = assertThrows(InvalidSearchException.class, () ->
                MonthTables.names("lgsttlogtable_", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 1), 12));
        assertEquals("종료일은 시작일보다 빠를 수 없습니다.", exception.getMessage());
    }

    @Test
    void rejectsRangeOverMaxMonths() {
        InvalidSearchException exception = assertThrows(InvalidSearchException.class, () ->
                MonthTables.names("lgsttlogtable_", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), 12));
        assertEquals("조회 기간은 최대 12개월까지 가능합니다.", exception.getMessage());
    }
}

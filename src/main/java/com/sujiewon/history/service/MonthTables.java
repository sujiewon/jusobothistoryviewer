package com.sujiewon.history.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class MonthTables {

    private static final DateTimeFormatter SUFFIX = DateTimeFormatter.ofPattern("yyyyMM");
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    private MonthTables() {
    }

    public static void validatePrefix(String prefix) {
        if (prefix == null || !IDENTIFIER.matcher(prefix).matches()) {
            throw new IllegalStateException("app.table-prefix 는 영문, 숫자, 밑줄만 사용할 수 있습니다.");
        }
    }

    public static List<String> names(String prefix, LocalDate from, LocalDate to, int maxMonths) {
        validatePrefix(prefix);
        if (from == null || to == null) {
            throw new InvalidSearchException("시작일과 종료일을 입력해 주세요.");
        }
        if (to.isBefore(from)) {
            throw new InvalidSearchException("종료일은 시작일보다 빠를 수 없습니다.");
        }
        if (maxMonths < 1) {
            throw new IllegalStateException("app.max-months 는 1 이상이어야 합니다.");
        }

        YearMonth start = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);
        long months = start.until(end, ChronoUnit.MONTHS) + 1;
        if (months > maxMonths) {
            throw new InvalidSearchException("조회 기간은 최대 " + maxMonths + "개월까지 가능합니다.");
        }

        List<String> names = new ArrayList<>();
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            String name = prefix + SUFFIX.format(month);
            if (!IDENTIFIER.matcher(name).matches()) {
                throw new IllegalStateException("테이블 이름이 올바르지 않습니다.");
            }
            names.add(name);
        }
        return List.copyOf(names);
    }
}

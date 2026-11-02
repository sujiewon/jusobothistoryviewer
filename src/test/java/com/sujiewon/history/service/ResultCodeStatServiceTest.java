package com.sujiewon.history.service;

import com.sujiewon.history.config.AppProperties;
import com.sujiewon.history.domain.CodeCount;
import com.sujiewon.history.domain.ResultCodeRow;
import com.sujiewon.history.domain.StatResponse;
import com.sujiewon.history.repo.LogTableRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultCodeStatServiceTest {

    @Test
    void endDateIncludesTheWholeDay() {
        CapturingRepository repository = new CapturingRepository();
        repository.existing.add("lgsttlogtable_202610");
        repository.rows.add(new CodeCount("1", 500));
        repository.rows.add(new CodeCount("2", 250));

        StatResponse response = service(repository).search(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5));

        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0), repository.start);
        assertEquals(LocalDateTime.of(2026, 10, 6, 0, 0), repository.end);
        assertEquals(List.of("lgsttlogtable_202610"), repository.queried);
        assertEquals(750L, response.totalCount());
        assertEquals(List.of(new ResultCodeRow("1", 500), new ResultCodeRow("2", 250)), response.rows());
    }

    @Test
    void sumsTheSameCodeAcrossMonthsAndSkipsMissingTables() {
        CapturingRepository repository = new CapturingRepository();
        repository.existing.add("lgsttlogtable_202610");
        repository.existing.add("lgsttlogtable_202612");
        repository.rowsByTable.put("lgsttlogtable_202610", List.of(
                new CodeCount("2", 250),
                new CodeCount("1", 500),
                new CodeCount(null, 4)));
        repository.rowsByTable.put("lgsttlogtable_202612", List.of(
                new CodeCount("1", 3),
                new CodeCount("10", 7),
                new CodeCount("A", 2)));

        StatResponse response = service(repository).search(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 2));

        assertEquals(List.of("lgsttlogtable_202611"), response.missingTables());
        assertEquals(List.of("lgsttlogtable_202610", "lgsttlogtable_202612"), response.tables());
        assertEquals(List.of(
                new ResultCodeRow("1", 503),
                new ResultCodeRow("2", 250),
                new ResultCodeRow("10", 7),
                new ResultCodeRow("A", 2),
                new ResultCodeRow(null, 4)), response.rows());
        assertEquals(766L, response.totalCount());
    }

    @Test
    void failsWhenNoMonthlyTableExists() {
        CapturingRepository repository = new CapturingRepository();
        InvalidSearchException exception = assertThrows(InvalidSearchException.class, () ->
                service(repository).search(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5)));
        assertEquals("선택한 기간의 로그 테이블이 없습니다. (lgsttlogtable_202610)", exception.getMessage());
    }

    private static ResultCodeStatService service(CapturingRepository repository) {
        AppProperties properties = new AppProperties();
        properties.setTablePrefix("lgsttlogtable_");
        properties.setMaxMonths(12);
        return new ResultCodeStatService(repository, properties);
    }

    private static final class CapturingRepository implements LogTableRepository {
        private final List<String> existing = new ArrayList<>();
        private final List<String> queried = new ArrayList<>();
        private final java.util.Map<String, List<CodeCount>> rowsByTable = new java.util.HashMap<>();
        private final List<CodeCount> rows = new ArrayList<>();
        private LocalDateTime start;
        private LocalDateTime end;

        @Override
        public boolean exists(String tableName) {
            return existing.contains(tableName);
        }

        @Override
        public List<CodeCount> countByResultCode(
                String tableName,
                LocalDateTime startInclusive,
                LocalDateTime endExclusive) {
            queried.add(tableName);
            start = startInclusive;
            end = endExclusive;
            if (rowsByTable.containsKey(tableName)) {
                return rowsByTable.get(tableName);
            }
            return rows;
        }
    }
}

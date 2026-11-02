package com.sujiewon.history.service;

import com.sujiewon.history.config.AppProperties;
import com.sujiewon.history.domain.CodeCount;
import com.sujiewon.history.domain.ResultCodeRow;
import com.sujiewon.history.domain.StatResponse;
import com.sujiewon.history.repo.LogTableRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResultCodeStatService {

    private static final Logger log = LoggerFactory.getLogger(ResultCodeStatService.class);

    private final LogTableRepository logTableRepository;
    private final AppProperties appProperties;

    public ResultCodeStatService(LogTableRepository logTableRepository, AppProperties appProperties) {
        this.logTableRepository = logTableRepository;
        this.appProperties = appProperties;
    }

    @PostConstruct
    void validateConfiguration() {
        MonthTables.validatePrefix(appProperties.getTablePrefix());
        if (appProperties.getMaxMonths() < 1) {
            throw new IllegalStateException("app.max-months 는 1 이상이어야 합니다.");
        }
    }

    @Transactional(readOnly = true)
    public StatResponse search(LocalDate from, LocalDate to) {
        List<String> candidates = MonthTables.names(
                appProperties.getTablePrefix(),
                from,
                to,
                appProperties.getMaxMonths());
        LocalDateTime startInclusive = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();

        List<String> used = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        Map<String, Long> counts = new HashMap<>();
        for (String tableName : candidates) {
            if (!logTableRepository.exists(tableName)) {
                missing.add(tableName);
                continue;
            }
            used.add(tableName);
            for (CodeCount row : logTableRepository.countByResultCode(tableName, startInclusive, endExclusive)) {
                counts.merge(row.resultCode(), row.count(), Long::sum);
            }
        }

        if (used.isEmpty()) {
            throw new InvalidSearchException(
                    "선택한 기간의 로그 테이블이 없습니다. (" + String.join(", ", missing) + ")");
        }

        List<ResultCodeRow> rows = toRows(counts);
        long total = 0L;
        for (ResultCodeRow row : rows) {
            total += row.count();
        }
        log.info("결과코드 집계 from={} to={} tables={} total={}", from, to, used, total);
        return new StatResponse(from, to, total, List.copyOf(used), List.copyOf(missing), rows);
    }

    private static List<ResultCodeRow> toRows(Map<String, Long> counts) {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((left, right) -> compareCode(left.getKey(), right.getKey()));
        List<ResultCodeRow> rows = new ArrayList<>();
        for (Map.Entry<String, Long> entry : entries) {
            rows.add(new ResultCodeRow(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(rows);
    }

    static int compareCode(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        Long leftNumber = parseWholeNumber(left);
        Long rightNumber = parseWholeNumber(right);
        if (leftNumber != null && rightNumber != null) {
            int compared = leftNumber.compareTo(rightNumber);
            if (compared != 0) {
                return compared;
            }
        } else if (leftNumber != null) {
            return -1;
        } else if (rightNumber != null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private static Long parseWholeNumber(String value) {
        if (value.isEmpty()) {
            return null;
        }
        int start = 0;
        if (value.charAt(0) == '-' || value.charAt(0) == '+') {
            if (value.length() == 1) {
                return null;
            }
            start = 1;
        }
        for (int index = start; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return null;
            }
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}

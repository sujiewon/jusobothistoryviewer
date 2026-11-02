package com.sujiewon.history.domain;

import java.time.LocalDate;
import java.util.List;

public record StatResponse(
        LocalDate from,
        LocalDate to,
        long totalCount,
        List<String> tables,
        List<String> missingTables,
        List<ResultCodeRow> rows) {
}

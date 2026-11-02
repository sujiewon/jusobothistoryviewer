package com.sujiewon.history.repo;

import com.sujiewon.history.domain.CodeCount;

import java.time.LocalDateTime;
import java.util.List;

public interface LogTableRepository {

    boolean exists(String tableName);

    List<CodeCount> countByResultCode(String tableName, LocalDateTime startInclusive, LocalDateTime endExclusive);
}

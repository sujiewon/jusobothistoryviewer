package com.sujiewon.history.repo;

import com.sujiewon.history.domain.CodeCount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class JdbcLogTableRepository implements LogTableRepository {

    private static final DateTimeFormatter SQL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    private final JdbcTemplate jdbcTemplate;

    public JdbcLogTableRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(String tableName) {
        requireIdentifier(tableName);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        return count != null && count > 0;
    }

    @Override
    public List<CodeCount> countByResultCode(
            String tableName,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive) {
        requireIdentifier(tableName);
        // 테이블 이름은 접두사와 연월로만 만든 식별자다. 날짜 값은 바인딩한다.
        // 종료 시각은 다음날 0시라서 종료일 당일이 모두 포함된다.
        String sql = "SELECT `결과코드` AS result_code, COUNT(*) AS cnt FROM `" + tableName
                + "` WHERE `로그시간` >= ? AND `로그시간` < ? GROUP BY `결과코드`";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CodeCount(rs.getString("result_code"), rs.getLong("cnt")),
                SQL_DATE_TIME.format(startInclusive),
                SQL_DATE_TIME.format(endExclusive));
    }

    private static void requireIdentifier(String tableName) {
        if (tableName == null || !IDENTIFIER.matcher(tableName).matches()) {
            throw new IllegalArgumentException("테이블 이름이 올바르지 않습니다.");
        }
    }
}

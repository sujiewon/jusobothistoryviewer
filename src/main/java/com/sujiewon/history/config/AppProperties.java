package com.sujiewon.history.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 월별 로그 테이블 접두사. 2026년 5월 테이블은 {@code lgsttlogtable_202605} 이다.
     */
    private String tablePrefix = "lgsttlogtable_";

    /**
     * 한 번에 조회할 수 있는 최대 개월 수.
     */
    private int maxMonths = 12;

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public int getMaxMonths() {
        return maxMonths;
    }

    public void setMaxMonths(int maxMonths) {
        this.maxMonths = maxMonths;
    }
}

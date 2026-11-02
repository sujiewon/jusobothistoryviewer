package com.sujiewon.history;

import com.sujiewon.history.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class HistoryViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistoryViewerApplication.class, args);
    }
}

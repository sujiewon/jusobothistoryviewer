package com.sujiewon.history.web;

import com.sujiewon.history.domain.StatResponse;
import com.sujiewon.history.service.ResultCodeStatService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class ResultCodeStatController {

    private final ResultCodeStatService resultCodeStatService;

    public ResultCodeStatController(ResultCodeStatService resultCodeStatService) {
        this.resultCodeStatService = resultCodeStatService;
    }

    @GetMapping("/api/result-codes")
    public StatResponse search(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return resultCodeStatService.search(from, to);
    }
}

package com.sujiewon.history.web;

import com.sujiewon.history.domain.ResultCodeRow;
import com.sujiewon.history.domain.StatResponse;
import com.sujiewon.history.service.InvalidSearchException;
import com.sujiewon.history.service.ResultCodeStatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResultCodeStatController.class)
class ResultCodeStatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResultCodeStatService resultCodeStatService;

    @Test
    void returnsCountsForTheSelectedPeriod() throws Exception {
        given(resultCodeStatService.search(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5)))
                .willReturn(new StatResponse(
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 5),
                        750,
                        List.of("lgsttlogtable_202610"),
                        List.of(),
                        List.of(new ResultCodeRow("1", 500), new ResultCodeRow("2", 250))));

        mockMvc.perform(get("/api/result-codes")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(750))
                .andExpect(jsonPath("$.tables[0]").value("lgsttlogtable_202610"))
                .andExpect(jsonPath("$.rows[0].resultCode").value("1"))
                .andExpect(jsonPath("$.rows[0].count").value(500))
                .andExpect(jsonPath("$.rows[1].resultCode").value("2"))
                .andExpect(jsonPath("$.rows[1].count").value(250));
    }

    @Test
    void returnsTheValidationMessage() throws Exception {
        given(resultCodeStatService.search(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 1)))
                .willThrow(new InvalidSearchException("종료일은 시작일보다 빠를 수 없습니다."));

        mockMvc.perform(get("/api/result-codes")
                        .param("from", "2026-10-05")
                        .param("to", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}

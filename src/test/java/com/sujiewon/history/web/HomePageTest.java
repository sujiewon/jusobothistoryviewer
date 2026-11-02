package com.sujiewon.history.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HomePageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexHasTheSearchForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("발화로그 결과코드 집계")))
                .andExpect(content().string(containsString("시작일")))
                .andExpect(content().string(containsString("종료일")))
                .andExpect(content().string(containsString("조회")))
                .andExpect(content().string(containsString("lgsttlogtable_YYYYMM")));
    }
}

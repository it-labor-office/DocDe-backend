package com.docde.domain.search.controller;

import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.config.WithMockAuthUser;
import com.docde.domain.search.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
public class SearchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "searchService")
    private SearchService searchService;

    @DisplayName("[GET] /hospitals/legacy/search")
    @Test
    @WithMockAuthUser
    void test1() throws Exception {
        // given
        when(searchService.searchHospitalLegacy(any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/hospitals/legacy/search")
                        .with(csrf())
                        .param("q", "search"))
                .andExpect(status().isOk());
    }

    @DisplayName("[GET] /hospitals/search")
    @Test
    @WithMockAuthUser
    void test2() throws Exception {
        // given
        when(searchService.searchHospitalLegacy(any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/hospitals/search")
                        .with(csrf())
                        .param("q", "search"))
                .andExpect(status().isOk());
    }
}

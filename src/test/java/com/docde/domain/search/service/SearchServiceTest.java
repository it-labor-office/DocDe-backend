package com.docde.domain.search.service;

import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalDocument;
import com.docde.domain.hospital.repository.HospitalElasticSearchRepository;
import com.docde.domain.hospital.repository.HospitalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {
    @InjectMocks
    SearchService searchService;

    @Mock
    HospitalRepository hospitalRepository;

    @Mock
    HospitalElasticSearchRepository hospitalElasticSearchRepository;

    @DisplayName("SearchService::searchHospitalLegacy")
    @Test
    void test1() {
        // given
        Page<Hospital> hospitalPage = Page.empty();
        when(hospitalRepository.findAllByQuery(any(), any())).thenReturn(hospitalPage);
        String query = "";
        Integer page = 0;
        Integer size = 10;

        // when
        Page<Hospital> hospitalPage1 = searchService.searchHospitalLegacy(query, page, size);

        // then
        assertEquals(hospitalPage1, hospitalPage);
    }

    @DisplayName("SearchService::searchHospital")
    @Test
    void test2() {
        // given
        Page<HospitalDocument> hospitalPage = Page.empty();
        when(hospitalElasticSearchRepository.findAllByQuery(any(), any())).thenReturn(hospitalPage);
        String query = "";
        Integer page = 0;
        Integer size = 10;

        // when
        Page<HospitalDocument> hospitalPage1 = searchService.searchHospital(query, page, size);

        // then
        assertEquals(hospitalPage1, hospitalPage);
    }
}

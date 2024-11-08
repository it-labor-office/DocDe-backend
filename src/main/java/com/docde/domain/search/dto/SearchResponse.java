package com.docde.domain.search.dto;

import com.docde.domain.search.dto.SearchResponse.SearchHospital;

import java.time.LocalTime;

public sealed interface SearchResponse permits SearchHospital {
    record SearchHospital(Long id, String name, String address, String contact,
                          LocalTime openTime, LocalTime closingTime) implements SearchResponse {
    }
}

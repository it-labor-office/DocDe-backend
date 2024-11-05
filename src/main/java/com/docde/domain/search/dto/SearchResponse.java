package com.docde.domain.search.dto;

import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.search.dto.SearchResponse.SearchHospital;

import java.time.LocalTime;
import java.util.List;

public sealed interface SearchResponse permits SearchHospital {
    record SearchHospital(Long id, String name, String address, String contact,
                          LocalTime openTime, LocalTime closingTime,
                          List<DoctorResponse> doctors) implements SearchResponse {
    }
}

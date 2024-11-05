package com.docde.domain.search.service;

import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalDocument;
import com.docde.domain.hospital.repository.HospitalElasticSearchRepository;
import com.docde.domain.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final HospitalRepository hospitalRepository;
    private final HospitalElasticSearchRepository hospitalElasticSearchRepository;

    public Page<Hospital> searchHospitalLegacy(String query, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return hospitalRepository.findAllByQuery(query, pageable);
    }

    public Page<HospitalDocument> searchHospital(String query, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return hospitalElasticSearchRepository.findAllByName(query, pageable);
    }
}

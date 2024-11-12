package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HospitalCustomRepository {
    Page<Hospital> findAllByQuery(String query, Pageable pageable);
}

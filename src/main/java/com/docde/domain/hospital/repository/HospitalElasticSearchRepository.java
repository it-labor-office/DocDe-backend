package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.HospitalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalElasticSearchRepository extends ElasticsearchRepository<HospitalDocument, Long> {
    // 병원 이름 검색하는 쿼리
    Page<HospitalDocument> findAllByName(String name, Pageable pageable);
}

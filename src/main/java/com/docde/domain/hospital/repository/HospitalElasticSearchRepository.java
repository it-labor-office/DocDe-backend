package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.HospitalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalElasticSearchRepository extends ElasticsearchRepository<HospitalDocument, String> {
    // 병원 이름 검색하는 쿼리

    @Query("""
            {
              "bool": {
                "should": [
                  { "match": { "name": "?0" }},
                  { "nested": {
                      "path": "doctors",
                      "query": { "match": { "doctors.name": "?0" }}
                    }},
                  { "nested": {
                      "path": "doctors",
                      "query": { "match": { "doctors.medicalDepartment": "?0" }}
                    }}
                ],
                "minimum_should_match": 1
              }
            }
            }
            """)
    Page<HospitalDocument> findAllByQuery(String name, Pageable pageable);
}

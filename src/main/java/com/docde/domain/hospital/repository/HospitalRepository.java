package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long>, HospitalCustomRepository {
    Optional<Hospital> findFirstByOrderByIdDesc();
}

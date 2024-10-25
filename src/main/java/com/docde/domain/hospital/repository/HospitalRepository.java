package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    @Modifying(clearAutomatically = true)
    @Query("update Hospital h set h.name = :newName where h.Id = :Id")
    void updateHospitalName(Long Id, String newName);
}

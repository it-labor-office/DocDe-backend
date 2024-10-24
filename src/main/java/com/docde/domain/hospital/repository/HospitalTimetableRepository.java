package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.HospitalTimetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalTimetableRepository extends JpaRepository<HospitalTimetable, Long> {
    @Modifying
    @Query("DELETE from HospitalTimetable ht where ht.hospital.Id = :hospitalId")
    void findByHospitalId(@Param("hospitalId")Long hospitalId);
}

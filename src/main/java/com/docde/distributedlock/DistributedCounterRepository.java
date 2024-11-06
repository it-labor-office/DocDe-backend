/*
package com.docde.distributedlock;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface DistributedCounterRepository extends JpaRepository<DistributedCounter, Long> {

    Optional<DistributedCounter> findByDoctorIdAndReservationTime(Long doctorId, LocalDateTime reservationTime);

    @Modifying
    @Query("UPDATE DistributedCounter dc SET dc.count = dc.count + 1 WHERE dc.id = :counterId")
    int incrementDoctorReservationCount(@Param("counterId") Long counterId);

}*/

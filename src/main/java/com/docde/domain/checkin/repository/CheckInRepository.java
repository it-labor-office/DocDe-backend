package com.docde.domain.checkin.repository;

import com.docde.domain.checkin.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
}

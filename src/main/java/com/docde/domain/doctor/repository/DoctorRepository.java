package com.docde.domain.doctor.repository;

import com.docde.domain.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser_Id(Long user_id);

    Optional<Doctor> findByUser_Email(String email);

}

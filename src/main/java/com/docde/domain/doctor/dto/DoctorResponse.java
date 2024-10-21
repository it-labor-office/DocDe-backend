package com.docde.domain.doctor.dto;

import com.docde.domain.doctor.entity.Doctor;

public record DoctorResponse(Long id, String name, String description) {
    public DoctorResponse(Doctor doctor) {
        this(doctor.getId(), doctor.getName(), doctor.getDescription());
    }
}

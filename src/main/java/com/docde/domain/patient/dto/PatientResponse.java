package com.docde.domain.patient.dto;

import com.docde.common.enums.Gender;
import com.docde.domain.patient.entity.Patient;

public record PatientResponse(Long id, String name, String address, String phone, Gender gender) {
    public PatientResponse(Patient patient) {
        this(patient.getId(), patient.getName(), patient.getAddress(), patient.getPhone(), patient.getGender());
    }
}

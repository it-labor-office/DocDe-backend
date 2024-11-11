package com.docde.domain.doctor.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
public class DoctorDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long, index = false)
    private Long doctorId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String medicalDepartment;

    @Builder
    public DoctorDocument(String id, Long doctorId, String name, String medicalDepartment) {
        this.id = id;
        this.doctorId = doctorId;
        this.name = name;
        this.medicalDepartment = medicalDepartment;
    }

    public static DoctorDocument from(Doctor doctor) {
        return DoctorDocument.builder()
                .id(doctor.getId().toString())
                .doctorId(doctor.getId())
                .name(doctor.getName())
                .medicalDepartment(doctor.getMedicalDepartment())
                .build();
    }

}

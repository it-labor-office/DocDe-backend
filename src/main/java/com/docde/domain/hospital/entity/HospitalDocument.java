package com.docde.domain.hospital.entity;

import com.docde.domain.doctor.entity.DoctorDocument;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Document(indexName = "hospitals")
@NoArgsConstructor
public class HospitalDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long, index = false)
    private Long hospitalId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Nested)
    private List<DoctorDocument> doctors;

    @Builder
    public HospitalDocument(String id, Long hospitalId, String name, String address, List<DoctorDocument> doctors) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
        this.doctors = doctors;
    }

    public static HospitalDocument from(Hospital hospital) {
        return HospitalDocument.builder()
                .id(hospital.getId().toString())
                .hospitalId(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .doctors(hospital.getDoctors().stream().map(DoctorDocument::from).toList())
                .build();
    }
}

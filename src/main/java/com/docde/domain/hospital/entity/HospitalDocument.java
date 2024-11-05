package com.docde.domain.hospital.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalTime;

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

    @Field(type = FieldType.Text, index = false)
    private String address;

    @Field(type = FieldType.Text, index = false)
    private String contact;

    @Field(type = FieldType.Text, index = false)
    private String openTime;

    @Field(type = FieldType.Text, index = false)
    private String closingTime;

    @Field(type = FieldType.Boolean, index = false)
    private boolean deleted = false;

    @Field(type = FieldType.Text, index = false)
    private String announcement;

    @Builder
    public HospitalDocument(String id, Long hospitalId, String name, String address, String contact, LocalTime openTime, LocalTime closingTime, boolean deleted, String announcement) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.openTime = openTime.toString();
        this.closingTime = closingTime.toString();
        this.deleted = deleted;
        this.announcement = announcement;
    }

    public static HospitalDocument from(Hospital hospital) {
        return HospitalDocument.builder()
                .id(hospital.getId().toString())
                .hospitalId(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .contact(hospital.getContact())
                .openTime(hospital.getOpenTime())
                .closingTime(hospital.getClosingTime())
                .deleted(hospital.isDeleted())
                .announcement(hospital.getAnnouncement())
                .build();
    }
}

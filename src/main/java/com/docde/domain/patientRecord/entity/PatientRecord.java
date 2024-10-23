package com.docde.domain.patientRecord.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patientRecords")
@NoArgsConstructor
@Getter
public class PatientRecord {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientRecordsId;
}

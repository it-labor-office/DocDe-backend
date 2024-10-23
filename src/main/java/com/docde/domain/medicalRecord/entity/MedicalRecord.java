package com.docde.domain.medicalRecord.entity;

import com.docde.common.entity.Timestamped;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "patientRecords")
@NoArgsConstructor
@Getter
public class MedicalRecord extends Timestamped {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientRecordId;

    private String description;

    private LocalDateTime consultation;

    private String treatmentPlan; // 치료 계획

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    public MedicalRecord(String description, LocalDateTime consultation,
                         Patient patient, Doctor doctor, String treatmentPlan) {

         this.description = description;
         this.consultation = consultation;
         this.patient = patient;
         this.doctor = doctor;
         this.treatmentPlan = treatmentPlan;
    }
}

package com.docde.domain.reservation.entity;

import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Reservation extends Timestamped {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reservationReason;

    @Column(nullable = false)
    @Setter
    private ReservationStatus status;

    @Column
    @Setter
    private String rejectReason;

    @Column(nullable = false)
    private LocalDateTime reservationTime;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Builder
    public Reservation(String reservationReason, ReservationStatus status, String rejectReason, Doctor doctor, Patient patient, LocalDateTime reservationTime) {
        this.reservationReason = reservationReason;
        this.status = status;
        this.rejectReason = rejectReason;
        this.doctor = doctor;
        this.patient = patient;
        this.reservationTime = reservationTime;
    }
}

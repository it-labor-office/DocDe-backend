package com.docde.domain.reservation.entity;

import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    public Reservation(String reservation_reason, ReservationStatus reservationStatus, Doctor doctor, Patient patient) {
        this.reservationReason = reservation_reason;
        this.status = reservationStatus;
        this.doctor = doctor;
        this.patient = patient;
    }


    public static Reservation createReservation(String reservation_reason, ReservationStatus reservationStatus, Doctor doctor, Patient patient) {
        return new Reservation(reservation_reason, reservationStatus, doctor, patient);
    }

    public void changeReservationStatus(ReservationStatus reservationStatus) {
        this.status = reservationStatus;
    }

    @Builder
    public Reservation(String reservation_reason, ReservationStatus reservationStatus, String rejectionReason, Doctor doctor, Patient patient) {
        this.reservationReason = reservation_reason;
        this.status = reservationStatus;
        this.rejectReason = rejectionReason;
        this.doctor = doctor;
        this.patient = patient;
    }
}

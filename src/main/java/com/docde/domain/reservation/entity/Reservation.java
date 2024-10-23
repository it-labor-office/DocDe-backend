package com.docde.domain.reservation.entity;

import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Reservation extends Timestamped {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reservation_reason;

    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Builder
    private Reservation(String reservation_reason, ReservationStatus reservationStatus, Doctor doctor, Patient patient){
        this.reservation_reason = reservation_reason;
        this.reservationStatus = reservationStatus;
        this.doctor = doctor;
        this.patient = patient;
    }


    public static Reservation createReservation(String reservation_reason, ReservationStatus reservationStatus, Doctor doctor, Patient patient){
        return Reservation.builder()
                .reservation_reason(reservation_reason)
                .reservationStatus(reservationStatus)
                .doctor(doctor)
                .patient(patient)
                .build();
    }

    public void cancelReservation(ReservationStatus reservationStatus){
        Reservation.builder()
                .reservationStatus(reservationStatus)
                .build();
    }

    public void approveReservation(ReservationStatus reservationStatus){
        Reservation.builder()
                .reservationStatus(reservationStatus)
                .build();
    }

    public void rejectReservation(ReservationStatus reservationStatus){
        Reservation.builder()
                .reservationStatus(reservationStatus)
                .build();
    }

}

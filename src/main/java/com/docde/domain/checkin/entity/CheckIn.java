package com.docde.domain.checkin.entity;

import com.docde.common.entity.Timestamped;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class CheckIn extends Timestamped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private CheckinStatus checkinStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "dotcor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "patient_id")
    private Patient patient;
}

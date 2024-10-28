package com.docde.domain.medicalRecord.repository;

import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.medicalRecord.repository.MedicalRecordRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.docde.domain.medicalRecord.entity.QMedicalRecord.medicalRecord;


@Repository
public class MedicalRecordRepositoryImpl implements MedicalRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MedicalRecordRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<MedicalRecord> findSpecificMedicalRecord(
            Long medicalRecordId, String description, String treatmentPlan, String doctorComment) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(medicalRecord.medicalRecordId.eq(medicalRecordId));

        if (description != null && !description.isEmpty()) {
            builder.and(medicalRecord.description.contains(description));
        }
        if (treatmentPlan != null && !treatmentPlan.isEmpty()) {
            builder.and(medicalRecord.treatmentPlan.contains(treatmentPlan));
        }
        if (doctorComment != null && !doctorComment.isEmpty()) {
            builder.and(medicalRecord.doctorComment.contains(doctorComment));
        }

        return Optional.ofNullable(
                queryFactory.selectFrom(medicalRecord)
                        .where(builder)
                        .fetchOne()
        );

    }
}

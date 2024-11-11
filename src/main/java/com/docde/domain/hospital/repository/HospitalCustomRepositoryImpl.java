package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.Hospital;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.function.LongSupplier;

import static com.docde.domain.hospital.entity.QHospital.hospital;


@RequiredArgsConstructor
public class HospitalCustomRepositoryImpl implements HospitalCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 병원 이름 중 query를 포함하는 병원을 반환한다.
     */
    @Override
    public Page<Hospital> findAllByQuery(String query, Pageable pageable) {
        List<Hospital> hospitals = getContent(query, pageable);
        return PageableExecutionUtils.getPage(hospitals, pageable, getCount(query));
    }

    private List<Hospital> getContent(String query, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(hospital)
                .where(hospital.deleted.eq(Boolean.FALSE),
                        hospital.name.contains(query))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private LongSupplier getCount(String query) {
        return () -> {
            Long count = jpaQueryFactory
                    .select(Wildcard.count)
                    .from(hospital)
                    .where(hospital.deleted.eq(Boolean.FALSE),
                            hospital.name.contains(query))
                    .fetchOne();
            return count == null ? 0 : count;
        };
    }


}

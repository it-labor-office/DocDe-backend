package com.docde.domain.hospital.repository;

import com.docde.domain.hospital.entity.HospitalTimetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalTimetableRepository extends JpaRepository<HospitalTimetable, Long> {
    /*@Modifying은 JPA의 영속성컨텍스트의 1차캐시를 무시하고 변경하기 때문에 clearAutomatically로 영속성 컨텍스트를
    초기화 시켜주지않으면 쿼리로인해 값이 변경되어도 영속성 컨텍스트에 있는 값을 꺼내쓰기때문에(변경값이 감지되지않음)
    쿼리가 실행된후 영속성컨텍스트를 비워주는 작업을 같이 진행해야 합니다.
     */
    // 이 기능이 다른 1차 캐시도 날린다면 주의해서 사용해야할 옵션 추후에 찾아보자
    @Modifying(clearAutomatically = true)
    @Query("DELETE from HospitalTimetable ht where ht.hospital.id = :hospitalId")
    void deleteAllByHospitalId(@Param("hospitalId") Long hospitalId);

    @Query("SELECT ht from HospitalTimetable ht where ht.hospital.id = :hospitalId")
    List<HospitalTimetable> findAllByHospitalId(@Param("hospitalId") Long hospitalId);
}

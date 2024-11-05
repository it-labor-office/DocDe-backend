package com.docde.domain.hospital;

import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalDocument;
import com.docde.domain.hospital.repository.HospitalRepository;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
//@Disabled
public class HospitalSearchTest {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    public void bulkInsertHospital() {
        List<Hospital> hospitals = new ArrayList<>();
        Optional<Hospital> hospitalOptional = hospitalRepository.findFirstByOrderByIdDesc();
        int hospitalStartIndex = 0;
        if (hospitalOptional.isPresent())
            hospitalStartIndex = hospitalOptional.get().getId().intValue();

        for (int i = hospitalStartIndex; i < hospitalStartIndex + 1000000; i++) {
            Hospital hospital = Hospital.builder()
                    .name(String.valueOf(UUID.randomUUID()))
                    .address(String.valueOf(UUID.randomUUID()))
                    .contact(String.valueOf(UUID.randomUUID()))
                    .openTime(LocalDateTime.now().toLocalTime())
                    .closingTime(LocalDateTime.now().toLocalTime())
                    .announcement(String.valueOf(UUID.randomUUID()))
                    .build();

            ReflectionTestUtils.setField(hospital, "id", (long) i);
            ReflectionTestUtils.setField(hospital, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(hospital, "modifiedAt", LocalDateTime.now());
            hospitals.add(hospital);
        }

        String hospitalSql = """
                    INSERT INTO hospital (name, address, contact, open_time, closing_time, announcement, deleted, created_at, modified_at)
                    VALUES (:name, :address, :contact, :openTime, :closingTime, :announcement, :deleted, :createdAt, :modifiedAt)
                """;
        SqlParameterSource[] hospitalParams = hospitals
                .stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(hospitalSql, hospitalParams);
    }

    @Test
    public void bulkInsertDoctor() {
        List<Doctor> doctors = new ArrayList<>();
        List<Hospital> savedHospitals = hospitalRepository.findAll();
        Optional<Doctor> doctorOptional = doctorRepository.findFirstByOrderByIdDesc();
        int doctorStartIndex = 0;
        if (doctorOptional.isPresent())
            doctorStartIndex = doctorOptional.get().getId().intValue();


        for (int i = doctorStartIndex; i < 1000000 + doctorStartIndex; i++) {
            Hospital hospital = savedHospitals.get(i - doctorStartIndex);
            Doctor doctor = Doctor
                    .builder()
                    .name(String.valueOf(UUID.randomUUID()))
                    .description(String.valueOf(UUID.randomUUID()))
                    .hospital(hospital)
                    .build();
            ReflectionTestUtils.setField(doctor, "id", (long) i);
            ReflectionTestUtils.setField(doctor, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(doctor, "modifiedAt", LocalDateTime.now());
            doctors.add(doctor);
        }

        String doctorSql = """
                INSERT INTO doctor (name, description, hospital_id, deleted, created_at, modified_at)
                VALUES (:name, :description, :hospitalId, :deleted, :createdAt, :modifiedAt)
                """;
        SqlParameterSource[] doctorParams = doctors
                .stream()
                .map(doctor -> {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("name", doctor.getName());
                    paramMap.put("description", doctor.getDescription());
                    paramMap.put("hospitalId", doctor.getHospital().getId()); // Hospital 엔티티에서 ID 추출
                    paramMap.put("deleted", doctor.getDeleted());
                    paramMap.put("createdAt", doctor.getCreatedAt());
                    paramMap.put("modifiedAt", doctor.getModifiedAt());
                    return new MapSqlParameterSource(paramMap);
                })
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(doctorSql, doctorParams);
    }

    @RepeatedTest(100)
    public void bulkInsertDocument(RepetitionInfo repetitionInfo) {
        System.out.println("start");
        Pageable pageable = PageRequest.of(repetitionInfo.getCurrentRepetition() - 1, 1000000 / 100);
        Page<Hospital> savedHospitals = hospitalRepository.findAll(pageable);
        List<HospitalDocument> hospitalDocuments = new ArrayList<>();

        System.out.println("foreach start");
        for (Hospital hospital : savedHospitals) {
            HospitalDocument hospitalDocument = HospitalDocument.from(hospital);
            hospitalDocuments.add(hospitalDocument);
        }
        System.out.println("map start");
        List<IndexQuery> queryList = hospitalDocuments.stream()
                .map(hospitalDocument ->
                        new IndexQueryBuilder()
                                .withId(hospitalDocument.getId().toString())
                                .withObject(hospitalDocument).build())
                .collect(Collectors.toList());

        System.out.println("bulk start");
        elasticsearchOperations
                .bulkIndex(queryList, IndexCoordinates.of("hospitals"));
        System.out.println("end start");
    }

}

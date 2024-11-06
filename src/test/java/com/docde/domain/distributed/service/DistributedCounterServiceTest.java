/*
package com.docde.domain.distributed.service;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.reservation.repository.ReservationRepository;
import com.docde.domain.reservation.service.ReservationPatientService;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.docde.domain.user.entity.QUser.user;
import static org.junit.jupiter.api.Assertions.assertEquals;


//@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class DistributedCounterServiceTest {

//    @InjectMocks

    @Autowired
    ReservationPatientService reservationPatientService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    HospitalRepository hospitalRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    private static final LocalDateTime VALID_RESERVATION_TIME = LocalDateTime.now();


    */
/*@Test
    void 카운터_저장() {
        // given
        DistributedCounter counter = new DistributedCounter(VALID_DOCTOR_ID, 0, VALID_RESERVATION_TIME);

        // when
        distributedCounterService.save(counter);

        // then
        verify(distributedCounterRepository, times(1)).save(counter);
    }*//*




*/
/*    @Test
    void 카운터가_존재하지_않을_경우_새로_생성() {
        // given
        given(distributedCounterRepository.findByDoctorIdAndReservationTime(VALID_DOCTOR_ID, VALID_RESERVATION_TIME))
                .willReturn(Optional.empty());

        // when
        Long counterId = distributedCounterService.getCounterIdForDoctor(VALID_DOCTOR_ID, VALID_RESERVATION_TIME);

        // then
        verify(distributedCounterRepository, times(1)).save(any(DistributedCounter.class));
        assertNotNull(counterId);
    }*//*



    @Test
    void testConcurrencyWithDifferentUsers() throws InterruptedException {

        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("name")
                .address("address")
                .contact("123-456-7890")
                .open_time(LocalTime.of(0, 0))
                .closing_time(LocalTime.of(23, 59))
                .announcement("announcement")
                .build());
            // 병원 및 의사 생성
            User doctorUser = userRepository.save(User.builder()
                    .email("doctor106@hospital.com")
                    .password("password")
                    .userRole(UserRole.ROLE_DOCTOR)
                    .build());



            Doctor doctor = doctorRepository.save(Doctor.builder()
                    .name("DoctorName")
                    .description("Description")
                    .hospital(hospital)
                    .user(doctorUser)
                    .build());

            LocalDateTime reservationTime = LocalDateTime.now().plusHours(3);

            // 5명의 서로 다른 환자 생성
            List<Patient> patients = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                // 각 환자 User와 Patient 생성
                User user = userRepository.save(User.builder()
                        .email("patient106" + i + "@example.com")
                        .password("password" + i)
                        .userRole(UserRole.ROLE_PATIENT)
                        .build());

                Patient patient = patientRepository.save(Patient.builder()
                        .name("Patient" + i)
                        .address("Address")
                        .phone("010-1234-567" + i)
                        .gender(Gender.M)
                        .user(user)
                        .build());

                patients.add(patient);
            }

            int threadCount = 5;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                Patient patient = patients.get(index);
                User user = patient.getUser();

                AuthUser authUser = AuthUser.builder()
                        .id(user.getId())  // 저장 후 자동 생성된 User의 ID 사용
                        .email(user.getEmail())
                        .userRole(UserRole.ROLE_PATIENT)
                        .doctorId(doctor.getId())
                        .patientId(patient.getId())
                        .build();

                Runnable task = () -> {
                    try {
                        reservationPatientService.createReservation(
                                doctor.getId(),
                                "예약 사유",
                                ,
                                authUser);  // 각기 다른 authUser로 예약 요청
                    } catch (ApiException e) {
                        System.out.println("예약 중 예외 발생: " + e.getMessage());
                    }
                };

            threads[i] = new Thread(task);
            threads[i].start();
        }

        // 모든 스레드가 종료될 때까지 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // 최종 예약 개수 확인
        long reservationCount = reservationRepository.count();
        assertEquals(threadCount, reservationCount, "동시성 테스트에서 모든 예약이 성공적으로 생성되었습니다.");
    }


*/
/*    @Test
    void 예약_생성_카운터_증가_실패() {
        // given
        given(distributedCounterRepository.incrementCounter(VALID_COUNTER_ID)).willThrow(new IllegalArgumentException("카운트를 찾을 수 없습니다."));

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            distributedCounterService.createReservation(DOCTOR_ID, "예약 사유", LocalDate.now(), authUser);
        });

        assertEquals(ErrorStatus._COUNTER_INCREMENT_FAILED.getMessage(), exception.getMessage());
    }*//*


}
*/

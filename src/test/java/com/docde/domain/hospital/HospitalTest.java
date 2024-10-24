package com.docde.domain.hospital;

import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.response.HospitalPostResponseDto;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.hospital.service.HospitalService;
import com.docde.domain.user.entity.User;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class HospitalTest {
    @Mock
    private UserDetailsImpl userDetails;
    @InjectMocks
    private HospitalService hospitalService;
    @Mock
    private HospitalRepository hospitalRepository;


    @BeforeAll
    public static void setUp() {
//        User user = new User("");
//        ReflectionTestUtils.setField(userDetails,"user",user);
    }
    @Test
    public void 병원생성성공() {
        HospitalPostRequestDto requestDto = new HospitalPostRequestDto(
                "testHospitalName",
                "testHospitalAddress",
                "testHospitalContact",
                LocalTime.now(),
                LocalTime.now().minusHours(3),
                "testannouncement"
        );
        HospitalPostResponseDto responseDto = hospitalService.postHospital(requestDto, userDetails);

        assertEquals(responseDto.getHospitalName(),"testHospitalName");
    }
}

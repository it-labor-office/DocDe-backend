package com.docde.domain.hospital;

import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.hospital.service.HospitalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HospitalTest {
    @InjectMocks
    private HospitalService hospitalService;
    @Mock
    private HospitalRepository hospitalRepository;

    @Test
    public void 병원생성성공() {

    }
}

package com.docde.config;

import com.docde.domain.medicalRecord.encryption.AES256EncryptionService;
import com.docde.domain.medicalRecord.encryption.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Value("${AES_SECRET_KEY}")
    private String secretKey;

    @Value("${AES_INIT_VECTOR}")
    private String initVector;

    @Bean
    public EncryptionService encryptionService() {
        return new AES256EncryptionService(secretKey,initVector);
    }
}

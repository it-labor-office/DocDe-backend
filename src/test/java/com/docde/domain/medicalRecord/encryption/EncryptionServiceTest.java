package com.docde.domain.medicalRecord.encryption;

import com.docde.domain.medicalRecord.encryption.AES256EncryptionService;
import com.docde.domain.medicalRecord.encryption.EncryptionService;

public class EncryptionServiceTest {
    public static void main(String[] args) {

        try {
            String testKey = "01234567890123456789012345678901"; // 32-byte key
            String testIV = "0123456789012345"; // 16-byte IV
            // 서비스 생성
            EncryptionService encryptionService = new AES256EncryptionService(testKey,testIV);

            // 암호화 테스트
            String originalData = "진료 데이터";
            String encryptedData = encryptionService.encrypt(originalData);
            System.out.println("Encrypted Data: " + encryptedData);

            // 복호화 테스트
            String decryptedData = encryptionService.decrypt(encryptedData);
            System.out.println("Decrypted Data: " + decryptedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



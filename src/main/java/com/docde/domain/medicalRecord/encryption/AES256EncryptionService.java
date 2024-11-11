package com.docde.domain.medicalRecord.encryption;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AES256EncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec secretKey;
    private final IvParameterSpec ivParameterSpec;

    public AES256EncryptionService(
            @Value("${AES_SECRET_KEY}") String secretKey,
            @Value("${AES_INIT_VECTOR}") String initVector) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        this.ivParameterSpec = new IvParameterSpec(initVector.getBytes());
    }

    @Override
    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Override
    public String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }
}

package com.docde.domain.medicalRecord.encryption;

public interface EncryptionService {

    /**
     * Encrypt the given plaintext data.
     * @param plainText Data to be encrypted.
     * @return Encrypted data as a String.
     * @throws Exception If encryption fails.
     */
    String encrypt(String plainText) throws Exception;

    /**
     * Decrypt the given encrypted data.
     * @param encryptedText Data to be decrypted.
     * @return Decrypted plain text.
     * @throws Exception If decryption fails.
     */
    String decrypt(String encryptedText) throws Exception;
}



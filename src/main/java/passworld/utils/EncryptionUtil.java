package passworld.utils;

import passworld.data.exceptions.EncryptionException;
import passworld.data.session.UserSession;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static byte[] getSalt() {
        return UserSession.getInstance().getUserId().getBytes();
    }

    // === HASHING Y VERIFICACIÓN ===
    public static String hashMasterPassword(String masterPassword) throws EncryptionException {
        try {
            PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error hashing master password: " + e);
            throw new EncryptionException(LanguageUtil.getBundle().getString("errorHashingMasterPassword"), e);
        }
    }

    public static boolean verifyMasterPassword(String enteredPassword, String storedHashBase64) throws EncryptionException {
        try {
            String hashOfInput = hashMasterPassword(enteredPassword);
            return hashOfInput.equals(storedHashBase64);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error verifying master password: " + e);
            throw new EncryptionException(LanguageUtil.getBundle().getString("errorVerifyingMasterPassword"), e);
        }
    }

    public static SecretKeySpec deriveAESKey(String masterPassword) throws EncryptionException {
        try {
            PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] derivedKey = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(derivedKey, "AES");
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error deriving AES key: " + e);
            throw new EncryptionException(LanguageUtil.getBundle().getString("errorDerivingAESKey"), e);
        }
    }

    // === CIFRADO Y DESCIFRADO ===
    public static String encryptData(String plainText, SecretKeySpec masterKey) throws EncryptionException {
        if (plainText == null || masterKey == null) {
            LogUtils.LOGGER.severe("Error encrypting data: Data is null");
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenamos IV + cifrado y codificamos en Base64
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error encrypting data: " + e);
            throw new EncryptionException(LanguageUtil.getBundle().getString("errorEncryptingPassword"), e);
        }
    }


    public static String decryptData(String encryptedData, SecretKeySpec masterKey) throws EncryptionException {
        if (encryptedData == null || masterKey == null) {
            LogUtils.LOGGER.severe("Error decrypting data: Data is null");
            return null;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[combined.length - 16];

            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, ivSpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error decrypting data: " + e);
            throw new EncryptionException(LanguageUtil.getBundle().getString("errorDecryptingPassword"), e);
        }
    }
}

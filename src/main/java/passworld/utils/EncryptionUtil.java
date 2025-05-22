package passworld.utils;

import passworld.data.exceptions.EncryptionException;
import passworld.data.session.UserSession;
import passworld.service.LanguageManager;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
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
            throw new EncryptionException(LanguageManager.getBundle().getString("errorHashingMasterPassword"), e);
        }
    }

    public static boolean verifyMasterPassword(String enteredPassword, String storedHashBase64) throws EncryptionException {
        try {
            String hashOfInput = hashMasterPassword(enteredPassword);
            return hashOfInput.equals(storedHashBase64);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error verifying master password: " + e);
            throw new EncryptionException(LanguageManager.getBundle().getString("errorVerifyingMasterPassword"), e);
        }
    }

    public static SecretKeySpec deriveAESKey(String masterPassword) throws EncryptionException {
        try {
            PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] derivedKey = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(derivedKey, ALGORITHM);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error deriving AES key: " + e);
            throw new EncryptionException(LanguageManager.getBundle().getString("errorDerivingAESKey"), e);
        }
    }

    // === CIFRADO Y DESCIFRADO ===
    public static String encryptData(String plainPassword, SecretKeySpec masterKey) throws EncryptionException {
        if (plainPassword == null || masterKey == null) {
            LogUtils.LOGGER.severe("Error decrypting data: Data is null");
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey);
            byte[] encrypted = cipher.doFinal(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error encrypting data: " + e);
            throw new EncryptionException(LanguageManager.getBundle().getString("errorEncryptingPassword"), e);
        }
    }

    public static String decryptData(String encryptedPassword, SecretKeySpec masterKey) throws EncryptionException {
        if (encryptedPassword == null || masterKey == null) {
            LogUtils.LOGGER.severe("Error decrypting data: Data is null");
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, masterKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decrypted);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error decrypting data: " + e);
            throw new EncryptionException(LanguageManager.getBundle().getString("errorDecryptingPassword"), e);
        }
    }
}

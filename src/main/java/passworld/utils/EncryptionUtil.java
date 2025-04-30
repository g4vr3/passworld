package passworld.utils;

import passworld.data.session.UserSession;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    // Genera un salt único por usuario (basado en su ID, puedes hacer más robusto si lo deseas)
    private static byte[] getSalt() {
        return UserSession.getInstance().getUserId().getBytes(); // Alternativa: guardar salt real por usuario
    }

    // === HASHING Y VERIFICACIÓN ===
    public static String hashMasterPassword(String masterPassword) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyMasterPassword(String enteredPassword, String storedHashBase64) throws Exception {
        String hashOfInput = hashMasterPassword(enteredPassword);
        return hashOfInput.equals(storedHashBase64);
    }

    // === DERIVACIÓN DE CLAVE PARA CIFRADO ===
    private static SecretKeySpec deriveAESKey(String masterPassword) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        byte[] derivedKey = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(derivedKey, ALGORITHM);
    }

    // === CIFRADO Y DESCIFRADO DE CONTRASEÑAS ===
    public static String encryptPassword(String plainPassword, String masterPassword) throws Exception {
        SecretKeySpec key = deriveAESKey(masterPassword);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plainPassword.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptPassword(String encryptedPassword, String masterPassword) throws Exception {
        SecretKeySpec key = deriveAESKey(masterPassword);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decrypted);
    }

    // Extra: para comprobar una contraseña
    public static boolean verifyPassword(String encryptedPassword, String masterPassword, String plainPassword) throws Exception {
        String decryptedPassword = decryptPassword(encryptedPassword, masterPassword);
        return decryptedPassword.equals(plainPassword);
    }
}

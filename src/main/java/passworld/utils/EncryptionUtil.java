package passworld.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";

    // Deriva la clave AES a partir del authToken
    private static SecretKeySpec getKey(String authToken) {
        byte[] key = authToken.getBytes();
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    // Cifrar una contraseña usando el authToken
    public static String encryptPassword(String plainPassword, String authToken) throws Exception {
        SecretKeySpec key = getKey(authToken);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plainPassword.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Descifrar la contraseña usando el authToken
    public static String decryptPassword(String encryptedPassword, String authToken) throws Exception {
        SecretKeySpec key = getKey(authToken);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decrypted);
    }
}
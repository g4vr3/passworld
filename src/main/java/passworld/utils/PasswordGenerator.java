package passworld.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnñopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_+=<>?/{}[]";

    // Método para generar la contraseña
    public static String generatePassword(boolean upperAndLowerCase, boolean numbers, boolean specialChars, int length) {
        String characterPool = LOWERCASE;  // Contiene minúsculas por defecto
        List<Character> passwordChars = new ArrayList<>();

        SecureRandom random = new SecureRandom();

        // Agrega al menos un carácter de cada tipo requerido
        if (upperAndLowerCase) {
            passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
            characterPool += UPPERCASE;
            passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (numbers) {
            characterPool += NUMBERS;
            passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (specialChars) {
            characterPool += SPECIAL_CHARACTERS;
            passwordChars.add(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));
        }

        // Completar la contraseña hasta la longitud especificada
        for (int i = passwordChars.size(); i < length; i++) {
            passwordChars.add(characterPool.charAt(random.nextInt(characterPool.length())));
        }

        // Mezcla aleatoria de caracteres para evitar patrones predecibles
        Collections.shuffle(passwordChars);

        // Construir la contraseña final
        StringBuilder password = new StringBuilder();
        for (Character c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}

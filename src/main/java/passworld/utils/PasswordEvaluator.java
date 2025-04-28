package passworld.utils;

public class PasswordEvaluator {

    // Calcular la fortaleza de la contraseña
    public static int calculateStrength(String password) {
        int strength = 0;

        // Evaluar la longitud de la contraseña
        int length = password.length();
        if (length >= 8) strength++;
            else strength--;
        if (length >= 12) strength++;

        // Evaluar según los tipos de caracteres
        if (password.matches(".*[A-Z].*")) strength++;  // Al menos una mayúscula
        if (password.matches(".*\\d.*")) strength++;     // Al menos un número
        if (password.matches(".*[!@#$%^&*()\\-_=+<>?/{}\\[\\]].*")) strength++; // Al menos un carácter especial

        // Evitar puntuación superior a 4 e inferior a 0
        return Math.max(0, Math.min(strength, 4));
    }
}

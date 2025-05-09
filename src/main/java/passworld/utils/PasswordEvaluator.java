package passworld.utils;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import passworld.service.LanguageManager;

public class PasswordEvaluator {

    // Calcular la fortaleza de la contraseña
    public static int calculateStrength(String password) {
        int strength = 0;

        // Evaluar la longitud de la contraseña
        int length = password.length();
        if (length >= 8) strength++;
        if (length >= 12) strength++;

        // Evaluar según los tipos de caracteres
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()\\-_=+<>?/{}\\[\\]].*");

        int diversityCount = 0;
        if (hasUppercase) diversityCount++;
        if (hasLowercase) diversityCount++;
        if (hasDigit) diversityCount++;
        if (hasSpecialChar) diversityCount++;

        // Evaluar el nivel de diversidad
        if (diversityCount >= 3) strength++; // Al menos tres tipos de caracteres
        if (diversityCount == 4) strength++; // Todos los tipos de caracteres

        // Prevenir valores fuera del rango permitido
        return Math.min(strength, 4);
    }

    public static void updatePasswordStrengthInfo(int strength, Label passwordStrengthLabel, ProgressBar passwordStrengthProgressBar) {
        double progress = strength / 4.0; // Divide la fortaleza entre 4 para obtener un valor entre 0 y 1
        passwordStrengthProgressBar.setProgress(progress + 0.1); // Actualiza el progreso del ProgressBar con un mínimo de 0.1

        // Eliminar cualquier clase anterior al actualizar
        passwordStrengthProgressBar.getStyleClass().removeAll("red", "orange", "yellowgreen", "green");

        // En función de la fortaleza, cambia el color, el texto y los mensajes de ayuda
        switch (strength) {
            case 0:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_0"));
                passwordStrengthLabel.setTextFill(Color.TOMATO);
                passwordStrengthProgressBar.getStyleClass().add("red");
                break;
            case 1:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_1"));
                passwordStrengthLabel.setTextFill(Color.TOMATO);
                passwordStrengthProgressBar.getStyleClass().add("red");
                break;
            case 2:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_2"));
                passwordStrengthLabel.setTextFill(Color.ORANGE);
                passwordStrengthProgressBar.getStyleClass().add("orange");
                break;
            case 3:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_3"));
                passwordStrengthLabel.setTextFill(Color.YELLOWGREEN);
                passwordStrengthProgressBar.getStyleClass().add("yellowgreen");
                break;
            case 4:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_4"));
                passwordStrengthLabel.setTextFill(Color.GREEN);
                passwordStrengthProgressBar.getStyleClass().add("green");
                break;
        }

        passwordStrengthProgressBar.setVisible(true);
        passwordStrengthLabel.setVisible(true);
    }

    public static void updateStrengthLabelOnLanguageChange(String password, Label passwordStrengthLabel, ProgressBar passwordStrengthProgressBar) {
        if (password.isEmpty()) {
            return; // No actualiza si no hay una contraseña generada
        }

        int strength = calculateStrength(password); // Calcular la fortaleza de la contraseña
        updatePasswordStrengthInfo(strength, passwordStrengthLabel, passwordStrengthProgressBar);
    }
}
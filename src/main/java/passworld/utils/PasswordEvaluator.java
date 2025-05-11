package passworld.utils;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import passworld.service.LanguageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class PasswordEvaluator {

    private static final Trie commonWordsTrie = new Trie();

    // Cargar palabras comunes en el Trie
    public static void loadCommonWords() {
        try (InputStream inputStream = new GZIPInputStream(
                Objects.requireNonNull(PasswordEvaluator.class.getResourceAsStream("/passworld/dictionaries/common_words.txt.gz")));
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                commonWordsTrie.insert(line.trim().toLowerCase());
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Error loading common words file", e);
        }
    }

    // Calcular la fortaleza de la contraseña
    public static int calculateStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;

        // 1. Puntuación por longitud
        int length = password.length();
        if (length < 8) score -= 2;
        if (length >= 8) score += 2;
        if (length >= 12) score += 2;
        if (length >= 16) score += 2;

        // 2. Puntuación por diversidad de caracteres
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();

        int diversity = 0;
        if (hasUpper) diversity++;
        if (hasLower) diversity++;
        if (hasDigit) diversity++;
        if (hasSpecial) diversity++;

        score += diversity * 2; // Incrementar peso de la diversidad

        // 3. Penalizaciones

        // 3.1 Contiene palabras comunes
        if (containsCommonWords(password)) score -= 4;

        // 3.2 Secuencias comunes
        if (hasSequentialChars(password)) score -= 2;

        // 3.3 Repetición fuerte de caracteres o patrones
        if (hasRepetition(password)) score -= 2;

        // 4. Normalizar puntuación final a rango 0–4
        if (score <= 2) return 0;            // Muy débil
        else if (score <= 5) return 1;       // Débil
        else if (score <= 7) return 2;       // Media
        else if (score <= 10) return 3;       // Fuerte
        else return 4;                       // Muy fuerte
    }

    // Verificar si la contraseña contiene palabras comunes
    private static boolean containsCommonWords(String password) {
        String lower = password.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            for (int j = i + 1; j <= lower.length(); j++) {
                String substring = lower.substring(i, j);
                if (commonWordsTrie.contains(substring)) {
                    // Penalizar si la palabra común representa más del 50% de la longitud de la contraseña
                    if ((double) substring.length() / password.length() > 0.5) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Detecta secuencias simples como "abcd", "1234"
    private static boolean hasSequentialChars(String password) {
        String lower = password.toLowerCase();
        String sequence = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";

        for (int i = 0; i < sequence.length() - 3; i++) {
            String sub = sequence.substring(i, i + 4);
            if (lower.contains(sub)) return true;
        }

        for (int i = 0; i < digits.length() - 3; i++) {
            String sub = digits.substring(i, i + 4);
            if (password.contains(sub)) return true;
        }

        return false;
    }

    // Detecta repeticiones de caracteres o patrones simples
    private static boolean hasRepetition(String password) {
        // Repetición exacta de substrings (como "abcabc", "1212")
        for (int len = 1; len <= password.length() / 2; len++) {
            String part = password.substring(0, len);
            StringBuilder repeated = new StringBuilder();
            while (repeated.length() < password.length()) {
                repeated.append(part);
            }
            if (repeated.toString().equals(password)) return true;
        }

        // Repetición de un solo carácter muchas veces
        return password.matches("(.)\\1{3,}");
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
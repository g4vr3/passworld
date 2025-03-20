package passworld.service;

import passworld.data.PasswordDTO;
import passworld.utils.CompromisedPasswordChecker;
import passworld.utils.PasswordEvaluator;
import passworld.utils.UnsafeUrlChecker;

import java.util.Set;
import java.util.HashSet;

public class SecurityFilterManager {

    // Conjunto que mantiene las contraseñas únicas activas
    private final Set<String> uniquePasswords = new HashSet<>();

    // Analizar la seguridad de una contraseña, usuario y URL
    public void analyzePasswordSecurity(PasswordDTO passwordDTO) {
        boolean isWeak = isWeakPassword(passwordDTO.getPassword());
        boolean isDuplicate = isDuplicatePassword(passwordDTO.getPassword());
        boolean isCompromised = isCompromisedPassword(passwordDTO.getPassword());
        boolean isUrlUnsafe = isUrlUnsafe(passwordDTO.getUrl());

        passwordDTO.setWeak(isWeak);
        passwordDTO.setDuplicate(isDuplicate);
        passwordDTO.setCompromised(isCompromised);
        passwordDTO.setUrlUnsafe(isUrlUnsafe);
    }

    // Verificar si la contraseña es débil
    private boolean isWeakPassword(String password) {
        return PasswordEvaluator.calculateStrength(password) < 3;
    }

    // Verificar si la contraseña está duplicada
    private boolean isDuplicatePassword(String password) {
        // Si ya está en el conjunto de contraseñas únicas, se marca como duplicada
        return !uniquePasswords.add(password);  // Si no se puede agregar, es duplicada
    }


    // Verificar si la contraseña ha sido comprometida
    private boolean isCompromisedPassword(String password) {
        try {
            return CompromisedPasswordChecker.isCompromisedPassword(password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar una contraseña de la lista de contraseñas únicas
    public void removeUniquePassword(String password) {
        uniquePasswords.remove(password);
    }

    // Agregar una contraseña a la lista de contraseñas únicas
    public void addUniquePassword(String password) {
        uniquePasswords.add(password);
    }

    // Limpiar la lista de contraseñas únicas
    public void clearUniquePasswords() {
        uniquePasswords.clear();
    }

    // Verificar si la URL es insegura
    public static boolean isUrlUnsafe(String url) {
        return UnsafeUrlChecker.isUnsafe(url); // Implementa esta clase
    }

    public static boolean hasPasswordSecurityIssues(PasswordDTO passwordDTO) {
        return passwordDTO.isWeak() || passwordDTO.isDuplicate() || passwordDTO.isCompromised() || passwordDTO.isUrlUnsafe();
    }
}

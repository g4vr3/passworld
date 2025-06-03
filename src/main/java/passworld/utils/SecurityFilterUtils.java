package passworld.utils;

import passworld.data.PasswordDTO;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SecurityFilterUtils {

    /**
     * Analiza la seguridad de una lista de contraseñas.
     * Detecta duplicados dentro del mismo lote y evalúa cada aspecto de seguridad.
     *
     * @param passwordList lista de PasswordDTO a analizar
     */
    public static void analyzePasswordList(List<PasswordDTO> passwordList) {
        Set<String> seenPasswords = new HashSet<>();

        for (PasswordDTO passwordDTO : passwordList) {
            analyzePasswordSecurity(passwordDTO, seenPasswords);
        }
    }

    /**
     * Analiza la seguridad de una contraseña individual usando el conjunto de contraseñas ya vistas
     * para detectar duplicados en el lote.
     *
     * @param passwordDTO  objeto con datos de la contraseña
     * @param seenPasswords conjunto de contraseñas ya vistas
     */
    private static void analyzePasswordSecurity(PasswordDTO passwordDTO, Set<String> seenPasswords) {
        String password = passwordDTO.getPassword();
        String url = passwordDTO.getUrl();

        boolean isWeak = isWeakPassword(password);
        boolean isDuplicate = !seenPasswords.add(password);  // Si no se pudo añadir, es duplicada
        boolean isCompromised = isCompromisedPassword(password);
        boolean isUrlUnsafe = isUrlUnsafe(url);

        passwordDTO.setWeak(isWeak);
        passwordDTO.setDuplicate(isDuplicate);
        passwordDTO.setCompromised(isCompromised);
        passwordDTO.setUrlUnsafe(isUrlUnsafe);
    }

    /**
     * Evalúa si una contraseña es débil según el criterio de fuerza.
     *
     * @param password la contraseña a evaluar
     * @return true si es débil, false en caso contrario
     */
    private static boolean isWeakPassword(String password) {
        return PasswordEvaluator.calculateStrength(password) < 3;
    }

    /**
     * Consulta si una contraseña está comprometida.
     *
     * @param password la contraseña a verificar
     * @return true si está comprometida, false si no o si hay error
     */
    private static boolean isCompromisedPassword(String password) {
        try {
            return CompromisedPasswordChecker.isCompromisedPassword(password);
        } catch (Exception e) {
            System.err.println("Error checking compromised password: " + e.getMessage());
            LogUtils.LOGGER.warning("Error checking compromised password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si una URL es insegura.
     *
     * @param url la URL a evaluar
     * @return true si es insegura, false en caso contrario
     */
    public static boolean isUrlUnsafe(String url) {
        return UnsafeUrlChecker.isUnsafe(url);
    }

    /**
     * Comprueba si la contraseña presenta problemas de seguridad.
     *
     * @param passwordDTO objeto con la contraseña ya analizada
     * @return true si tiene algún problema (débil, duplicada, comprometida o URL insegura)
     */
    public static boolean hasPasswordSecurityIssues(PasswordDTO passwordDTO) {
        return passwordDTO.isWeak() ||
                passwordDTO.isDuplicate() ||
                passwordDTO.isCompromised() ||
                passwordDTO.isUrlUnsafe();
    }
}

package passworld.service;

public class VaultManager {

    // Instancia única de la clase (Singleton)
    private static VaultManager instance;

    // Variable que indica si el baúl está desbloqueado o no
    private boolean isUnlocked;

    // Constructor privado para evitar instanciación externa
    private VaultManager() {
        isUnlocked = false;  // Por defecto, esta bloqueado
    }

    // Método para obtener la instancia única
    public static VaultManager getInstance() {
        if (instance == null) {
            instance = new VaultManager();
        }
        return instance;
    }

    // Métodos para cambiar el estado de bloqueo
    public void unlock() {
        isUnlocked = true;
    }

    public void lock() {
        isUnlocked = false;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}

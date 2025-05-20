package passworld.data;

import passworld.service.LanguageManager;
import passworld.utils.LogUtils;

import java.io.*;
import java.util.Properties;

public class ConfigUtil {
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.passworld_config.properties";

    public static void saveSession(String userId, String refreshToken) throws IOException {
        Properties props = new Properties();
        props.setProperty("userId", userId);
        props.setProperty("refreshToken", refreshToken);
        try (FileOutputStream out = new FileOutputStream(CONFIG_PATH)) {
            props.store(out, "Passworld Session");
        } catch (IOException e) {
            LogUtils.LOGGER.severe("Error saving session: " + e);
            throw new IOException(LanguageManager.getBundle().getString("errorSavingSession"), e);
        }
    }

    public static void clearSession() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            if (!file.delete()) {
                LogUtils.LOGGER.severe("Error clearing session");
                throw new RuntimeException(LanguageManager.getBundle().getString("errorClearingSession"));
            }
        }
    }

    public static Properties loadSession() throws IOException {
        Properties props = new Properties();
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                LogUtils.LOGGER.severe("Error loading session: " + e);
                throw new IOException(LanguageManager.getBundle().getString("errorLoadingSession"), e);
            }
        }
        return props;
    }
}

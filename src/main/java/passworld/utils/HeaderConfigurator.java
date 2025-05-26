package passworld.utils;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import passworld.PassworldApplication;

import java.util.Objects;

public class HeaderConfigurator {

    public static void configureHeader(ImageView logoImageView, ImageView languageImageView, Button toggleThemeButton, Button helpButton) {
        // Establecer imagen de logo
        Image logoImage = new Image(Objects.requireNonNull(HeaderConfigurator.class.getResource("/passworld/images/passworld_logo.png")).toExternalForm());
        logoImageView.setImage(logoImage);
        ThemeManager.applyThemeToImage(logoImageView);

        // Acción del logo para mostrar el diálogo "Acerca de"
        logoImageView.setOnMouseClicked(event -> {
            DialogUtil.showAboutDialog();
        });

        // Icono de ayuda
        Image helpIcon = new Image(Objects.requireNonNull(HeaderConfigurator.class.getResource("/passworld/images/help_icon.png")).toExternalForm());
        ImageView helpIconImageView = new ImageView(helpIcon);
        ThemeManager.applyThemeToImage(helpIconImageView);
        helpIconImageView.getStyleClass().add("icon");
        // Configuración de tamaño redundante para evitar problemas de visualización
        helpIconImageView.setFitWidth(15);
        helpIconImageView.setFitHeight(15);
        helpButton.setGraphic(helpIconImageView);

        // Acción del botón de ayuda para abrir la web
        helpButton.setOnAction(_ -> {
            String url = "https://g4vr3.github.io/passworld-web/help.html";
            try {
                PassworldApplication.getHostServicesInstance().showDocument(url);
            } catch (Exception e) {
                System.err.println("Error al abrir la URL de ayuda: " + url);
                LogUtils.LOGGER.warning("Error opening help URL: " + url);
                e.printStackTrace();
            }
        });

        // Icono de idioma
        Image languageIcon = new Image(Objects.requireNonNull(HeaderConfigurator.class.getResource("/passworld/images/language_icon.png")).toExternalForm());
        languageImageView.setImage(languageIcon);
        ThemeManager.applyThemeToImage(languageImageView);
        languageImageView.getStyleClass().add("icon");

        // Actualizar el icono del botón de alternar tema
        String themeIconPath = ThemeManager.isDarkMode()
                ? "/passworld/images/light_mode_icon.png"
                : "/passworld/images/dark_mode_icon.png";
        Image themeIcon = new Image(Objects.requireNonNull(HeaderConfigurator.class.getResource(themeIconPath)).toExternalForm());
        ImageView themeImageView = new ImageView(themeIcon);
        // Configuración de tamaño redundante para evitar problemas de visualización
        themeImageView.setFitWidth(15);
        themeImageView.setFitHeight(15);
        themeImageView.getStyleClass().add("icon");
        toggleThemeButton.setGraphic(themeImageView);
    }

    public static void configureLogoutButton(Button logoutButton, ImageView logoutImageView, Runnable handleLogout) {
        // Establecer imagen de logout
        Image logoutImage = new Image(Objects.requireNonNull(HeaderConfigurator.class.getResource("/passworld/images/logout_icon.png")).toExternalForm());
        logoutImageView.setImage(logoutImage);
        ThemeManager.applyThemeToImage(logoutImageView);
        logoutImageView.getStyleClass().add("icon");
        // Configuración de tamaño redundante para evitar problemas de visualización
        logoutImageView.setFitWidth(15);
        logoutImageView.setFitHeight(15);

        // Acción del botón de logout
        logoutButton.setOnAction(_ -> {
            // Llamar al handleLogout pasado como parámetro
            handleLogout.run();
        });
    }
}
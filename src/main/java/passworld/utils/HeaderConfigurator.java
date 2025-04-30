package passworld.utils;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import passworld.service.LanguageManager;

import java.util.Objects;

public class HeaderConfigurator {

    public static void configureHeader(ImageView logoImageView, ImageView languageImageView, Button toggleThemeButton) {
        // Establecer imagen de logo
        Image logoImage = new Image(Objects.requireNonNull(HeaderConfigurator.class.getResource("/passworld/images/passworld_logo.png")).toExternalForm());
        logoImageView.setImage(logoImage);
        ThemeManager.applyThemeToImage(logoImageView);

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
        themeImageView.getStyleClass().add("icon");
        toggleThemeButton.setGraphic(themeImageView);
        toggleThemeButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_toggleThemeButton")));
    }
}
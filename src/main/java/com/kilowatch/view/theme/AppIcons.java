package com.kilowatch.view.theme;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;

public enum AppIcons {
    BANKNOTE,
    BANKNOTE_CHECK,
    CIRCLE_CHECK_BIG,
    FILE,
    FILE_TEXT,
    GAUGE,
    HAND_COINS,
    LAYOUT_DASHBOARD,
    LOG_IN,
    LOG_OUT,
    RECEIPT_TEXT,
    USER,
    USER_PEN,
    USER_PLUS,
    USERS,
    WALLET;

    /**
     * Crée une icône avec une taille et une couleur personnalisables
     */
    public FlatSVGIcon get(int size, Color color) {
        // 1. Conversion dynamique du nom (ex: LOG_OUT -> log-out)
        String fileName = this.name().toLowerCase().replace("_", "-");
        String fullPath = "icons/" + fileName + ".svg";

        // 2. Création de l'icône
        FlatSVGIcon icon = new FlatSVGIcon(fullPath, size, size);

        // 3. Application du filtre couleur
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));

        return icon;
    }

    // Version par défaut
    public FlatSVGIcon get() {
        return get(18, AppColors.TEXT_SECONDARY);
    }
}
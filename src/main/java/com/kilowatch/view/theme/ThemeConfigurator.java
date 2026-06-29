package com.kilowatch.view.theme;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import java.awt.Font;

public class ThemeConfigurator {

    public static void setupGlobalTheme() {
        // --- 1. Fonts ---
        Font defaultFont = new Font("Inter", Font.PLAIN, 13);
        UIManager.put("defaultFont", defaultFont);

        // --- 2. Global Backgrounds & Text ---
        UIManager.put("Panel.background", AppColors.BG_DEEP);
        UIManager.put("Label.foreground", AppColors.TEXT_PRIMARY);

        // --- 3. Inputs & TextFields ---
        UIManager.put("TextField.background", AppColors.BG_SURFACE_2);
        UIManager.put("TextField.foreground", AppColors.TEXT_PRIMARY);
        UIManager.put("TextField.focusedBorderColor", AppColors.ACCENT_AMBER);
        UIManager.put("Component.focusWidth", 2);

        // --- 4. Tables ---
        UIManager.put("Table.background", AppColors.BG_SURFACE);
        UIManager.put("Table.foreground", AppColors.TEXT_PRIMARY);
        UIManager.put("Table.gridColor", AppColors.BORDER_SOFT);
        UIManager.put("Table.selectionBackground", AppColors.BG_ELEVATED);
        UIManager.put("TableHeader.background", AppColors.BG_SURFACE);
        UIManager.put("TableHeader.foreground", AppColors.TEXT_DIM);

        // --- 5. Global Radius ---
        UIManager.put("Component.arc", 6);
        UIManager.put("Button.arc", 10);

        // --- 6. Topbar & Menus (JMenuBar, JMenu, JMenuItem) ---

        // Fond de la barre principale (doit matcher avec la Sidebar)
        UIManager.put("MenuBar.background", AppColors.BG_SIDEBAR);
        UIManager.put("MenuBar.border", null); // On laisse le composant gérer sa propre bordure basse

        // Le fond des menus déroulants (Popups)
        UIManager.put("PopupMenu.background", AppColors.BG_ELEVATED);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(AppColors.BORDER_DEFAULT));

        // Couleurs du texte au repos
        UIManager.put("Menu.foreground", AppColors.TEXT_SECONDARY);
        UIManager.put("MenuItem.foreground", AppColors.TEXT_SECONDARY);

        // --- 7. Gestion Globale des Hovers (Survols / Sélections) ---

        // Hover sur les menus de la barre (Fichier, Edition...)
        UIManager.put("MenuBar.hoverBackground", AppColors.BG_SURFACE);
        UIManager.put("Menu.selectionBackground", AppColors.BG_SURFACE);
        UIManager.put("Menu.selectionForeground", AppColors.TEXT_PRIMARY);

        // Hover sur les sous-menus déroulants (Nouveau, Quitter...)
        UIManager.put("MenuItem.selectionBackground", AppColors.BG_SURFACE_2);
        UIManager.put("MenuItem.selectionForeground", AppColors.TEXT_PRIMARY);

        // Standardiser le hover de tous les boutons classiques par défaut
        UIManager.put("Button.hoverBackground", AppColors.BG_SURFACE_2);
        UIManager.put("Button.background", AppColors.BG_SURFACE);
        UIManager.put("Button.foreground", AppColors.TEXT_PRIMARY);
    }
}
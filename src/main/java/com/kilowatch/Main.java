package com.kilowatch;

import com.formdev.flatlaf.FlatDarkLaf;
import com.kilowatch.view.theme.ThemeConfigurator;
import com.kilowatch.view.ui.MainLayout;
import javax.swing.*;
import java.awt.Dimension;

public class Main {
    public static void main(String[] args) {

        // 1. ACTIVER LES DÉCORATIONS DE FENÊTRE FLATLAF (La clé de la fusion)
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // (Optionnel) S'assurer que l'intégration du menu est bien forcée
        System.setProperty("flatlaf.menuBarEmbedded", "true");

        // 2. Initialiser le thème de base FlatLaf
        FlatDarkLaf.setup();

        // 3. Appliquer ta configuration globale (couleurs, polices, hovers...)
        ThemeConfigurator.setupGlobalTheme();

        // 4. Lancer l'interface graphique avec barrières de sécurité de taille
        SwingUtilities.invokeLater(() -> {
            MainLayout app = new MainLayout();

            app.setMinimumSize(new Dimension(1150, 720)); // Empêche de réduire en dessous de cette taille
            app.setSize(1280, 800); // Taille par défaut idéale au démarrage
            app.setLocationRelativeTo(null); // Centre l'application à l'écran

            app.setVisible(true);
        });
    }
}
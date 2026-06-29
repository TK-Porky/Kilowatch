package com.kilowatch;

import com.formdev.flatlaf.FlatDarkLaf;
import com.kilowatch.view.theme.ThemeConfigurator;
import com.kilowatch.view.ui.MainLayout;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // 1. ACTIVER LES DÉCORATIONS DE FENÊTRE FLATLAF (La clé de la fusion)
        // Ces deux lignes disent à Swing de ne pas utiliser la barre de titre Ubuntu,
        // mais celle générée par FlatLaf.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // (Optionnel) S'assurer que l'intégration du menu est bien forcée
        System.setProperty("flatlaf.menuBarEmbedded", "true");

        // 2. Initialiser le thème de base FlatLaf
        FlatDarkLaf.setup();

        // 3. Appliquer ta configuration globale (couleurs, polices, hovers...)
        ThemeConfigurator.setupGlobalTheme();

        // 4. Lancer l'interface graphique
        SwingUtilities.invokeLater(() -> {
            MainLayout app = new MainLayout();
            app.setVisible(true);
        });
    }
}
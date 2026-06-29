package com.kilowatch.view.ui;

import javax.swing.*;
import java.awt.*;
import com.kilowatch.view.theme.AppColors;

public class Topbar extends JMenuBar {

    private final JLabel viewTitleLabel;

    public Topbar() {
        // Configuration de la barre de menu
        setBackground(AppColors.BG_SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER_SOFT));
        setPreferredSize(new Dimension(0, 40)); // Une hauteur de 40px est standard pour un menu

        // --- 1. CRÉATION DES MENUS (JMenu) ---
        JMenu menuFichier = new JMenu("Fichier");
        JMenu menuEdition = new JMenu("Édition");
        JMenu menuOutils = new JMenu("Outils");
        JMenu menuAide = new JMenu("Aide");

        // Style des menus principaux
        menuFichier.setForeground(AppColors.TEXT_SECONDARY);
        menuEdition.setForeground(AppColors.TEXT_SECONDARY);
        menuOutils.setForeground(AppColors.TEXT_SECONDARY);
        menuAide.setForeground(AppColors.TEXT_SECONDARY);

        // --- 2. CRÉATION DES SOUS-MENUS (JMenuItem) ---
        // Exemple pour le menu "Fichier"
        JMenuItem itemNouveau = new JMenuItem("Nouveau relevé");
        JMenuItem itemExport = new JMenuItem("Exporter le lot CSV");
        JMenuItem itemQuitter = new JMenuItem("Quitter");

        // On peut ajouter des raccourcis clavier natifs très facilement
        itemNouveau.setAccelerator(KeyStroke.getKeyStroke("control N"));
        itemQuitter.setAccelerator(KeyStroke.getKeyStroke("control Q"));

        menuFichier.add(itemNouveau);
        menuFichier.addSeparator(); // Ligne de séparation esthétique
        menuFichier.add(itemExport);
        menuFichier.addSeparator();
        menuFichier.add(itemQuitter);

        // --- 3. AJOUT DES MENUS À LA BARRE ---
        add(menuFichier);
        add(menuEdition);
        add(menuOutils);
        add(menuAide);

        // --- 4. SÉPARATEUR INVISIBLE ---
        // Ce "glue" va pousser tout ce qu'on ajoute après lui complètement à droite !
        add(Box.createHorizontalGlue());

        // --- 5. ZONE DROITE : Titre dynamique et Badge ---
        viewTitleLabel = new JLabel("Tableau de bord  |  ");
        viewTitleLabel.setFont(new Font("Inter", Font.BOLD, 13));
        viewTitleLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel onlineBadge = new JLabel("● Live  ");
        onlineBadge.setFont(new Font("Inter", Font.BOLD, 11));
        onlineBadge.setForeground(AppColors.STATUS_GREEN);

        add(viewTitleLabel);
        add(onlineBadge);
    }

    /**
     * Met à jour le titre affiché à droite de la barre de menu
     */
    public void updateTitle(String newTitle) {
        viewTitleLabel.setText(newTitle + "  |  ");
    }
}
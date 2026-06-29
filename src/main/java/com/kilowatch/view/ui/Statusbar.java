package com.kilowatch.view.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.kilowatch.view.theme.AppColors;

public class Statusbar extends JPanel {

    public Statusbar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(AppColors.BG_SIDEBAR);
        setPreferredSize(new Dimension(0, 30));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppColors.BORDER_SOFT),
                new EmptyBorder(0, 12, 0, 12))); // Marges latérales globales réduites pour laisser place aux labels

        // --- Création des éléments ---
        // Ajout d'une marge horizontale interne de 10px (5px de chaque côté)
        // pour empêcher le contact entre les textes
        JLabel lblAgent = createLabel("Agent : J. Dupont");
        JLabel lblPeriod = createLabel("Période active : Mai 2026");

        JLabel lblStatus = createLabel("● CSV Connecté");
        lblStatus.setForeground(AppColors.STATUS_GREEN);

        JLabel lblVersion = createLabel("v1.0.0 - kilowatch");
        lblVersion.setFont(new Font("Inter", Font.PLAIN, 11)); // Remplacé "IBM Plex Mono" si non installé

        // --- Assemblage ---
        add(lblAgent);
        add(Box.createHorizontalGlue());
        add(lblPeriod);
        add(Box.createHorizontalGlue());
        add(lblStatus);
        add(Box.createHorizontalGlue());
        add(lblVersion);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.PLAIN, 12));
        label.setForeground(AppColors.TEXT_SECONDARY);

        // C'est ICI que l'on protège le label :
        // On ajoute une marge fixe de 10px à gauche et à droite de chaque label
        label.setBorder(new EmptyBorder(0, 10, 0, 10));

        return label;
    }
}
package com.kilowatch.view.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.data.ViewDto.StatusbarInfo;

public class Statusbar extends JPanel {

    private final JLabel lblAgent;
    private final JLabel lblPeriod;
    private final JLabel lblStatus;
    private final JLabel lblVersion;

    public Statusbar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(AppColors.BG_SIDEBAR);
        setPreferredSize(new Dimension(0, 30));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppColors.BORDER_SOFT),
                new EmptyBorder(0, 12, 0, 12)));

        // --- Initialisation des composants avec des valeurs par défaut ---
        lblAgent = createLabel("Agent : —");
        lblPeriod = createLabel("Période active : —");

        lblStatus = createLabel("● Déconnecté");
        lblStatus.setForeground(AppColors.STATUS_GREEN); // Garde la couleur définie par votre charte

        lblVersion = createLabel("v1.0.0");
        lblVersion.setFont(new Font("Inter", Font.PLAIN, 11));

        // --- Assemblage ---
        add(lblAgent);
        add(Box.createHorizontalGlue());
        add(lblPeriod);
        add(Box.createHorizontalGlue());
        add(lblStatus);
        add(Box.createHorizontalGlue());
        add(lblVersion);
    }

    /**
     * Permet de mettre à jour dynamiquement les données textuelles de la barre
     * d'état.
     */
    public void updateStatusbar(StatusbarInfo info) {
        lblAgent.setText("Agent : " + info.nomAgent());
        lblPeriod.setText("Période active : " + info.periodeActive());
        lblStatus.setText(info.statutConnexion());
        lblVersion.setText(info.version());
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.PLAIN, 12));
        label.setForeground(AppColors.TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(0, 10, 0, 10));
        return label;
    }
}
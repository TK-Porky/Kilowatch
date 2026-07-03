package com.kilowatch.view.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.data.ViewDto.StatusbarInfo;

import java.util.concurrent.atomic.AtomicReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Statusbar extends JPanel {

    private final JLabel lblAgent;
    private final JLabel lblPeriod;
    private final JLabel lblStatus;
    private final JLabel lblVersion;
    private final JProgressBar progressBar;
    private final JButton cancelBtn;
    private final AtomicReference<Runnable> cancelAction = new AtomicReference<>();

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

        // Progress bar (hidden by default)
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(200, 12));
        progressBar.setVisible(false);
        progressBar.setStringPainted(false);
        add(progressBar);

        cancelBtn = new JButton("Annuler");
        cancelBtn.setVisible(false);
        cancelBtn.setFont(new Font("Inter", Font.PLAIN, 11));
        cancelBtn.setForeground(AppColors.TEXT_SECONDARY);
        cancelBtn.setOpaque(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Runnable a = cancelAction.getAndSet(null);
                if (a != null) a.run();
            }
        });

        add(Box.createHorizontalGlue());
        add(lblStatus);
        add(Box.createHorizontalGlue());
        add(cancelBtn);
        add(Box.createHorizontalStrut(8));
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

    // Progress API
    public void startTask(String label, boolean cancellable, Runnable onCancel) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setVisible(true);
            cancelBtn.setVisible(cancellable);
            if (cancellable) cancelAction.set(onCancel);
            lblStatus.setText(label == null ? "" : label);
            revalidate();
            repaint();
        });
    }

    public void updateProgress(int percent) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(Math.max(0, Math.min(100, percent)));
        });
    }

    public void endTask() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            cancelBtn.setVisible(false);
            cancelAction.set(null);
            revalidate();
            repaint();
            // restore status text
            lblStatus.setText("● Connecté");
        });
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.PLAIN, 12));
        label.setForeground(AppColors.TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(0, 10, 0, 10));
        return label;
    }
}
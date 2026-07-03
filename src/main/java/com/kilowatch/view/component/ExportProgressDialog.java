package com.kilowatch.view.component;

import javax.swing.*;
import java.awt.*;

/**
 * Composant graphique "Dumb" (purement présentational).
 * Il s'occupe uniquement du rendu visuel de la progression et est pilotable de
 * l'extérieur.
 */
public class ExportProgressDialog extends JDialog {

    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public ExportProgressDialog(Window owner, String titre) {
        super(owner, titre, ModalityType.APPLICATION_MODAL);

        // Configuration de la fenêtre popup
        setSize(380, 140);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Empêche la fermeture manuelle forcée
        setResizable(false);
        setLayout(new BorderLayout());

        // Composants de texte et d'état
        statusLabel = new JLabel("Initialisation du flux...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Barre de progression
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true); // Affiche le pourcentage (ex: 50%)
        progressBar.setPreferredSize(new Dimension(320, 24));

        // Conteneur avec marges et espacements (Design épuré)
        JPanel panelContainer = new JPanel(new BorderLayout(10, 15));
        panelContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelContainer.add(statusLabel, BorderLayout.NORTH);
        panelContainer.add(progressBar, BorderLayout.CENTER);

        add(panelContainer);
    }

    /**
     * Met à jour la barre de progression et le libellé de manière asynchrone et
     * sécurisée (EDT).
     */
    public void updateProgress(int pourcentage, String statut) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(pourcentage);
            statusLabel.setText(statut);
        });
    }

    /**
     * Ferme proprement et de manière sécurisée la boîte de dialogue depuis
     * n'importe quel thread.
     */
    public void safeDispose() {
        SwingUtilities.invokeLater(this::dispose);
    }
}
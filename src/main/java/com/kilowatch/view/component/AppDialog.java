package com.kilowatch.view.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Classe de base pour toutes les fenêtres modales de l'application.
 * Gère automatiquement la fermeture via 'Échap' et la soumission via 'Entrée'.
 */
public abstract class AppDialog extends JDialog {

    public AppDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);

        // Configuration de base commune à toutes vos fenêtres
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setUndecorated(true);

        // --- 1. FERMETURE AUTOMATIQUE AVEC ÉCHAP ---
        setupEscapeKey();
    }

    private void setupEscapeKey() {
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(
                e -> closeDialog(),
                escapeKeyStroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Méthode appelée lors de l'appui sur Échap.
     * Peut être redéfinie par les enfants si besoin d'une confirmation avant de
     * quitter.
     */
    protected void closeDialog() {
        dispose();
    }

    /**
     * --- 2. DÉFINIR LE BOUTON DE SOUMISSION (Touche Entrée) ---
     * Les classes filles doivent appeler cette méthode à la fin de leur
     * constructeur.
     */
    protected void setSubmitButton(JButton button) {
        getRootPane().setDefaultButton(button);
    }
}
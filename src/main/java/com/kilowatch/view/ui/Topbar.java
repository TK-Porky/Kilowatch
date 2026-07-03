package com.kilowatch.view.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import com.kilowatch.view.theme.AppColors;

public class Topbar extends JMenuBar {

    private final JLabel viewTitleLabel;

    // --- CALLBACKS POUR LE MAINLAYOUT ---
    private Runnable onNewAbonneListener;
    private Runnable onGoToRelevesListener;
    private Runnable onExportCsvListener;
    private Runnable onGoToImpayesListener;
    private Runnable onGoToGrosConsommateursListener;
    private Runnable onLogoutListener;

    public Topbar() {
        setBackground(AppColors.BG_SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER_SOFT));
        setPreferredSize(new Dimension(0, 40));

        // ==========================================
        // 1. MENU FICHIER (Actions principales)
        // ==========================================
        JMenu menuFichier = new JMenu("Fichier");
        menuFichier.setForeground(AppColors.TEXT_SECONDARY);

        JMenuItem itemExport = new JMenuItem("Exporter impayés (CSV)");
        itemExport.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemExport.addActionListener(e -> {
            if (onExportCsvListener != null)
                onExportCsvListener.run();
        });

        // --- NOUVEAU : Option Déconnexion ---
        JMenuItem itemDeconnexion = new JMenuItem("Déconnexion");
        itemDeconnexion.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemDeconnexion.addActionListener(e -> {
            if (onLogoutListener != null)
                onLogoutListener.run();
        });

        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemQuitter.addActionListener(e -> System.exit(0));

        menuFichier.add(itemExport);
        menuFichier.addSeparator();
        menuFichier.add(itemDeconnexion); // Ajouté juste avant "Quitter"
        menuFichier.add(itemQuitter);

        // ==========================================
        // 2. MENU NAVIGATION (Déplacements rapides)
        // ==========================================
        JMenu menuNav = new JMenu("Navigation");
        menuNav.setForeground(AppColors.TEXT_SECONDARY);

        JMenuItem itemNouveau = new JMenuItem("Nouvel Abonné");
        itemNouveau.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemNouveau.addActionListener(e -> {
            if (onNewAbonneListener != null)
                onNewAbonneListener.run();
        });

        JMenuItem itemReleves = new JMenuItem("Saisie des relevés");
        itemReleves.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemReleves.addActionListener(e -> {
            if (onGoToRelevesListener != null)
                onGoToRelevesListener.run();
        });

        JMenuItem itemImpayee = new JMenuItem("Liste des impayés");
        itemImpayee.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemImpayee.addActionListener(e -> {
            if (onGoToImpayesListener != null)
                onGoToImpayesListener.run();
        });

        JMenuItem itemGrosCons = new JMenuItem("Gros Consommateurs");
        itemGrosCons.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemGrosCons.addActionListener(e -> {
            if (onGoToGrosConsommateursListener != null)
                onGoToGrosConsommateursListener.run();
        });

        menuNav.add(itemNouveau);
        menuNav.add(itemReleves);
        menuNav.addSeparator();
        menuNav.add(itemImpayee);
        menuNav.add(itemGrosCons);

        // ==========================================
        // 3. MENU AIDE (Infos statiques)
        // ==========================================
        JMenu menuAide = new JMenu("Aide");
        menuAide.setForeground(AppColors.TEXT_SECONDARY);

        JMenuItem itemRaccourcis = new JMenuItem("Raccourcis clavier");
        itemRaccourcis.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemRaccourcis.addActionListener(e -> showRaccourcisDialog());

        JMenuItem itemApropos = new JMenuItem("À propos de Kilowatch");
        itemApropos.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemApropos.addActionListener(e -> showAproposDialog());

        menuAide.add(itemRaccourcis);
        menuAide.addSeparator();
        menuAide.add(itemApropos);

        // --- AJOUT DES MENUS À LA BARRE ---
        add(menuFichier);
        add(menuNav);
        add(menuAide);
        add(Box.createHorizontalGlue()); // Pousse le reste à droite

        // --- ZONE DROITE : Titre et Badge ---
        viewTitleLabel = new JLabel("Tableau de bord  |  ");
        viewTitleLabel.setFont(new Font("Inter", Font.BOLD, 13));
        viewTitleLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel onlineBadge = new JLabel("● Live  ");
        onlineBadge.setFont(new Font("Inter", Font.BOLD, 11));
        onlineBadge.setForeground(AppColors.STATUS_GREEN);

        add(viewTitleLabel);
        add(onlineBadge);
    }

    // ==========================================
    // MÉTHODES DE MISE À JOUR ET SETTERS
    // ==========================================

    public void updateTitle(String newTitle) {
        viewTitleLabel.setText(newTitle + "  |  ");
    }

    public void setOnNewAbonneListener(Runnable onNewAbonneListener) {
        this.onNewAbonneListener = onNewAbonneListener;
    }

    public void setOnGoToRelevesListener(Runnable onGoToRelevesListener) {
        this.onGoToRelevesListener = onGoToRelevesListener;
    }

    public void setOnExportCsvListener(Runnable onExportCsvListener) {
        this.onExportCsvListener = onExportCsvListener;
    }

    public void setOnGoToImpayesListener(Runnable onGoToImpayesListener) {
        this.onGoToImpayesListener = onGoToImpayesListener;
    }

    public void setOnGoToGrosConsommateursListener(Runnable onGoToGrosConsommateursListener) {
        this.onGoToGrosConsommateursListener = onGoToGrosConsommateursListener;
    }

    public void setOnLogoutListener(Runnable onLogoutListener) {
        this.onLogoutListener = onLogoutListener;
    }

    // ==========================================
    // BOÎTES DE DIALOGUE (JOptionPane)
    // ==========================================

    private void showRaccourcisDialog() {
        String msg = """
                Liste des raccourcis clavier globaux :

                • Ctrl + N : Nouvel abonné
                • Ctrl + R : Aller à la saisie des relevés
                • Ctrl + L : Liste des impayés
                • Ctrl + G : Gros consommateurs
                • Ctrl + E : Exporter les impayés (CSV)
                • Ctrl + H : Afficher les raccourcis
                • Ctrl + I : À propos
                • Ctrl + D : Déconnexion de la session
                • Ctrl + Q : Quitter l'application

                Raccourcis contextuels :
                • Touche [Entrée] : Valider la saisie dans les formulaires
                • Touche [Échap] : Fermer les fenêtres pop-up (modales)
                """;
        JOptionPane.showMessageDialog(this, msg, "Raccourcis Clavier", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAproposDialog() {
        String msg = """
                Kilowatch v2.0
                Plateforme de Suivi des Consommations d'Énergie Post-payées.

                Projet académique développé par l'équipe G2.
                Conçu pour la numérisation des relevés d'index et la facturation (ENEO).
                """;
        JOptionPane.showMessageDialog(this, msg, "À propos de Kilowatch", JOptionPane.INFORMATION_MESSAGE);
    }
}
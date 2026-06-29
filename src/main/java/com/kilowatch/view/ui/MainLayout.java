package com.kilowatch.view.ui;

import javax.swing.*;
import java.awt.*;
import com.kilowatch.view.theme.AppColors;

public class MainLayout extends JFrame {

    private CardLayout cardLayout;
    private JPanel workzone;
    private Topbar topbar;
    private Sidebar sidebar;
    private Statusbar statusbar;

    public MainLayout() {
        setTitle("Kilowatch — Plateforme de Suivi de Consommation");
        setSize(1280, 850); // Résolution optimale proche de la maquette web
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrer la fenêtre sur l'écran

        // Le layout principal qui orchestre les blocs
        setLayout(new BorderLayout());

        // 1. Initialiser le centre (La Workzone) d'abord
        initWorkzone();

        // 2. Initialiser la Barre Supérieure (Topbar)
        topbar = new Topbar();

        // 3. Initialiser la Barre d'État (Statusbar)
        statusbar = new Statusbar();

        // 4. Initialiser la Sidebar avec son écouteur de navigation
        sidebar = new Sidebar(viewId -> {
            // Action lors du clic sur un bouton de la sidebar :
            cardLayout.show(workzone, viewId); // On change la page

            // On change dynamiquement le titre du Topbar en fonction de l'ID
            switch (viewId) {
                case "VIEW_DASHBOARD" -> topbar.updateTitle("Tableau de bord");
                case "VIEW_ABONNES" -> topbar.updateTitle("Abonnés & Contrats");
                case "VIEW_RELEVES" -> topbar.updateTitle("Relevés");
                case "VIEW_CAISSE" -> topbar.updateTitle("Caisse & Facturation");
            }
        });

        // 5. Imbrication des briques dans le BorderLayout global de la JFrame
        add(sidebar, BorderLayout.WEST);
        add(statusbar, BorderLayout.SOUTH);

        // Swing possède une méthode dédiée pour accrocher la barre de menu tout en haut
        // de la fenêtre
        setJMenuBar(topbar);

        // On ajoute directement la Workzone au centre, plus besoin de sous-conteneur
        add(workzone, BorderLayout.CENTER);
    }

    private void initWorkzone() {
        cardLayout = new CardLayout();
        workzone = new JPanel(cardLayout);

        // Vues de contenus temporaires (À remplacer plus tard par vos panels métiers)
        JPanel viewDashboard = createTempView("Contenu du Tableau de Bord");
        JPanel viewAbonnes = createTempView("Contenu de la Gestion des Abonnés");
        JPanel viewReleves = createTempView("Contenu de Relevés d'indexes");
        JPanel viewCaisse = createTempView("Contenu de Caisse & Factures");

        workzone.add(viewDashboard, "VIEW_DASHBOARD");
        workzone.add(viewAbonnes, "VIEW_ABONNES");
        workzone.add(viewReleves, "VIEW_RELEVES");
        workzone.add(viewCaisse, "VIEW_CAISSE");
    }

    private JPanel createTempView(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppColors.BG_DEEP); // Le fond sombre profond de l'application

        JLabel label = new JLabel(title);
        label.setFont(new Font("Inter", Font.PLAIN, 16));
        label.setForeground(AppColors.TEXT_SECONDARY);

        panel.add(label);
        return panel;
    }
}
package com.kilowatch.view.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.kilowatch.view.theme.AppColors;

public class MainLayout extends JFrame {

    private CardLayout cardLayout;
    private JPanel workzone;
    private Topbar topbar;
    private Sidebar sidebar;
    private Statusbar statusbar;

    // Variables d'état pour la simulation du Mock Relevés
    private final String mockCompteur = "CMP-20007";
    private final String mockNom = "Etoa Ekani C.";
    private final String mockCategorie = "Résidentiel";
    private final int mockAncienIndex = 3685;

    private DefaultTableModel sessionRelevesModel;
    private int totalSaisiesSession = 0;

    public MainLayout() {
        setTitle("Kilowatch — Plateforme de Suivi de Consommation");
        setSize(1280, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // 1. Initialiser le centre (La Workzone) d'abord
        initWorkzone();

        // 2. Initialiser la Barre Supérieure (Topbar)
        topbar = new Topbar();

        // 3. Initialiser la Barre d'État (Statusbar)
        statusbar = new Statusbar();

        // 4. Initialiser la Sidebar avec son écouteur de navigation
        sidebar = new Sidebar(viewId -> {
            cardLayout.show(workzone, viewId);

            switch (viewId) {
                case "VIEW_DASHBOARD" -> topbar.updateTitle("Tableau de bord");
                case "VIEW_ABONNES" -> topbar.updateTitle("Abonnés & Contrats");
                case "VIEW_RELEVES" -> topbar.updateTitle("Relevés");
                case "VIEW_CAISSE" -> topbar.updateTitle("Caisse & Facturation");
            }
        });

        add(sidebar, BorderLayout.WEST);
        add(statusbar, BorderLayout.SOUTH);

        setJMenuBar(topbar);
        add(workzone, BorderLayout.CENTER);
    }

    private void initWorkzone() {
        cardLayout = new CardLayout();
        workzone = new JPanel(cardLayout);

        // --- Configuration des données dumpées pour les abonnés ---
        String[] columns = { "ID ABONNÉ", "NOM", "N° COMPTEUR", "CATÉGORIE", "ANCIEN INDEX", "CONSO (KWH)", "STATUT" };
        Object[][] data = {
                { "AB-1000", "Ngo Bilong A.", "CMP-20000", "Social", "2 803", "—", "En attente" },
                { "AB-1001", "Etoa Ekani C.", "CMP-20007", "Résidentiel", "1 433", "135", "Impayée" },
                { "AB-1002", "Fouda Mballa P.", "CMP-20014", "Résidentiel", "4 955", "218", "Impayée" },
                { "AB-1003", "Atangana R.", "CMP-20021", "Industriel", "2 940", "—", "En attente" },
                { "AB-1004", "Mendomo S.", "CMP-20028", "Résidentiel", "3 769", "309", "Payée" },
                { "AB-1005", "Talla J.", "CMP-20035", "Social", "3 558", "94", "Impayée" }
        };

        DefaultTableModel abonnesModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String[] columnsC = { "ABONNÉ", "COMPTEUR", "CATÉGORIE", "CONSO (KWH)", "TTC", "STATUT", "" };
        Object[][] dataC = {
                { "Njoya K.", "CMP-20098", "Résidentiel", "303", "34 326 FCFA", "Impayée", "Encaisser" },
                { "Fouda Mballa P.", "CMP-20014", "Résidentiel", "274", "31 041 FCFA", "Impayée", "Encaisser" },
                { "Sané Aïcha", "CMP-20133", "Résidentiel", "271", "30 701 FCFA", "Impayée", "Encaisser" },
                { "Etoa Ekani C.", "CMP-20007", "Résidentiel", "105", "11 895 FCFA", "Impayée", "Encaisser" },
                { "Essama F.", "CMP-20077", "Résidentiel", "75", "8 497 FCFA", "Impayée", "Encaisser" }
        };

        DefaultTableModel caisseModel = new DefaultTableModel(dataC, columnsC) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // --- Configuration du modèle de l'historique des relevés (Session) ---
        String[] columnsR = { "HEURE", "ABONNÉ", "COMPTEUR", "CONSO (KWH)" };
        sessionRelevesModel = new DefaultTableModel(columnsR, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // --- Initialisation des Vues ---
        JPanel viewDashboard = createTempView("Contenu du Tableau de Bord");
        JPanel viewAbonnes = new AbonnesView(abonnesModel);
        JPanel viewCaisse = new CaisseView(caisseModel);

        // Instanciation de la vue Releves
        RelevesView viewReleves = new RelevesView();

        // =========================================================
        // CONNEXION DES CALLBACKS AVEC LOGIQUE DE MOCK DYNAMIQUE
        // =========================================================

        // 1. Callback de Recherche de compteur [Entrée]
        viewReleves.setOnSearchListener(query -> {
            if (query.equalsIgnoreCase(mockCompteur) || query.toLowerCase().contains("etoa")) {
                // Simule la découverte de l'abonné ciblé
                viewReleves.setSubscriberFoundState(mockCompteur, mockNom, mockCategorie, mockAncienIndex);
            } else {
                // Feedback si le matricule n'est pas le bon (pour aider le test)
                JOptionPane.showMessageDialog(this,
                        "Abonné introuvable.\n(Pour tester le mock, veuillez saisir : CMP-20007)",
                        "Kilowatch Simulation",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 2. Callback de Validation du nouvel index
        viewReleves.setOnValidateIndexListener(nouvelIndex -> {
            int consommation = nouvelIndex - mockAncienIndex;

            // Protection anti-index inférieur (Le composant passe déjà en rouge, mais on
            // bloque la validation)
            if (consommation < 0) {
                JOptionPane.showMessageDialog(this,
                        "Impossible de valider : Le nouvel index est inférieur à l'ancien !",
                        "Erreur de saisie",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Génération de l'heure système réelle du relevé
            String heureActuelle = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            // Injection de la ligne calculée dans le modèle partagé
            sessionRelevesModel.insertRow(0, new Object[] {
                    heureActuelle,
                    mockNom,
                    mockCompteur,
                    String.format("%,d", consommation).replace(',', ' ')
            });

            totalSaisiesSession++;

            // Notification de mise à jour des structures de données à la vue secondaire
            viewReleves.updateSessionTable(sessionRelevesModel, totalSaisiesSession);

            // Retour automatique à l'état initial d'attente de scan
            viewReleves.resetToSearchState();
        });

        // 3. Callback d'annulation ou changement d'abonné ciblée
        viewReleves.setOnChangeSubscriberListener(() -> {
            // Logique de nettoyage annexe si nécessaire
            System.out.println("Changement d'abonné requis par l'opérateur.");
        });

        // --- Enregistrement dans la zone d'affichage (Workzone) ---
        workzone.add(viewDashboard, "VIEW_DASHBOARD");
        workzone.add(viewAbonnes, "VIEW_ABONNES");
        workzone.add(viewReleves, "VIEW_RELEVES");
        workzone.add(viewCaisse, "VIEW_CAISSE");
    }

    private JPanel createTempView(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppColors.BG_DEEP);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Inter", Font.PLAIN, 16));
        label.setForeground(AppColors.TEXT_SECONDARY);

        panel.add(label);
        return panel;
    }
}
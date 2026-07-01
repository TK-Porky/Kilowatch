package com.kilowatch.view.ui;

import com.kilowatch.view.data.ViewDataService;
import com.kilowatch.view.data.ViewDto.Abonne;
import com.kilowatch.view.data.ViewDto.FactureEnAttente;
import com.kilowatch.view.data.ViewDto.ReleveSession;
import com.kilowatch.view.data.ViewDto.ActionLog;
import com.kilowatch.view.data.ViewDto.DashboardStats;
import com.kilowatch.view.theme.AppColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainLayout extends JFrame {

    private CardLayout cardLayout;
    private JPanel workzone;
    private Topbar topbar;
    private Sidebar sidebar;
    private Statusbar statusbar;

    // LE MOTEUR DE DONNÉES
    private final ViewDataService dataService;

    // --- MODÈLES ET VUES GLOBAUX ---
    private DefaultTableModel abonnesModel;
    private DefaultTableModel caisseModel;
    private DefaultTableModel sessionRelevesModel;
    private DefaultTableModel recentActionsModel;

    private CaisseView viewCaisse;
    private DashboardView viewDashboard;

    // --- ÉTATS (Uniquement ceux liés à l'interface pure) ---
    private int totalSaisiesSession = 0;
    private String releveCurrentCompteur = "";
    private String currentCaisseFilter = "FILTER_UNPAID";
    private boolean currentCaisseSortDesc = true;

    public MainLayout(ViewDataService dataService) {
        this.dataService = dataService;

        setTitle("Kilowatch — Plateforme de Suivi de Consommation");
        setSize(1280, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        initWorkzone();

        topbar = new Topbar();
        statusbar = new Statusbar();
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

        // =========================================================
        // INITIALISATION DES MODÈLES ET DES VUES
        // =========================================================

        String[] colAbonnes = { "ID ABONNÉ", "NOM", "N° COMPTEUR", "CATÉGORIE", "ANCIEN INDEX", "CONSO (KWH)",
                "STATUT" };
        abonnesModel = new DefaultTableModel(colAbonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        AbonnesView viewAbonnes = new AbonnesView(abonnesModel);
        refreshAbonnesTable("");

        String[] colCaisse = { "ABONNÉ", "COMPTEUR", "CATÉGORIE", "CONSO (KWH)", "TTC", "STATUT", "" };
        caisseModel = new DefaultTableModel(colCaisse, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        viewCaisse = new CaisseView(caisseModel);
        refreshCaisseTable();

        String[] colReleves = { "HEURE", "ABONNÉ", "COMPTEUR", "CONSO (KWH)" };
        sessionRelevesModel = new DefaultTableModel(colReleves, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        RelevesView viewReleves = new RelevesView();

        String[] colActions = { "HORODATAGE", "ACTION" };
        recentActionsModel = new DefaultTableModel(colActions, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        viewDashboard = new DashboardView(recentActionsModel);

        // Premier affichage du Dashboard (le service a déjà loggé le "Démarrage")
        refreshDashboard();

        // =========================================================
        // CONNEXIONS DES ÉVÉNEMENTS
        // =========================================================

        // --- VUE ABONNÉS ---
        viewAbonnes.setOnSearchListener(this::refreshAbonnesTable);

        viewAbonnes.setOnAddAbonneListener(() -> {
            AbonneFormDialog dialog = new AbonneFormDialog(this);
            dialog.setOnSaveListener(nouvelAbonne -> {
                Abonne savedAbonne = dataService.saveAbonne(nouvelAbonne);
                if (savedAbonne != null) {
                    abonnesModel.insertRow(0, new Object[] {
                            savedAbonne.id(), savedAbonne.nom(), savedAbonne.numeroCompteur(),
                            savedAbonne.categorie(), String.format("%,d", savedAbonne.ancienIndex()).replace(',', ' '),
                            "—", savedAbonne.statut()
                    });
                    refreshDashboard(); // Le service s'est occupé de tout !
                }
            });
            dialog.setVisible(true);
        });

        // --- VUE RELEVÉS ---
        viewReleves.setOnSearchListener(query -> {
            Abonne abonne = dataService.findAbonneForReleve(query);
            if (abonne != null) {
                releveCurrentCompteur = abonne.numeroCompteur();
                viewReleves.setSubscriberFoundState(abonne.numeroCompteur(), abonne.nom(), abonne.categorie(),
                        abonne.ancienIndex());
            } else {
                JOptionPane.showMessageDialog(this, "Abonné introuvable.", "Kilowatch",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        viewReleves.setOnValidateIndexListener(nouvelIndex -> {
            try {
                ReleveSession session = dataService.validerNouvelIndex(releveCurrentCompteur, nouvelIndex);

                sessionRelevesModel.insertRow(0, new Object[] {
                        session.heure(), session.nomAbonne(), session.numeroCompteur(),
                        String.format("%,d", session.consoKwh()).replace(',', ' ')
                });

                totalSaisiesSession++;
                viewReleves.updateSessionTable(sessionRelevesModel, totalSaisiesSession);
                viewReleves.resetToSearchState();

                refreshAbonnesTable("");
                refreshCaisseTable();
                refreshDashboard(); // Le service a généré la facture et loggé l'action !

                releveCurrentCompteur = "";
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewReleves.setOnChangeSubscriberListener(() -> releveCurrentCompteur = "");

        // --- VUE CAISSE ---
        viewCaisse.setOnEncaisserListener(numeroCompteur -> {
            boolean success = dataService.encaisserFacture(numeroCompteur);
            if (success) {
                refreshCaisseTable();
                refreshAbonnesTable("");
                refreshDashboard(); // Le service a calculé le CA et loggé l'action !

                JOptionPane.showMessageDialog(this, "Facture encaissée avec succès !", "Caisse Kilowatch",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        viewCaisse.setOnFilterChangedListener(filterId -> {
            currentCaisseFilter = filterId;
            refreshCaisseTable();
        });

        viewCaisse.setOnSortChangedListener(isDescending -> {
            currentCaisseSortDesc = isDescending;
            refreshCaisseTable();
        });

        viewCaisse.setOnExportListener(() -> {
            dataService.registrarAction("Tentative d'export CSV (en développement)");
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "L'export CSV sera bientôt implémenté.", "Export CSV",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        workzone.add(viewDashboard, "VIEW_DASHBOARD");
        workzone.add(viewAbonnes, "VIEW_ABONNES");
        workzone.add(viewReleves, "VIEW_RELEVES");
        workzone.add(viewCaisse, "VIEW_CAISSE");
    }

    // =========================================================
    // MÉTHODES DE RAFRAÎCHISSEMENT DYNAMIQUES
    // =========================================================

    /**
     * Récupère les données depuis le DataService et met à jour la vue pure (Dumb
     * View)
     */
    private void refreshDashboard() {
        if (viewDashboard == null)
            return;

        // 1. On demande l'état des KPIs au service
        DashboardStats stats = dataService.getDashboardStats();

        // 2. On injecte dans la vue
        String abonneSub = stats.abonnesInscritsCeMois() == 0 ? "Aucun inscrit ce mois"
                : "+" + stats.abonnesInscritsCeMois() + " inscrit(s) ce mois";
        viewDashboard.updateKpiAbonnes(String.valueOf(stats.totalAbonnes()), abonneSub);

        viewDashboard.updateKpiCa(String.format("%,.0f FCFA", stats.chiffreAffaires()).replace(',', ' '),
                "Total encaissé lors de cette session");

        int pourcentage = stats.totalFactures() == 0 ? 0
                : (int) Math.round(((double) stats.facturesPayees() / stats.totalFactures()) * 100);
        viewDashboard.updateKpiRecouvrement(pourcentage + "%",
                stats.facturesPayees() + " factures payées sur " + stats.totalFactures());

        viewDashboard.setChartData(stats.encaissements7DerniersJours());

        // 3. On rafraîchit la table des actions récentes
        recentActionsModel.setRowCount(0);
        for (ActionLog log : dataService.getRecentActions()) {
            recentActionsModel.addRow(new Object[] { log.horodatage(), log.description() });
        }
    }

    private void refreshAbonnesTable(String query) {
        abonnesModel.setRowCount(0);
        List<Abonne> list = (query == null || query.trim().isEmpty())
                ? dataService.getAllAbonnes()
                : dataService.searchAbonnes(query);

        for (Abonne a : list) {
            abonnesModel.addRow(new Object[] {
                    a.id(), a.nom(), a.numeroCompteur(), a.categorie(),
                    String.format("%,d", a.ancienIndex()).replace(',', ' '), "—", a.statut()
            });
        }
    }

    private void refreshCaisseTable() {
        caisseModel.setRowCount(0);
        double totalCalcule = 0.0;

        List<FactureEnAttente> factures = new ArrayList<>(dataService.getFacturesEnAttente());

        factures.sort((f1, f2) -> {
            int cmp = Double.compare(f1.montantTtc(), f2.montantTtc());
            return currentCaisseSortDesc ? -cmp : cmp;
        });

        for (FactureEnAttente f : factures) {
            if (currentCaisseFilter.equals("FILTER_PAID"))
                continue;

            caisseModel.addRow(new Object[] {
                    f.nomAbonne(), f.numeroCompteur(), f.categorie(), f.consoKwh(),
                    String.format("%,.0f FCFA", f.montantTtc()).replace(',', ' '), "Impayée", "Encaisser"
            });
            totalCalcule += f.montantTtc();
        }

        if (viewCaisse != null) {
            viewCaisse.setTotalAmount(String.format("%,.0f FCFA", totalCalcule).replace(',', ' '));
        }
    }
}
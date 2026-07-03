package com.kilowatch.view.ui;

import com.kilowatch.view.component.ExportProgressDialog;
import com.kilowatch.view.data.ViewDataService;
import com.kilowatch.view.data.ViewEnum.*; // <-- Importation globale des Enums sécurisés
import com.kilowatch.view.data.ViewDto.Abonne;
import com.kilowatch.view.data.ViewDto.FactureEnAttente;
import com.kilowatch.view.data.ViewDto.ReleveSession;
import com.kilowatch.view.data.ViewDto.ActionLog;
import com.kilowatch.view.data.ViewDto.DashboardStats;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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

    // Vues promues en attributs pour la synchronisation visuelle depuis la Topbar
    private AbonnesView viewAbonnes;
    private CaisseView viewCaisse;
    private DashboardView viewDashboard;

    // --- ÉTATS CONFIGURABLES ET TYPÉS DE L'INTERFACE ---
    private int totalSaisiesSession = 0;
    private String releveCurrentCompteur = "";

    // États typés pour les Abonnés
    private String currentAbonneQuery = "";
    private FiltreAbonne currentAbonneFilter = FiltreAbonne.FILTER_ALL;

    // États typés pour la Caisse (Mise à jour avec le filtre par catégorie)
    private FiltreCaisse currentCaisseFilter = FiltreCaisse.FILTER_ALL;
    private boolean currentCaisseSortDesc = true;

    // Constantes d'UI
    private static final String ACTION_ENCAISSER = "Encaisser";

    public MainLayout(ViewDataService dataService) {
        this.dataService = dataService;

        setTitle("Kilowatch — Plateforme de Suivi de Consommation");
        setSize(1280, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        topbar = new Topbar();
        initWorkzone();

        statusbar = new Statusbar();
        sidebar = new Sidebar(this::navigateToView, this::logout);

        add(sidebar, BorderLayout.WEST);
        add(statusbar, BorderLayout.SOUTH);
        setJMenuBar(topbar);
        add(workzone, BorderLayout.CENTER);

        refreshStatusbar();
    }

    private void navigateToView(String viewId) {
        AppView targetView = AppView.fromId(viewId);
        cardLayout.show(workzone, targetView.getId());
        topbar.updateTitle(targetView.getTitre());

        // SYNCHRONISATION VISUELLE : Met à jour l'item actif dans la Sidebar
        if (sidebar != null) {
            sidebar.setActiveMenu(viewId);
        }

        if (targetView == AppView.DASHBOARD) {
            refreshDashboard();
        }
    }

    private void logout() {
        // Demande de confirmation
        int reponse = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment vous déconnecter ?",
                "Déconnexion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (reponse == JOptionPane.YES_OPTION) {
            // 1. On informe le service de la déconnexion
            dataService.deconnecter();

            // 2. On ferme proprement la fenêtre principale
            this.dispose();

            // 3. (Optionnel) Si vous voulez relancer l'application complètement
            // pour retomber sur le LoginDialog, vous pouvez simplement faire :
            // Main.main(new String[0]);

            // Ou si vous préférez quitter totalement :
            System.exit(0);
        }
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
        viewAbonnes = new AbonnesView(abonnesModel); // Utilisation de l'attribut de classe
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

        refreshDashboard();

        // =========================================================
        // CONNEXIONS DES ÉVÉNEMENTS DE LA TOPBAR
        // =========================================================

        topbar.setOnNewAbonneListener(this::openNewAbonneDialog);
        topbar.setOnGoToRelevesListener(() -> navigateToView(AppView.RELEVES.getId()));

        topbar.setOnGoToImpayesListener(() -> {
            currentCaisseFilter = FiltreCaisse.FILTER_ALL;
            refreshCaisseTable();

            // SYNCHRONISATION VISUELLE : Met à jour le SwitchGroup de la Caisse
            if (viewCaisse != null) {
                viewCaisse.setSelectedFilter(currentCaisseFilter.name());
            }

            navigateToView(AppView.CAISSE.getId());
        });

        topbar.setOnGoToGrosConsommateursListener(() -> {
            currentAbonneFilter = FiltreAbonne.FILTER_GROS_CONS;
            refreshAbonnesTable(currentAbonneQuery);

            // SYNCHRONISATION VISUELLE : Met à jour le SwitchGroup des Abonnés
            if (viewAbonnes != null) {
                viewAbonnes.setSelectedFilter(currentAbonneFilter.name());
            }

            navigateToView(AppView.ABONNES.getId());
        });

        topbar.setOnExportCsvListener(this::triggerCsvExport);
        topbar.setOnLogoutListener(this::logout);

        // =========================================================
        // CONNEXIONS DES ÉVÉNEMENTS DES VUES ENFANTS
        // =========================================================

        // --- VUE ABONNÉS ---
        viewAbonnes.setOnSearchListener(query -> {
            this.currentAbonneQuery = query;
            refreshAbonnesTable(query);
        });

        viewAbonnes.setOnFilterChangedListener(filterId -> {
            this.currentAbonneFilter = FiltreAbonne.valueOf(filterId);
            refreshAbonnesTable(currentAbonneQuery);
        });

        viewAbonnes.setOnAddAbonneListener(this::openNewAbonneDialog);

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

                // Rafraîchit les tables pour afficher la nouvelle conso et la nouvelle facture
                refreshAbonnesTable(currentAbonneQuery);
                refreshCaisseTable();
                refreshDashboard();

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
                refreshAbonnesTable(currentAbonneQuery); // Rafraîchit pour remettre la conso à 0
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Facture encaissée avec succès !", "Caisse Kilowatch",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        viewCaisse.setOnFilterChangedListener(filterId -> {
            this.currentCaisseFilter = FiltreCaisse.valueOf(filterId);
            refreshCaisseTable();
        });

        viewCaisse.setOnSortChangedListener(isDescending -> {
            currentCaisseSortDesc = isDescending;
            refreshCaisseTable();
        });

        viewCaisse.setOnExportListener(this::triggerCsvExport);

        workzone.add(viewDashboard, AppView.DASHBOARD.getId());
        workzone.add(viewAbonnes, AppView.ABONNES.getId());
        workzone.add(viewReleves, AppView.RELEVES.getId());
        workzone.add(viewCaisse, AppView.CAISSE.getId());
    }

    // =========================================================
    // STATUS BAR SYNCHRONISATION
    // =========================================================

    private void refreshStatusbar() {
        if (statusbar != null) {
            statusbar.updateStatusbar(dataService.getStatusbarInfo());
        }
    }

    // =========================================================
    // COUCHE DE PRESENTATION PURIFIÉE (DUMB RENDERING)
    // =========================================================

    private void openNewAbonneDialog() {
        AbonneFormDialog dialog = new AbonneFormDialog(this);
        dialog.setOnSaveListener(nouvelAbonne -> {
            Abonne savedAbonne = dataService.saveAbonne(nouvelAbonne);
            if (savedAbonne != null) {
                refreshAbonnesTable(currentAbonneQuery);
                refreshDashboard();
            }
        });
        dialog.setVisible(true);
    }

    private void triggerCsvExport() {
        // 1. Configuration et ouverture du sélecteur de fichier standard
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer l'export des impayés");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File("factures_impayees_" + java.time.LocalDate.now() + ".csv"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // Annulation de l'utilisateur
        }

        // Récupération et sécurisation de l'extension .csv
        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
        }

        final File finalFile = fileToSave;
        dataService.registrarAction("Lancement export CSV vers : " + finalFile.getName());
        refreshDashboard();

        // 2. Instanciation du nouveau composant modulaire
        ExportProgressDialog progressDialog = new ExportProgressDialog(this, "Kilowatch — Exportation");

        // 3. Liaison asynchrone avec la couche métier (ViewDataService)
        dataService.exporterFacturesImpayees(finalFile, pct -> {
            // On pilote le composant de progression de l'extérieur
            progressDialog.updateProgress(pct, "Écriture du fichier : " + pct + "%");
        }).thenAccept(totalLines -> {
            // Cas de succès : Fermeture du dialogue et notification
            progressDialog.safeDispose();
            SwingUtilities.invokeLater(() -> {
                dataService.registrarAction("Export CSV terminé : " + totalLines + " factures générées.");
                refreshDashboard();
                JOptionPane.showMessageDialog(this,
                        "L'exportation a été complétée avec succès !\n\n" +
                                "Fichier créé : " + finalFile.getName() + "\n" +
                                "Total lignes exportées : " + totalLines,
                        "Succès Exportation", JOptionPane.INFORMATION_MESSAGE);
            });
        }).exceptionally(ex -> {
            // Cas d'erreur : Fermeture du dialogue et affichage du problème (ex: fichier
            // ouvert dans Excel)
            progressDialog.safeDispose();
            SwingUtilities.invokeLater(() -> {
                String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                dataService.registrarAction("Erreur export CSV : " + errorMsg);
                refreshDashboard();
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la génération du fichier CSV :\n" + errorMsg,
                        "Erreur d'écriture", JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });

        // 4. Affichage de la boîte de dialogue (Bloquante visuellement mais laisse
        // tourner l'arrière-plan)
        progressDialog.setVisible(true);
    }

    private void refreshDashboard() {
        if (viewDashboard == null)
            return;

        DashboardStats stats = dataService.getDashboardStats();

        String abonneSub = stats.abonnesInscrits() == 0 ? "Aucun inscrit cette session"
                : "+" + stats.abonnesInscrits() + " inscrit(s)";
        viewDashboard.updateKpiAbonnes(String.valueOf(stats.totalAbonnes()), abonneSub);
        viewDashboard.updateKpiCa(String.format("%,.0f FCFA", stats.chiffreAffaires()).replace(',', ' '),
                "Total encaissé lors de cette session");

        int pourcentage = stats.totalFactures() == 0 ? 0
                : (int) Math.round(((double) stats.facturesPayees() / stats.totalFactures()) * 100);
        viewDashboard.updateKpiRecouvrement(pourcentage + "%",
                stats.facturesPayees() + " factures payées sur " + stats.totalFactures());

        viewDashboard.setChartData(stats.encaissements7DerniersJours());

        recentActionsModel.setRowCount(0);
        for (ActionLog log : dataService.getRecentActions()) {
            recentActionsModel.addRow(new Object[] { log.horodatage(), log.description() });
        }
    }

    private void refreshAbonnesTable(String query) {
        abonnesModel.setRowCount(0);

        List<Abonne> list = dataService.getFilteredAbonnes(query, currentAbonneFilter.name());

        for (Abonne a : list) {
            // Appel dynamique pour récupérer la consommation réelle en attente !
            int consoActive = dataService.getConsoEnAttente(a.numeroCompteur());
            String consoAffichee = (consoActive > 0) ? String.format("%,d", consoActive).replace(',', ' ') : "0";

            abonnesModel.addRow(new Object[] {
                    a.id(),
                    a.nom(),
                    a.numeroCompteur(),
                    a.categorie(),
                    String.format("%,d", a.ancienIndex()).replace(',', ' '),
                    consoAffichee,
                    a.statut()
            });
        }
    }

    private void refreshCaisseTable() {
        caisseModel.setRowCount(0);

        List<FactureEnAttente> facturesTraitees = dataService.getFilteredFactures(currentCaisseFilter.name(),
                currentCaisseSortDesc);

        double totalCalcule = 0.0;
        for (FactureEnAttente f : facturesTraitees) {
            caisseModel.addRow(new Object[] {
                    f.nomAbonne(),
                    f.numeroCompteur(),
                    f.categorie(),
                    f.consoKwh(),
                    String.format("%,.0f FCFA", f.montantTtc()).replace(',', ' '),
                    StatutFacture.IMPAYEE.getLibelle(), // Utilisation propre de l'Enum métier
                    ACTION_ENCAISSER
            });
            totalCalcule += f.montantTtc();
        }

        if (viewCaisse != null) {
            viewCaisse.setTotalAmount(String.format("%,.0f FCFA", totalCalcule).replace(',', ' '));
        }
    }
}
package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton;
import com.kilowatch.view.component.SwitchGroup;
import com.kilowatch.view.component.Table;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.theme.AppIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import java.awt.*;

public class CaisseView extends JPanel {

    private JLabel totalValueLabel;
    private Table table; // On garde la référence du tableau
    private boolean isSortDescending = true; // État initial du tri

    public CaisseView(TableModel tableModel) {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_DEEP);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        // -- COMPOSANT : EN-TÊTE GAUCHE (TEXTES) --
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel titleLabel = new JLabel("Caisse & Facturation");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 26));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Encaissement des paiements et export du lot pour la comptabilité");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(AppColors.TEXT_SECONDARY);

        headerLeft.add(titleLabel);
        headerLeft.add(Box.createVerticalStrut(8));
        headerLeft.add(subtitleLabel);

        // -- COMPOSANT : BOUTON EXPORT CSV --
        AppButton exportBtn = new AppButton("Exporter le lot CSV", AppIcons.DOWNLOAD, AppButton.Theme.PRIMARY);
        exportBtn.setPreferredSize(new Dimension(300, 42));

        // -- CONTENEUR : LIGNE D'EN-TÊTE GLOBAL --
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(headerLeft, BorderLayout.WEST);

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        exportPanel.setOpaque(false);
        exportPanel.add(exportBtn);
        headerRow.add(exportPanel, BorderLayout.EAST);

        // -- CONTENEUR : LIGNE DES FILTRES ET ACTIONS --
        JPanel filtersRow = new JPanel(new BorderLayout());
        filtersRow.setOpaque(false);
        filtersRow.setBorder(new EmptyBorder(24, 0, 24, 0));

        // -- COMPOSANT : COMMUTATEUR D'ONGLETS (SWITCH GROUP) --
        SwitchGroup filterSwitch = new SwitchGroup();
        filterSwitch.addSwitch("FILTER_ALL", "Toutes", false);
        filterSwitch.addSwitch("FILTER_PAID", "Payées", false);
        filterSwitch.addSwitch("FILTER_UNPAID", "Impayées", true);

        filterSwitch.setOnSwitchListener(filterId -> {
            switch (filterId) {
                case "FILTER_ALL" -> System.out.println("Afficher toutes les factures");
                case "FILTER_PAID" -> System.out.println("Afficher uniquement les factures payées");
                case "FILTER_UNPAID" -> System.out.println("Afficher uniquement les factures impayées");
            }
        });

        JPanel filterTabsWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterTabsWrapper.setOpaque(false);
        filterTabsWrapper.add(filterSwitch);

        // -- COMPOSANT : BOUTON DE TRI DYNAMIQUE --
        AppButton sortBtn = new AppButton("Trier par montant décroissant", AppIcons.MOVE_DOWN,
                AppButton.Theme.SECONDARY);
        sortBtn.setFont(new Font("Inter", Font.PLAIN, 13));
        sortBtn.setPadding(6, 16, 6, 16);
        sortBtn.setPreferredSize(new Dimension(290, 35));

        sortBtn.addActionListener(e -> {
            isSortDescending = !isSortDescending;
            if (isSortDescending) {
                sortBtn.setText("Trier par montant décroissant");
                sortBtn.setIconEnum(AppIcons.MOVE_DOWN);
                System.out.println("Action : Tri par montant décroissant");
            } else {
                sortBtn.setText("Trier par montant croissant");
                sortBtn.setIconEnum(AppIcons.MOVE_UP);
                System.out.println("Action : Tri par montant croissant");
            }
        });

        filterTabsWrapper.add(sortBtn);

        // -- COMPOSANT : ZONE D'AFFICHAGE DU TOTAL --
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        totalPanel.setOpaque(false);

        JLabel totalTextLabel = new JLabel("Total affiché : ");
        totalTextLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        totalTextLabel.setForeground(AppColors.TEXT_SECONDARY);

        totalValueLabel = new JLabel("0 FCFA");
        totalValueLabel.setFont(new Font("Inter", Font.BOLD, 14));
        totalValueLabel.setForeground(AppColors.ACCENT_AMBER);

        totalPanel.add(totalTextLabel);
        totalPanel.add(totalValueLabel);

        filtersRow.add(filterTabsWrapper, BorderLayout.WEST);
        filtersRow.add(totalPanel, BorderLayout.EAST);

        // -- CONTENEUR : ZONE SUPÉRIEURE (EN-TÊTE + FILTRES) --
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(headerRow, BorderLayout.NORTH);
        topContainer.add(filtersRow, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // -- COMPOSANT : TABLEAU DE DONNÉES PRINCIPAL --
        table = new Table(tableModel);
        setupTableColumns();

        add(table.createRoundedContainer(), BorderLayout.CENTER);
    }

    public void setTotalAmount(String amount) {
        if (totalValueLabel != null) {
            totalValueLabel.setText(amount);
        }
    }

    /**
     * Configuration propre du tableau.
     */
    private void setupTableColumns() {
        Table.BadgeCellRenderer catRenderer = new Table.BadgeCellRenderer();
        catRenderer.registerStyle("Résidentiel", new Table.BadgeStyle(AppColors.AMBER_SOFT, AppColors.ACCENT_AMBER));
        catRenderer.registerStyle("Industriel", new Table.BadgeStyle(AppColors.PURPLE_SOFT, AppColors.ACCENT_PURPLE));
        catRenderer.registerStyle("Social", new Table.BadgeStyle(AppColors.BLUE_SOFT, AppColors.ACCENT_BLUE));

        Table.BadgeCellRenderer statRenderer = new Table.BadgeCellRenderer();
        statRenderer.registerStyle("Impayée", new Table.BadgeStyle(AppColors.RED_SOFT, AppColors.STATUS_RED));
        statRenderer.registerStyle("Payée", new Table.BadgeStyle(AppColors.GREEN_SOFT, AppColors.STATUS_GREEN));

        for (int i = 0; i < table.getColumnCount(); i++) {
            String colName = table.getColumnName(i);

            if ("CONSO (KWH)".equalsIgnoreCase(colName) || "TTC".equalsIgnoreCase(colName)) {
                table.setColumnAlignment(i, SwingConstants.RIGHT);
            } else if ("CATÉGORIE".equalsIgnoreCase(colName)) {
                table.getColumnModel().getColumn(i).setCellRenderer(catRenderer);
            } else if ("STATUT".equalsIgnoreCase(colName)) {
                table.getColumnModel().getColumn(i).setCellRenderer(statRenderer);
            } else if ("".equals(colName) || "ACTION".equalsIgnoreCase(colName)) {
                table.setActionColumn(i, "Encaisser", e -> {
                    int row = Integer.parseInt(e.getActionCommand());
                    if (row != -1) {
                        String idAbonne = (String) table.getValueAt(row, 0);
                        System.out.println(
                                "Action Encaisser cliquée pour l'abonné : " + idAbonne + " (Ligne: " + row + ")");
                    }
                });
            }
        }
    }
}
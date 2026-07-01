package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton;
import com.kilowatch.view.component.SwitchGroup;
import com.kilowatch.view.component.Table;
import com.kilowatch.view.data.ViewEnum.CategorieAbonne;
import com.kilowatch.view.data.ViewEnum.StatutFacture;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.theme.AppIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.function.Consumer;

public class CaisseView extends JPanel {

    private JLabel totalValueLabel;
    private Table table;
    private boolean isSortDescending = true;

    // --- DÉCLARATION DES CALLBACKS ---
    private Consumer<String> onEncaisserListener;
    private Consumer<String> onFilterChangedListener; // Pour le SwitchGroup
    private Consumer<Boolean> onSortChangedListener; // Pour le bouton de tri
    private Runnable onExportListener; // Pour le bouton d'export CSV

    public CaisseView(TableModel tableModel) {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_DEEP);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        // -- EN-TÊTE GAUCHE --
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

        // -- BOUTON EXPORT CSV --
        AppButton exportBtn = new AppButton("Exporter le lot CSV", AppIcons.DOWNLOAD, AppButton.Theme.PRIMARY);
        exportBtn.setPreferredSize(new Dimension(300, 42));

        // CONNEXION DU CALLBACK D'EXPORT
        exportBtn.addActionListener(e -> {
            if (onExportListener != null) {
                onExportListener.run();
            }
        });

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(headerLeft, BorderLayout.WEST);

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        exportPanel.setOpaque(false);
        exportPanel.add(exportBtn);
        headerRow.add(exportPanel, BorderLayout.EAST);

        // -- FILTRES ET ACTIONS --
        JPanel filtersRow = new JPanel(new BorderLayout());
        filtersRow.setOpaque(false);
        filtersRow.setBorder(new EmptyBorder(24, 0, 24, 0));

        SwitchGroup filterSwitch = new SwitchGroup();
        filterSwitch.addSwitch("FILTER_ALL", "Toutes", false);
        filterSwitch.addSwitch("FILTER_PAID", "Payées", false);
        filterSwitch.addSwitch("FILTER_UNPAID", "Impayées", true);

        // CONNEXION DU CALLBACK DE FILTRE
        filterSwitch.setOnSwitchListener(filterId -> {
            if (onFilterChangedListener != null) {
                onFilterChangedListener.accept(filterId);
            }
        });

        JPanel filterTabsWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterTabsWrapper.setOpaque(false);
        filterTabsWrapper.add(filterSwitch);

        // -- BOUTON DE TRI DYNAMIQUE --
        AppButton sortBtn = new AppButton("Trier par montant décroissant", AppIcons.MOVE_DOWN,
                AppButton.Theme.SECONDARY);
        sortBtn.setFont(new Font("Inter", Font.PLAIN, 13));
        sortBtn.setPadding(6, 16, 6, 16);
        sortBtn.setPreferredSize(new Dimension(290, 35));

        // CONNEXION DU CALLBACK DE TRI
        sortBtn.addActionListener(e -> {
            isSortDescending = !isSortDescending;
            if (isSortDescending) {
                sortBtn.setText("Trier par montant décroissant");
                sortBtn.setIconEnum(AppIcons.MOVE_DOWN);
            } else {
                sortBtn.setText("Trier par montant croissant");
                sortBtn.setIconEnum(AppIcons.MOVE_UP);
            }

            if (onSortChangedListener != null) {
                onSortChangedListener.accept(isSortDescending);
            }
        });

        filterTabsWrapper.add(sortBtn);

        // -- ZONE D'AFFICHAGE DU TOTAL --
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

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(headerRow, BorderLayout.NORTH);
        topContainer.add(filtersRow, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // -- TABLEAU --
        table = new Table(tableModel);
        setupTableColumns();
        add(table.createRoundedContainer(), BorderLayout.CENTER);
    }

    public void setTotalAmount(String amount) {
        if (totalValueLabel != null) {
            totalValueLabel.setText(amount);
        }
    }

    private void setupTableColumns() {
        Table.BadgeCellRenderer catRenderer = new Table.BadgeCellRenderer();
        catRenderer.registerStyle(CategorieAbonne.RESIDENTIEL.getLibelle(),
                new Table.BadgeStyle(AppColors.AMBER_SOFT, AppColors.ACCENT_AMBER));
        catRenderer.registerStyle(CategorieAbonne.INDUSTRIEL.getLibelle(),
                new Table.BadgeStyle(AppColors.PURPLE_SOFT, AppColors.ACCENT_PURPLE));
        catRenderer.registerStyle(CategorieAbonne.SOCIAL.getLibelle(),
                new Table.BadgeStyle(AppColors.BLUE_SOFT, AppColors.ACCENT_BLUE));

        Table.BadgeCellRenderer statRenderer = new Table.BadgeCellRenderer();
        statRenderer.registerStyle(StatutFacture.IMPAYEE.getLibelle(),
                new Table.BadgeStyle(AppColors.RED_SOFT, AppColors.STATUS_RED));
        statRenderer.registerStyle(StatutFacture.PAYEE.getLibelle(),
                new Table.BadgeStyle(AppColors.GREEN_SOFT, AppColors.STATUS_GREEN));

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
                        String numeroCompteur = (String) table.getValueAt(row, 1);
                        if (onEncaisserListener != null) {
                            onEncaisserListener.accept(numeroCompteur);
                        }
                    }
                });
            }
        }
    }

    // --- SETTERS POUR LES CALLBACKS ---
    public void setOnEncaisserListener(Consumer<String> listener) {
        this.onEncaisserListener = listener;
    }

    public void setOnFilterChangedListener(Consumer<String> listener) {
        this.onFilterChangedListener = listener;
    }

    public void setOnSortChangedListener(Consumer<Boolean> listener) {
        this.onSortChangedListener = listener;
    }

    public void setOnExportListener(Runnable listener) {
        this.onExportListener = listener;
    }
}
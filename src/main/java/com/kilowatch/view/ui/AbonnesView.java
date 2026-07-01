package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton;
import com.kilowatch.view.component.SearchBar;
import com.kilowatch.view.component.SwitchGroup;
import com.kilowatch.view.component.Table;
import com.kilowatch.view.data.ViewEnum;
import com.kilowatch.view.data.ViewEnum.CategorieAbonne;
import com.kilowatch.view.data.ViewEnum.StatutFacture;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.theme.AppIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.function.Consumer;

public class AbonnesView extends JPanel {

    // --- 1. DÉCLARATION DES CALLBACKS ---
    private Consumer<String> onSearchListener;
    private Runnable onAddAbonneListener;
    private Consumer<String> onFilterChangedListener;

    // COMPOSANT PROMU EN ATTRIBUT DE CLASSE POUR LA SYNCHRONISATION
    private final SwitchGroup filterGroup;

    public AbonnesView(TableModel tableModel) {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_DEEP);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        // --- 2. EN-TÊTE (TEXTES À GAUCHE + BOUTON À DROITE) ---
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);

        // 2.A Textes (Ouest)
        JPanel leftTextsPanel = new JPanel();
        leftTextsPanel.setLayout(new BoxLayout(leftTextsPanel, BoxLayout.Y_AXIS));
        leftTextsPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Abonnés & Contrats");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 26));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Recherche et gestion de la clientèle post-payée");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(AppColors.TEXT_SECONDARY);

        leftTextsPanel.add(titleLabel);
        leftTextsPanel.add(Box.createVerticalStrut(8));
        leftTextsPanel.add(subtitleLabel);

        // 2.B Bouton Ajout (Est)
        JPanel rightButtonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtonContainer.setOpaque(false);

        AppButton addAbonneBtn = new AppButton("Nouvel Abonné", AppIcons.USER_PLUS, AppButton.Theme.PRIMARY);

        addAbonneBtn.addActionListener(e -> {
            if (onAddAbonneListener != null) {
                onAddAbonneListener.run();
            }
        });

        rightButtonContainer.add(addAbonneBtn);

        headerWrapper.add(leftTextsPanel, BorderLayout.WEST);
        headerWrapper.add(rightButtonContainer, BorderLayout.EAST);

        // --- 3. ZONE DE RECHERCHE & FILTRES ---
        SearchBar searchBar = new SearchBar("Recherche par ID, Nom, ou N° de Compteur... (F2)");
        searchBar.setOnSearchListener(query -> {
            if (onSearchListener != null) {
                onSearchListener.accept(query);
            }
        });

        // Instanciation de l'attribut de classe
        this.filterGroup = new SwitchGroup();
        for (ViewEnum.FiltreAbonne f : ViewEnum.FiltreAbonne.values()) {
            filterGroup.addSwitch(f.name(), f.getLibelle(), f == ViewEnum.FiltreAbonne.FILTER_ALL);
        }

        filterGroup.setOnSwitchListener(filterId -> {
            if (onFilterChangedListener != null) {
                onFilterChangedListener.accept(filterId);
            }
        });

        // Alignement horizontal de la barre de recherche et du groupe de filtres
        JPanel searchBarWrapper = new JPanel(new BorderLayout(16, 0));
        searchBarWrapper.setOpaque(false);
        searchBarWrapper.add(searchBar, BorderLayout.CENTER);
        searchBarWrapper.add(filterGroup, BorderLayout.EAST);

        // Conteneur de marge pour la zone de recherche
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setOpaque(false);
        searchContainer.setBorder(new EmptyBorder(24, 0, 24, 0));
        searchContainer.add(searchBarWrapper, BorderLayout.CENTER);

        // --- 4. AJOUT AU PANNEAU NORD ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(headerWrapper, BorderLayout.NORTH);
        northPanel.add(searchContainer, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // --- 5. INSTANCIATION DU COMPOSANT TABLE ---
        Table table = new Table(tableModel);

        // --- 6. CONFIGURATION DES COLONNES SPÉCIFIQUES ---
        Table.BadgeCellRenderer categorieRenderer = new Table.BadgeCellRenderer();
        categorieRenderer.registerStyle(CategorieAbonne.SOCIAL.getLibelle(),
                new Table.BadgeStyle(new Color(59, 130, 246, 38), Color.decode("#3b82f6")));
        categorieRenderer.registerStyle(CategorieAbonne.RESIDENTIEL.getLibelle(),
                new Table.BadgeStyle(AppColors.AMBER_SOFT, AppColors.ACCENT_AMBER));
        categorieRenderer.registerStyle(CategorieAbonne.INDUSTRIEL.getLibelle(),
                new Table.BadgeStyle(new Color(168, 85, 247, 38), Color.decode("#a855f7")));

        table.getColumnModel().getColumn(3).setCellRenderer(categorieRenderer);

        Table.BadgeCellRenderer statutRenderer = new Table.BadgeCellRenderer();
        statutRenderer.registerStyle(StatutFacture.PAYEE.getLibelle(),
                new Table.BadgeStyle(AppColors.GREEN_SOFT, AppColors.STATUS_GREEN));
        statutRenderer.registerStyle(StatutFacture.IMPAYEE.getLibelle(),
                new Table.BadgeStyle(AppColors.RED_SOFT, AppColors.STATUS_RED));
        statutRenderer.registerStyle(StatutFacture.EN_ATTENTE.getLibelle(),
                new Table.BadgeStyle(AppColors.BG_SURFACE_2, AppColors.TEXT_SECONDARY));

        table.getColumnModel().getColumn(6).setCellRenderer(statutRenderer);

        add(table.createRoundedContainer(), BorderLayout.CENTER);
    }

    // --- 7. MÉTHODES PUBLIQUES POUR LE MAINLAYOUT ---

    /**
     * Permet de forcer visuellement la sélection du filtre depuis l'extérieur (ex:
     * Topbar)
     * sans déclencher d'action utilisateur redondante.
     */
    public void setSelectedFilter(String filterId) {
        if (this.filterGroup != null) {
            // Appelle la méthode de sélection programmée de votre SwitchGroup
            this.filterGroup.setSelected(filterId);
        }
    }

    public void setOnSearchListener(Consumer<String> onSearchListener) {
        this.onSearchListener = onSearchListener;
    }

    public void setOnAddAbonneListener(Runnable onAddAbonneListener) {
        this.onAddAbonneListener = onAddAbonneListener;
    }

    public void setOnFilterChangedListener(Consumer<String> onFilterChangedListener) {
        this.onFilterChangedListener = onFilterChangedListener;
    }
}
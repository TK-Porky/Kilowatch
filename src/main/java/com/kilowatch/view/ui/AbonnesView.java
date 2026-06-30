package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton; // <-- Import ajouté
import com.kilowatch.view.component.SearchBar;
import com.kilowatch.view.component.Table;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.theme.AppIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import java.awt.*;

public class AbonnesView extends JPanel {

    public AbonnesView(TableModel tableModel) {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_DEEP);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        // --- 1. EN-TÊTE (TEXTES À GAUCHE + BOUTON À DROITE) ---
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);

        // 1.A Textes (Ouest)
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

        // 1.B Bouton Ajout (Est)
        JPanel rightButtonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtonContainer.setOpaque(false);

        AppButton addAbonneBtn = new AppButton("Nouvel Abonné", AppIcons.USER_PLUS, AppButton.Theme.PRIMARY);
        // addAbonneBtn.setPreferredSize(new Dimension(160, 42)); // Optionnel : si tu
        // veux forcer sa taille
        rightButtonContainer.add(addAbonneBtn);

        // Assemblage de l'en-tête
        headerWrapper.add(leftTextsPanel, BorderLayout.WEST);
        headerWrapper.add(rightButtonContainer, BorderLayout.EAST);

        // --- SearchBar ---
        SearchBar searchBar = new SearchBar("Recherche par ID, Nom, ou N° de Compteur... (F2)");

        // Conteneur pour gérer les espacements (Marges)
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setOpaque(false);
        searchContainer.setBorder(new EmptyBorder(24, 0, 24, 0));
        searchContainer.add(searchBar, BorderLayout.CENTER);

        // --- Ajout au panneau Nord ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(headerWrapper, BorderLayout.NORTH);
        northPanel.add(searchContainer, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // --- 2. INSTANCIATION DE NOTRE COMPOSANT TABLE ---
        Table table = new Table(tableModel);

        // --- 3. CONFIGURATION DES COLONNES SPÉCIFIQUES À CETTE VUE ---
        Table.BadgeCellRenderer categorieRenderer = new Table.BadgeCellRenderer();
        categorieRenderer.registerStyle("Social",
                new Table.BadgeStyle(new Color(59, 130, 246, 38), Color.decode("#3b82f6")));
        categorieRenderer.registerStyle("Résidentiel",
                new Table.BadgeStyle(AppColors.AMBER_SOFT, AppColors.ACCENT_AMBER));
        categorieRenderer.registerStyle("Industriel",
                new Table.BadgeStyle(new Color(168, 85, 247, 38), Color.decode("#a855f7")));
        table.getColumnModel().getColumn(3).setCellRenderer(categorieRenderer);

        Table.BadgeCellRenderer statutRenderer = new Table.BadgeCellRenderer();
        statutRenderer.registerStyle("Payée", new Table.BadgeStyle(AppColors.GREEN_SOFT, AppColors.STATUS_GREEN));
        statutRenderer.registerStyle("Impayée", new Table.BadgeStyle(AppColors.RED_SOFT, AppColors.STATUS_RED));
        table.getColumnModel().getColumn(6).setCellRenderer(statutRenderer);

        // --- 4. AJOUT AU PANEL VIA LA MÉTHODE ENCAPSULÉE ---
        add(table.createRoundedContainer(), BorderLayout.CENTER);
    }
}
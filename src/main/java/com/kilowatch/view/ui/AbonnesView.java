package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton;
import com.kilowatch.view.component.SearchBar;
import com.kilowatch.view.component.Table;
import com.kilowatch.view.data.ViewEnum.CategorieAbonne; // <-- Import de l'Enum
import com.kilowatch.view.data.ViewEnum.StatutFacture; // <-- Import de l'Enum
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
        
        // --- CONNEXION DU BOUTON AU CALLBACK ---
        addAbonneBtn.addActionListener(e -> {
            if (onAddAbonneListener != null) {
                onAddAbonneListener.run();
            }
        });
        
        rightButtonContainer.add(addAbonneBtn);

        // Assemblage de l'en-tête
        headerWrapper.add(leftTextsPanel, BorderLayout.WEST);
        headerWrapper.add(rightButtonContainer, BorderLayout.EAST);

        // --- 3. BARRE DE RECHERCHE ---
        SearchBar searchBar = new SearchBar("Recherche par ID, Nom, ou N° de Compteur... (F2)");
        
        // --- CONNEXION DE LA RECHERCHE AU CALLBACK ---
        searchBar.setOnSearchListener(query -> {
            if (onSearchListener != null) {
                onSearchListener.accept(query);
            }
        });

        // Conteneur pour gérer les espacements (Marges)
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setOpaque(false);
        searchContainer.setBorder(new EmptyBorder(24, 0, 24, 0));
        searchContainer.add(searchBar, BorderLayout.CENTER);

        // --- 4. AJOUT AU PANNEAU NORD ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(headerWrapper, BorderLayout.NORTH);
        northPanel.add(searchContainer, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // --- 5. INSTANCIATION DE NOTRE COMPOSANT TABLE ---
        Table table = new Table(tableModel);

        // --- 6. CONFIGURATION DES COLONNES SPÉCIFIQUES À CETTE VUE ---
        Table.BadgeCellRenderer categorieRenderer = new Table.BadgeCellRenderer();
        
        // Utilisation de l'Enum CategorieAbonne !
        categorieRenderer.registerStyle(CategorieAbonne.SOCIAL.getLibelle(),
                new Table.BadgeStyle(new Color(59, 130, 246, 38), Color.decode("#3b82f6")));
        categorieRenderer.registerStyle(CategorieAbonne.RESIDENTIEL.getLibelle(),
                new Table.BadgeStyle(AppColors.AMBER_SOFT, AppColors.ACCENT_AMBER));
        categorieRenderer.registerStyle(CategorieAbonne.INDUSTRIEL.getLibelle(),
                new Table.BadgeStyle(new Color(168, 85, 247, 38), Color.decode("#a855f7")));
        
        table.getColumnModel().getColumn(3).setCellRenderer(categorieRenderer);

        Table.BadgeCellRenderer statutRenderer = new Table.BadgeCellRenderer();
        
        // Utilisation de l'Enum StatutFacture !
        statutRenderer.registerStyle(StatutFacture.PAYEE.getLibelle(), 
                new Table.BadgeStyle(AppColors.GREEN_SOFT, AppColors.STATUS_GREEN));
        statutRenderer.registerStyle(StatutFacture.IMPAYEE.getLibelle(), 
                new Table.BadgeStyle(AppColors.RED_SOFT, AppColors.STATUS_RED));
        // Bonus : Ajout du style pour le statut "En Attente" (gris neutre)
        statutRenderer.registerStyle(StatutFacture.EN_ATTENTE.getLibelle(), 
                new Table.BadgeStyle(AppColors.BG_SURFACE_2, AppColors.TEXT_SECONDARY));
        
        table.getColumnModel().getColumn(6).setCellRenderer(statutRenderer);

        // --- 7. AJOUT AU PANEL VIA LA MÉTHODE ENCAPSULÉE ---
        add(table.createRoundedContainer(), BorderLayout.CENTER);
    }

    // --- 8. MÉTHODES PUBLIQUES POUR LE MAINLAYOUT ---
    public void setOnSearchListener(Consumer<String> onSearchListener) {
        this.onSearchListener = onSearchListener;
    }

    public void setOnAddAbonneListener(Runnable onAddAbonneListener) {
        this.onAddAbonneListener = onAddAbonneListener;
    }
}
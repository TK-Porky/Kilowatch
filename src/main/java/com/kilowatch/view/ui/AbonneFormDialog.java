package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton;
import com.kilowatch.view.component.AppInputField;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.data.ViewDto.Abonne;
import com.kilowatch.view.data.ViewEnum.CategorieAbonne;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

public class AbonneFormDialog extends JDialog {

    private AppInputField nomField;
    private AppInputField compteurField;
    private JComboBox<String> categorieBox;
    private AppInputField indexField;

    private Consumer<Abonne> onSaveListener;

    public AbonneFormDialog(Frame owner) {
        super(owner, "Nouvel Abonné", true); // true = modal (bloque la fenêtre principale)

        setSize(450, 550);
        setLocationRelativeTo(owner);
        setUndecorated(true); // Enlève la barre de titre classique de Windows/Mac pour un look moderne

        // --- PANNEAU PRINCIPAL AVEC BORDURE ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(AppColors.BG_SURFACE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.BORDER_SOFT, 1), // Petite bordure
                new EmptyBorder(32, 32, 32, 32) // Marges internes
        ));

        // --- 1. EN-TÊTE DU POPUP ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel titleLabel = new JLabel("Ajouter un abonné");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Remplissez les informations du nouveau contrat.");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(AppColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);

        // --- 2. FORMULAIRE (GRIDBAGLAYOUT POUR ALIGNER LES CHAMPS) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Champs de saisie
        nomField = new AppInputField("Nom complet", AppInputField.Variant.SEARCH);
        compteurField = new AppInputField("N° de Compteur (ex: CMP-20099)", AppInputField.Variant.SEARCH);
        indexField = new AppInputField("Index de départ (kWh)", AppInputField.Variant.BLOCK_NUMBER);

        // ComboBox personnalisé pour la catégorie
        JComboBox<CategorieAbonne> categorieBox = new JComboBox<>(CategorieAbonne.values());
        categorieBox.setFont(new Font("Inter", Font.PLAIN, 14));
        categorieBox.setBackground(AppColors.BG_SURFACE_2);
        categorieBox.setForeground(AppColors.TEXT_PRIMARY);
        categorieBox.setPreferredSize(new Dimension(0, 42));

        // Ajout des champs avec espacement
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(nomField, gbc);
        gbc.gridy = 1;
        formPanel.add(compteurField, gbc);

        // Label pour la combo
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        JLabel catLabel = new JLabel("Catégorie tarifaire :");
        catLabel.setForeground(AppColors.TEXT_DIM);
        catLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        formPanel.add(catLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(categorieBox, gbc);
        gbc.gridy = 4;
        formPanel.add(indexField, gbc);

        // --- 3. BOUTONS D'ACTION (BAS) ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(32, 0, 0, 0));

        AppButton cancelBtn = new AppButton("Annuler", AppButton.Theme.GHOST);
        AppButton saveBtn = new AppButton("Enregistrer", AppButton.Theme.PRIMARY);

        cancelBtn.addActionListener(e -> dispose()); // Ferme la fenêtre
        saveBtn.addActionListener(e -> validerFormulaire());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        // --- ASSEMBLAGE ---
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void validerFormulaire() {
        // Validation basique
        String nom = nomField.getText();
        String compteur = compteurField.getText();
        String cat = (String) categorieBox.getSelectedItem();
        String indexStr = indexField.getText();

        if (nom.isEmpty() || compteur.isEmpty() || indexStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int index = Integer.parseInt(indexStr);
            // Si tout est bon, on crée un objet Abonne temporaire avec ID généré et statut
            // par défaut
            Abonne nouvelAbonne = new Abonne(
                    "AB-" + (int) (Math.random() * 10000), // Fake ID
                    nom,
                    compteur,
                    cat,
                    index,
                    "En attente");

            // On déclenche le callback !
            if (onSaveListener != null) {
                onSaveListener.accept(nouvelAbonne);
            }
            dispose(); // On ferme le popup

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "L'index doit être un nombre valide.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setOnSaveListener(Consumer<Abonne> onSaveListener) {
        this.onSaveListener = onSaveListener;
    }
}
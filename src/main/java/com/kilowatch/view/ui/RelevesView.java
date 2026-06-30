package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton;
import com.kilowatch.view.component.AppInputField;
import com.kilowatch.view.component.Table;
import com.kilowatch.view.theme.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class RelevesView extends JPanel {

    private Consumer<String> onSearchListener;
    private Consumer<Integer> onValidateIndexListener;
    private Runnable onChangeSubscriberListener;

    private RelevesHeader headerPanel;
    private RelevesFormPanel formPanel;
    private RelevesHistoryPanel historyPanel;

    public RelevesView() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_DEEP);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        headerPanel = new RelevesHeader();
        formPanel = new RelevesFormPanel();
        historyPanel = new RelevesHistoryPanel();

        // =========================================================
        // ARCHITECTURE DU LAYOUT PRINCIPAL (BARRE LATERALE STABLE)
        // =========================================================
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        // 1. Panneau de Saisie (Gauche) : Taille fixe protectrice, ancré en haut
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0; // Ne s'étire pas horizontalement au-delà de sa taille
        gbc.weighty = 1.0; // Aligné sur l'axe vertical
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH; // Empêche l'étirement vertical
        gbc.insets = new Insets(0, 0, 0, 24); // Marge entre les deux cartes

        // Sécurité anti-écrasement
        formPanel.setPreferredSize(new Dimension(360, 390));
        formPanel.setMinimumSize(new Dimension(340, 390));
        mainContent.add(formPanel, gbc);

        // 2. Panneau Historique (Droite) : S'accapare tout l'espace restant
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Consomme 100% de la largeur disponible restante
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainContent.add(historyPanel, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        setupInternalForwarding();
    }

    public void setOnSearchListener(Consumer<String> l) {
        this.onSearchListener = l;
    }

    public void setOnValidateIndexListener(Consumer<Integer> l) {
        this.onValidateIndexListener = l;
    }

    public void setOnChangeSubscriberListener(Runnable l) {
        this.onChangeSubscriberListener = l;
    }

    public void setSubscriberFoundState(String idCompteur, String nomAbonne, String cat, int ancienIdx) {
        formPanel.switchToIndexState(idCompteur, nomAbonne, cat, ancienIdx);
    }

    public void resetToSearchState() {
        formPanel.switchToSearchState();
    }

    public void updateSessionTable(DefaultTableModel m, int t) {
        historyPanel.updateTableData(m, t);
    }

    private void setupInternalForwarding() {
        formPanel.setSearchCallback(q -> {
            if (onSearchListener != null)
                onSearchListener.accept(q);
        });
        formPanel.setValidationCallback(i -> {
            if (onValidateIndexListener != null)
                onValidateIndexListener.accept(i);
        });
        formPanel.setChangeCallback(() -> {
            if (onChangeSubscriberListener != null)
                onChangeSubscriberListener.run();
        });
    }

    // =========================================================
    // EN-TÊTE DE LA VUE
    // =========================================================
    private static class RelevesHeader extends JPanel {
        public RelevesHeader() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 28, 0));

            JPanel leftTexts = new JPanel();
            leftTexts.setLayout(new BoxLayout(leftTexts, BoxLayout.Y_AXIS));
            leftTexts.setOpaque(false);

            JLabel title = new JLabel("Saisie des relevés");
            title.setFont(new Font("Inter", Font.BOLD, 26));
            title.setForeground(AppColors.TEXT_PRIMARY);

            JLabel subtitle = new JLabel("Optimisé clavier — scannez ou tapez le compteur, validez avec Entrée");
            subtitle.setFont(new Font("Inter", Font.PLAIN, 14));
            subtitle.setForeground(AppColors.TEXT_SECONDARY);

            leftTexts.add(title);
            leftTexts.add(Box.createVerticalStrut(6));
            leftTexts.add(subtitle);

            JPanel periodContainer = new JPanel();
            periodContainer.setLayout(new BoxLayout(periodContainer, BoxLayout.Y_AXIS));
            periodContainer.setOpaque(false);

            JLabel pLabel = new JLabel("PÉRIODE DE FACTURATION");
            pLabel.setFont(new Font("Inter", Font.BOLD, 11));
            pLabel.setForeground(AppColors.TEXT_DIM);
            pLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

            JComboBox<String> pCombo = new JComboBox<>(new String[] { "Juin 2026", "Mai 2026" });
            pCombo.setFont(new Font("Inter", Font.PLAIN, 14));
            pCombo.setBackground(AppColors.BG_SURFACE_2);
            pCombo.setForeground(AppColors.TEXT_PRIMARY);
            pCombo.setPreferredSize(new Dimension(150, 36));
            pCombo.setMaximumSize(new Dimension(150, 36));
            pCombo.setAlignmentX(Component.RIGHT_ALIGNMENT);

            periodContainer.add(pLabel);
            periodContainer.add(Box.createVerticalStrut(6));
            periodContainer.add(pCombo);

            add(leftTexts, BorderLayout.WEST);
            add(periodContainer, BorderLayout.EAST);
        }
    }

    // =========================================================
    // CARTE FORMULAIRE (SÉCURISÉE CONTRE LES CHEVAUCHEMENTS)
    // =========================================================
    private static class RelevesFormPanel extends JPanel {
        private AppInputField searchField;
        private AppInputField ancienIndexField;
        private AppInputField nouvelIndexField;

        private JPanel abonneInfoRow;
        private JLabel abonneNameLabel;
        private JLabel abonneCatBadge;
        private AppButton changerAbonneBtn;

        private JPanel consoBar;
        private JLabel consoTitle, consoValue;
        private AppButton actionBtn;

        private boolean isInIndexState = false;
        private int currentAncienIndex = 0;

        private Consumer<String> searchCallback;
        private Consumer<Integer> validationCallback;
        private Runnable changeCallback;

        public RelevesFormPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(24, 24, 24, 24));

            // L'arme absolue anti-chevauchement : le GridBagLayout vertical
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.weightx = 1.0;

            // -- CHAMP RECHERCHE --
            searchField = new AppInputField("N° COMPTEUR OU ID ABONNÉ", AppInputField.Variant.SEARCH);
            searchField.setText("CMP-20007");

            c.gridy = 0;
            c.insets = new Insets(0, 0, 16, 0);
            add(searchField, c);

            // -- INFO ABONNÉ CONFIGUREE AVEC ARRIÈRE-PLAN ARRONDI ET MARGES --
            abonneInfoRow = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Couleur d'arrière-plan de la ligne d'information (ici une nuance légèrement
                    // plus claire ou sombre)
                    g2.setColor(AppColors.PURPLE_SOFT);

                    // Dessine le rectangle arrondi sur toute la surface de la ligne
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            abonneInfoRow.setOpaque(false); // Indispensable pour laisser voir les arrondis du paintComponent

            // AJOUT DES MARGES INTERNES : 10px en haut/bas et 12px à gauche/droite
            abonneInfoRow.setBorder(new EmptyBorder(10, 12, 10, 12));
            abonneInfoRow.setOpaque(false);
            abonneInfoRow.setVisible(false);

            JPanel abonneLeftMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            abonneLeftMeta.setOpaque(false);
            abonneNameLabel = new JLabel("");
            abonneNameLabel.setFont(new Font("Inter", Font.BOLD, 14));
            abonneNameLabel.setForeground(AppColors.TEXT_PRIMARY);

            abonneCatBadge = new JLabel("") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppColors.AMBER_SOFT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    super.paintComponent(g2);
                    g2.dispose();
                }
            };
            abonneCatBadge.setFont(new Font("Inter", Font.PLAIN, 12));
            abonneCatBadge.setForeground(AppColors.ACCENT_AMBER);
            abonneCatBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
            abonneLeftMeta.add(abonneNameLabel);
            abonneLeftMeta.add(abonneCatBadge);

            changerAbonneBtn = new AppButton("changer", AppButton.Theme.GHOST);
            changerAbonneBtn.setFont(new Font("Inter", Font.PLAIN, 13));
            changerAbonneBtn.setForeground(AppColors.ACCENT_AMBER);
            changerAbonneBtn.setPadding(0, 0, 0, 0);

            abonneInfoRow.add(abonneLeftMeta, BorderLayout.WEST);
            abonneInfoRow.add(changerAbonneBtn, BorderLayout.EAST);

            c.gridy = 1;
            c.insets = new Insets(0, 0, 16, 0);
            add(abonneInfoRow, c);

            // -- LES BLOCS INDEX --
            JPanel indexesRow = new JPanel(new GridLayout(1, 2, 16, 0));
            indexesRow.setOpaque(false);
            ancienIndexField = new AppInputField("ANCIEN INDEX", AppInputField.Variant.BLOCK_READONLY);
            nouvelIndexField = new AppInputField("NOUVEL INDEX", AppInputField.Variant.BLOCK_NUMBER);
            indexesRow.add(ancienIndexField);
            indexesRow.add(nouvelIndexField);

            c.gridy = 2;
            c.insets = new Insets(0, 0, 18, 0);
            add(indexesRow, c);

            // -- BARRE DE CONSOMMATION CALCULÉE --
            consoBar = new JPanel(new BorderLayout());
            consoBar.setOpaque(false);
            consoBar.setBackground(AppColors.BG_SURFACE_2);
            consoBar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(AppColors.BORDER_SOFT, 1),
                    new EmptyBorder(12, 14, 12, 14)));

            consoTitle = new JLabel("Consommation calculée");
            consoTitle.setFont(new Font("Inter", Font.PLAIN, 14));
            consoTitle.setForeground(AppColors.TEXT_SECONDARY);
            consoValue = new JLabel("—");
            consoValue.setFont(new Font("Inter", Font.BOLD, 14));
            consoValue.setForeground(AppColors.TEXT_PRIMARY);
            consoBar.add(consoTitle, BorderLayout.WEST);
            consoBar.add(consoValue, BorderLayout.EAST);

            c.gridy = 3;
            c.insets = new Insets(0, 0, 20, 0);
            add(consoBar, c);

            // -- BOUTON D'ACTION PRINCIPAL (Pleine largeur garantie)
            actionBtn = new AppButton("Rechercher (Entrée)", AppButton.Theme.SECONDARY);
            actionBtn.setPreferredSize(new Dimension(actionBtn.getPreferredSize().width, 46));

            JPanel btnWrapper = new JPanel(new BorderLayout());
            btnWrapper.setOpaque(false);
            btnWrapper.add(actionBtn, BorderLayout.CENTER);

            c.gridy = 4;
            c.insets = new Insets(0, 0, 0, 0);
            add(btnWrapper, c);

            setupListeners();
        }

        public void switchToIndexState(String code, String nom, String cat, int ancienIdx) {
            this.isInIndexState = true;
            this.currentAncienIndex = ancienIdx;
            searchField.setText(code);
            searchField.setInputFieldEnabled(false);
            abonneNameLabel.setText(nom);
            abonneCatBadge.setText(cat);
            abonneInfoRow.setVisible(true);
            ancienIndexField.setText(String.format("%,d", ancienIdx).replace(',', ' '));
            nouvelIndexField.setText("");
            nouvelIndexField.setInputFieldEnabled(true);
            nouvelIndexField.requestFocusInWindow();
            actionBtn.setText("Valider l'index (Entrée)");
            actionBtn.setTheme(AppButton.Theme.PRIMARY);
            revalidate();
            repaint();
        }

        public void switchToSearchState() {
            this.isInIndexState = false;
            searchField.setText("");
            searchField.setInputFieldEnabled(true);
            abonneInfoRow.setVisible(false);
            ancienIndexField.setText("—");
            nouvelIndexField.setText("0");
            nouvelIndexField.setInputFieldEnabled(false);
            consoBar.setBackground(AppColors.BG_SURFACE_2);
            consoTitle.setForeground(AppColors.TEXT_SECONDARY);
            consoValue.setText("—");
            consoValue.setForeground(AppColors.TEXT_PRIMARY);
            actionBtn.setText("Rechercher (Entrée)");
            actionBtn.setTheme(AppButton.Theme.SECONDARY);
            searchField.requestFocusInWindow();
            revalidate();
            repaint();
        }

        private void setupListeners() {
            searchField.addActionListener(e -> triggerSearch());
            actionBtn.addActionListener(e -> {
                if (!isInIndexState)
                    triggerSearch();
                else
                    triggerValidation();
            });
            changerAbonneBtn.addActionListener(e -> {
                if (changeCallback != null)
                    changeCallback.run();
                switchToSearchState();
            });
            nouvelIndexField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    recalculateConso();
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        triggerValidation();
                }
            });
        }

        private void recalculateConso() {
            try {
                int newIdx = Integer.parseInt(nouvelIndexField.getText());
                int diff = newIdx - currentAncienIndex;
                if (diff < 0) {
                    consoBar.setBackground(AppColors.RED_SOFT);
                    consoTitle.setForeground(AppColors.STATUS_RED);
                    consoValue.setText(diff + " kWh");
                    consoValue.setForeground(AppColors.STATUS_RED);
                } else {
                    consoBar.setBackground(AppColors.BG_SURFACE_2);
                    consoTitle.setForeground(AppColors.TEXT_SECONDARY);
                    consoValue.setText(diff + " kWh");
                    consoValue.setForeground(AppColors.TEXT_PRIMARY);
                }
            } catch (NumberFormatException ex) {
                consoValue.setText("—");
            }

            this.revalidate();
            this.repaint();
        }

        private void triggerSearch() {
            String q = searchField.getText();
            if (!q.isEmpty() && searchCallback != null)
                searchCallback.accept(q);
        }

        private void triggerValidation() {
            try {
                int idx = Integer.parseInt(nouvelIndexField.getText());
                if (validationCallback != null)
                    validationCallback.accept(idx);
            } catch (NumberFormatException ignored) {
            }
        }

        public void setSearchCallback(Consumer<String> cb) {
            this.searchCallback = cb;
        }

        public void setValidationCallback(Consumer<Integer> cb) {
            this.validationCallback = cb;
        }

        public void setChangeCallback(Runnable cb) {
            this.changeCallback = cb;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColors.BG_SURFACE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(AppColors.BORDER_SOFT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
        }
    }

    // =========================================================
    // CARTE HISTORIQUE (DROITE)
    // =========================================================
    private static class RelevesHistoryPanel extends JPanel {
        private JLabel countLabel;
        private JPanel centerViewContainer;
        private Table table;
        private JPanel placeholder;

        public RelevesHistoryPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(24, 24, 24, 24));
            setLayout(new BorderLayout());

            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            head.setBorder(new EmptyBorder(0, 0, 16, 0));

            JLabel title = new JLabel("Relevés de la session courante");
            title.setFont(new Font("Inter", Font.BOLD, 16));
            title.setForeground(AppColors.TEXT_PRIMARY);

            countLabel = new JLabel("0 saisie(s)");
            countLabel.setFont(new Font("Inter", Font.PLAIN, 13));
            countLabel.setForeground(AppColors.TEXT_DIM);

            head.add(title, BorderLayout.WEST);
            head.add(countLabel, BorderLayout.EAST);
            add(head, BorderLayout.NORTH);

            centerViewContainer = new JPanel(new CardLayout());
            centerViewContainer.setOpaque(false);

            placeholder = new JPanel(new GridBagLayout());
            placeholder.setOpaque(false);
            JLabel phLabel = new JLabel("Aucun relevé saisi pour le moment dans cette session.");
            phLabel.setFont(new Font("Inter", Font.PLAIN, 14));
            phLabel.setForeground(AppColors.TEXT_DIM);
            placeholder.add(phLabel);

            centerViewContainer.add(placeholder, "PLACEHOLDER");
            add(centerViewContainer, BorderLayout.CENTER);
        }

        public void updateTableData(DefaultTableModel model, int totalSaisies) {
            countLabel.setText(totalSaisies + " saisie(s)");
            centerViewContainer.removeAll();

            if (totalSaisies == 0) {
                centerViewContainer.add(placeholder, "PLACEHOLDER");
            } else {
                table = new Table(model);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if ("CONSO (KWH)".equalsIgnoreCase(table.getColumnName(i))) {
                        table.setColumnAlignment(i, SwingConstants.RIGHT);
                    }
                }
                centerViewContainer.add(table.createRoundedContainer(), "TABLE");
            }
            centerViewContainer.revalidate();
            centerViewContainer.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColors.BG_SURFACE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(AppColors.BORDER_SOFT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
        }
    }
}
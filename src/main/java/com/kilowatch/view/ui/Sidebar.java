package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppButton;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.theme.AppIcons;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Sidebar extends JPanel {

    private final Consumer<String> onMenuSelected;
    private final List<JButton> menuButtons = new ArrayList<>();

    public Sidebar(Consumer<String> onMenuSelected) {
        this.onMenuSelected = onMenuSelected;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));
        setBackground(AppColors.BG_SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppColors.BORDER_SOFT));

        add(createBrandHeader(), BorderLayout.NORTH);
        add(createNavigation(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        // Par défaut, on peut activer le tableau de bord au démarrage
        setActiveMenu("VIEW_DASHBOARD");
    }

    /**
     * Permet de mettre à jour visuellement le bouton actif de la sidebar
     * depuis un appel externe (ex: MainLayout).
     */
    public void setActiveMenu(String viewId) {
        for (JButton btn : menuButtons) {
            String btnViewId = (String) btn.getClientProperty("viewId");
            boolean isActive = viewId.equals(btnViewId);
            applyButtonStyle(btn, isActive);
        }
    }

    private JPanel createNavigation() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(new EmptyBorder(0, 12, 0, 12));

        navPanel.add(createMenuButton("Tableau de bord", "VIEW_DASHBOARD", AppIcons.LAYOUT_DASHBOARD));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createMenuButton("Abonnés", "VIEW_ABONNES", AppIcons.USERS));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createMenuButton("Relevés", "VIEW_RELEVES", AppIcons.GAUGE));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createMenuButton("Caisse & Facturation", "VIEW_CAISSE", AppIcons.BANKNOTE_CHECK));

        return navPanel;
    }

    private JButton createMenuButton(String text, String viewId, AppIcons iconEnum) {
        JButton btn = new JButton(text, iconEnum.get(18, AppColors.TEXT_SECONDARY));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // CRUCIAL : On attache l'ID de la vue au bouton pour pouvoir le retrouver plus
        // tard
        btn.putClientProperty("viewId", viewId);

        applyButtonStyle(btn, false);

        btn.addActionListener(e -> {
            // Met à jour visuellement la sélection en local
            setActiveMenu(viewId);
            // Déclenche la navigation CardLayout
            onMenuSelected.accept(viewId);
        });

        menuButtons.add(btn);
        return btn;
    }

    private void applyButtonStyle(JButton btn, boolean active) {
        String bgColor = active ? "#242e3c" : toHex(getBackground());
        String fgColor = active ? "#ffffff" : toHex(AppColors.TEXT_SECONDARY);

        String btnStyle = "background: " + bgColor + "; " +
                "foreground: " + fgColor + "; " +
                "borderWidth: 0; " +
                "focusWidth: 0; " +
                "margin: 8,12,8,12; " +
                "hoverBackground: " + toHex(AppColors.BG_SURFACE) + "; " +
                "hoverForeground: " + toHex(AppColors.TEXT_PRIMARY) + "; " +
                "font: 13";

        btn.putClientProperty("FlatLaf.style", btnStyle);
        btn.repaint();
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 16, 16, 16));

        JButton btnLogout = new AppButton("Déconnexion", AppIcons.LOG_OUT, AppButton.Theme.DANGER);
        btnLogout.setPreferredSize(new Dimension(0, 40));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footer.add(btnLogout, BorderLayout.CENTER);
        footer.setPreferredSize(new Dimension(220, 70));

        return footer;
    }

    private JPanel createBrandHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel brandIcon = new JLabel("⚡");
        brandIcon.setForeground(AppColors.ACCENT_AMBER);
        brandIcon.setFont(brandIcon.getFont().deriveFont(Font.BOLD, 16f));

        JLabel brandName = new JLabel(" ENEO Guichet");
        brandName.setForeground(AppColors.TEXT_PRIMARY);
        brandName.setFont(brandName.getFont().deriveFont(Font.BOLD, 14f));

        panel.add(brandIcon);
        panel.add(brandName);
        return panel;
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
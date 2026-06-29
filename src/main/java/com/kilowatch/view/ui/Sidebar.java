package com.kilowatch.view.ui;

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
    }

    private JPanel createNavigation() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(new EmptyBorder(0, 12, 0, 12));

        // Utilisation de l'Enum AppIcons (automatique et robuste)
        navPanel.add(createMenuButton("Tableau de bord", "VIEW_DASHBOARD", AppIcons.LAYOUT_DASHBOARD));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createMenuButton("Abonnés", "VIEW_ABONNES", AppIcons.USERS));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createMenuButton("Relevés", "VIEW_RELEVES", AppIcons.GAUGE));
        navPanel.add(createMenuButton("Caisse & Facturation", "VIEW_CAISSE", AppIcons.BANKNOTE_CHECK));

        return navPanel;
    }

    private JButton createMenuButton(String text, String viewId, AppIcons iconEnum) {
        // Appelle l'Enum pour générer l'icône à la volée
        JButton btn = new JButton(text, iconEnum.get(18, AppColors.TEXT_SECONDARY));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        applyButtonStyle(btn, false);

        btn.addActionListener(e -> {
            menuButtons.forEach(b -> applyButtonStyle(b, false));
            applyButtonStyle(btn, true);
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
                "margin: 8,12,8,12; " + // Marges internes du bouton
                "hoverBackground: " + toHex(AppColors.BG_SURFACE) + "; " +
                "hoverForeground: " + toHex(AppColors.TEXT_PRIMARY) + "; " +
                "font: 13";

        btn.putClientProperty("FlatLaf.style", btnStyle);
        btn.repaint();
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 16, 16, 16)); // Padding suffisant

        JButton btnLogout = new JButton("Déconnexion", AppIcons.LOG_OUT.get(16, AppColors.STATUS_RED));

        // 1. Force une hauteur fixe pour le bouton
        btnLogout.setPreferredSize(new Dimension(0, 40));

        // 2. Utilisation de la propriété FlatLaf pour le style (sans arc si géré
        // globalement)
        btnLogout.putClientProperty("FlatLaf.style",
                "background: " + toHex(getBackground()) + "; " +
                        "foreground: #e2543d; " +
                        "borderWidth: 1; " +
                        "borderColor: #e2543d; " +
                        "margin: 0,10,0,10;");

        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Ajouter le bouton au centre du footer
        footer.add(btnLogout, BorderLayout.CENTER);

        // 4. Force la taille du footer pour qu'il ne soit pas "écrasé" par le reste de
        // la sidebar
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
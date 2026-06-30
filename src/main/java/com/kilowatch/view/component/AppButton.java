package com.kilowatch.view.component;

import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.theme.AppIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AppButton extends JButton {

    // =========================================================
    // ÉNUMÉRATION DES THÈMES DE BOUTON
    // =========================================================
    public enum Theme {
        PRIMARY, // Fond Ambre, Texte Sombre
        SECONDARY, // Fond Surface, Texte Clair
        DANGER, // Fond Rouge, Texte Blanc
        GHOST // Fond Transparent, Texte Gris
    }

    private Theme theme;
    private boolean isHovered = false;
    private int radius = 12;

    // Gestion de l'icône
    private AppIcons iconEnum;
    private int iconSize = 18;

    // =========================================================
    // CONSTRUCTEURS
    // =========================================================

    // -- CONSTRUCTEUR : TEXTE UNIQUEMENT --
    public AppButton(String text, Theme theme) {
        super(text);
        this.theme = theme;
        initStyle();
        setupMouseListener();
    }

    // -- CONSTRUCTEUR : TEXTE ET ICÔNE --
    public AppButton(String text, AppIcons iconEnum, Theme theme) {
        super(text);
        this.theme = theme;
        this.iconEnum = iconEnum;
        initStyle();
        setupMouseListener();
    }

    // -- CONSTRUCTEUR : ICÔNE UNIQUEMENT --
    public AppButton(AppIcons iconEnum, Theme theme) {
        super("");
        this.theme = theme;
        this.iconEnum = iconEnum;
        initStyle();
        setupMouseListener();
    }

    // =========================================================
    // CONFIGURATION INITIALE
    // =========================================================
    private void initStyle() {
        setFont(new Font("Inter", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);

        // Paramètres de marges par défaut
        setPadding(10, 24, 10, 24);
        setIconTextGap(8);

        updateColors();
    }

    // =========================================================
    // GESTION DES COULEURS ET DE L'ÉTAT
    // =========================================================
    private void updateColors() {
        Color fgColor;

        if (!isEnabled()) {
            fgColor = AppColors.TEXT_SECONDARY;
        } else {
            fgColor = switch (theme) {
                case PRIMARY -> AppColors.BG_DEEP;
                case SECONDARY -> AppColors.TEXT_PRIMARY;
                case DANGER -> Color.WHITE;
                case GHOST -> isHovered ? AppColors.TEXT_PRIMARY : AppColors.TEXT_SECONDARY;
            };
        }

        // Application de la couleur au texte
        setForeground(fgColor);

        // Application et rafraîchissement immédiat de l'icône
        if (iconEnum != null) {
            setIcon(iconEnum.get(iconSize, fgColor));
        }
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        updateColors();
        setCursor(b ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
        repaint();
    }

    // =========================================================
    // PERSONNALISATION PUBLIQUE
    // =========================================================
    public void setPadding(int top, int left, int bottom, int right) {
        setBorder(new EmptyBorder(top, left, bottom, right));
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        updateColors();
        repaint();
    }

    // -- CORRECTION : MET À JOUR ET RECALCULE L'ICÔNE IMMÉDIATEMENT --
    public void setIconEnum(AppIcons iconEnum) {
        this.iconEnum = iconEnum;
        updateColors();
        repaint();
    }

    public void setIconSize(int size) {
        this.iconSize = size;
        updateColors();
        repaint();
    }

    // =========================================================
    // LOGIQUE DE DESSIN
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bgColor = getBackgroundColor();

        if (bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }

        super.paintComponent(g);
        g2.dispose();
    }

    // -- CORRECTION : RETOUR À DES COULEURS DE SURVOL SOLIDES ET SUBTILES --
    private Color getBackgroundColor() {
        if (!isEnabled()) {
            return (theme == Theme.GHOST) ? new Color(0, 0, 0, 0) : AppColors.BG_SURFACE_2;
        }

        return switch (theme) {
            // Un ambre un poil plus clair au survol
            case PRIMARY -> isHovered ? Color.decode("#f0b35a") : AppColors.ACCENT_AMBER;
            // Utilisation de BG_ELEVATED (plus clair) au lieu de BG_SURFACE (plus sombre)
            case SECONDARY -> isHovered ? AppColors.BG_ELEVATED : AppColors.BG_SURFACE_2;
            // Un rouge un poil plus clair au survol
            case DANGER -> isHovered ? Color.decode("#e86853") : AppColors.STATUS_RED;
            // Un fond très léger pour le mode Ghost au survol
            case GHOST -> isHovered ? AppColors.BG_SURFACE : new Color(0, 0, 0, 0);
        };
    }

    // =========================================================
    // ÉVÉNEMENTS SOURIS (Hover)
    // =========================================================
    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    updateColors();
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = false;
                    updateColors();
                    repaint();
                }
            }
        });
    }
}
package com.kilowatch.view.component;

import com.kilowatch.view.theme.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SwitchGroup extends JPanel {

    private final List<SwitchItem> items = new ArrayList<>();
    private Consumer<String> onSwitchListener;

    public SwitchGroup() {
        // Un espace très léger entre les boutons (2px) et pas d'espace vertical
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setOpaque(false); // Important pour dessiner le fond arrondi custom
        // La marge interne (padding) du conteneur
        setBorder(new EmptyBorder(4, 4, 4, 4));
    }

    /**
     * Dessine l'arrière-plan global du conteneur de filtres (SURFACE_2)
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Couleur de fond du bloc
        g2.setColor(AppColors.BG_SURFACE_2);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // Bords arrondis du conteneur

        super.paintComponent(g);
        g2.dispose();
    }

    /**
     * Définit l'action à exécuter lorsqu'un switch est cliqué.
     */
    public void setOnSwitchListener(Consumer<String> listener) {
        this.onSwitchListener = listener;
    }

    /**
     * Ajoute un nouvel onglet au switch.
     */
    public void addSwitch(String id, String text, boolean isDefault) {
        SwitchItem item = new SwitchItem(id, text);
        items.add(item);
        add(item);

        if (isDefault) {
            setActive(id, false);
        }

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!item.active) {
                    setActive(id, true);
                }
            }
        });
    }

    private void setActive(String id, boolean notify) {
        // Met à jour l'affichage de tous les items
        for (SwitchItem item : items) {
            item.setActive(item.id.equals(id));
        }

        // Notifie la vue que le switch a changé
        if (notify && onSwitchListener != null) {
            onSwitchListener.accept(id);
        }
    }

    // =========================================================
    // CLASSE INTERNE : L'élément visuel du Switch
    // =========================================================
    private class SwitchItem extends JPanel {
        String id;
        boolean active = false;
        JLabel label;

        public SwitchItem(String id, String text) {
            this.id = id;
            setOpaque(false);
            setLayout(new BorderLayout());
            // Padding interne du bouton (la "pilule")
            setBorder(new EmptyBorder(6, 16, 6, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            label = new JLabel(text);
            label.setFont(new Font("Inter", Font.BOLD, 13));
            label.setForeground(AppColors.TEXT_SECONDARY);
            add(label, BorderLayout.CENTER);
        }

        public void setActive(boolean active) {
            this.active = active;
            label.setForeground(active ? AppColors.BG_DEEP : AppColors.TEXT_SECONDARY);
            repaint(); // Force le redessin du fond
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dessine le fond ambre uniquement si l'élément est sélectionné
            if (active) {
                g2.setColor(AppColors.ACCENT_AMBER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); // Bords de la "pilule"
            }

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
package com.kilowatch.view.component;

import com.kilowatch.view.theme.AppColors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer; // <-- Import ajouté

public class SearchBar extends JPanel {

    private JTextField textField;
    private String placeholderText;

    // --- 1. DÉCLARATION DU CALLBACK ---
    private Consumer<String> onSearchListener;

    public SearchBar(String placeholderText) {
        this.placeholderText = placeholderText;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(0, 42));

        // --- Panneau pour l'icône Loupe ---
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(AppColors.TEXT_SECONDARY);
                g2.setStroke(new BasicStroke(1.5f));
                int x = 16, y = 14;
                g2.drawOval(x, y, 12, 12);
                g2.drawLine(x + 10, y + 10, x + 16, y + 16);

                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(48, 42));

        // --- Champ de texte avec gestion du Placeholder ---
        textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner() && getPlaceholderText() != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(AppColors.TEXT_SECONDARY);
                    FontMetrics fm = g2.getFontMetrics();
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                    g2.drawString(getPlaceholderText(), getInsets().left, y);
                    g2.dispose();
                }
            }
        };

        textField.setOpaque(false);
        textField.setBorder(new EmptyBorder(0, 0, 0, 16));
        textField.setFont(new Font("Inter", Font.PLAIN, 14));
        textField.setForeground(AppColors.TEXT_PRIMARY);
        textField.setCaretColor(AppColors.TEXT_PRIMARY);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.repaint();
            }
        });

        // --- 2. DÉCLENCHEMENT DU CALLBACK SUR LA TOUCHE "ENTRÉE" ---
        textField.addActionListener(e -> {
            if (onSearchListener != null) {
                onSearchListener.accept(textField.getText());
            }
        });

        // --- Assemblage ---
        add(iconPanel, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(AppColors.BG_SURFACE_2);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

        g2.setColor(AppColors.BORDER_SOFT);
        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

        g2.dispose();
    }

    // --- 3. SETTER POUR LE CALLBACK ---
    public void setOnSearchListener(Consumer<String> onSearchListener) {
        this.onSearchListener = onSearchListener;
    }

    // --- Getters et Setters existants ---
    public String getPlaceholderText() {
        return placeholderText;
    }

    public void setPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText;
        textField.repaint();
    }

    public String getText() {
        return textField.getText();
    }

    public JTextField getTextField() {
        return textField;
    }
}
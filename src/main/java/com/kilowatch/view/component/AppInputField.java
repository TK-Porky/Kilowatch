package com.kilowatch.view.component;

import com.kilowatch.view.theme.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;

public class AppInputField extends JPanel {

    public enum Variant {
        SEARCH,
        BLOCK_EDITABLE,
        BLOCK_READONLY,
        BLOCK_NUMBER // <-- NOUVELLE VARIANTE POUR L'INDEX
    }

    private final Variant variant;
    private final JTextField textField;
    private JLabel titleLabel;
    private JPanel innerContainer;

    public AppInputField(String title, Variant variant) {
        this.variant = variant;
        this.textField = new JTextField();

        setOpaque(false);
        setLayout(new BorderLayout(0, 6));

        if (title != null && !title.isEmpty()) {
            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Inter", Font.BOLD, variant == Variant.SEARCH ? 12 : 11));
            titleLabel.setForeground(variant == Variant.BLOCK_READONLY ? AppColors.TEXT_DIM : AppColors.TEXT_SECONDARY);
            add(titleLabel, BorderLayout.NORTH);
        }

        initTextField();
        initContainer();
        setupFocusBehavior();
    }

    private void initTextField() {
        textField.setBorder(null);
        textField.setOpaque(false);

        if (variant == Variant.SEARCH) {
            textField.setFont(new Font("Inter", Font.BOLD, 22));
            textField.setForeground(Color.decode("#111827"));
            textField.setCaretColor(Color.decode("#111827"));
        } else {
            textField.setFont(new Font("Inter", Font.BOLD, 18));
            textField.setForeground(
                    variant == Variant.BLOCK_READONLY ? AppColors.TEXT_SECONDARY : AppColors.TEXT_PRIMARY);
            textField.setCaretColor(AppColors.TEXT_PRIMARY);
            if (variant == Variant.BLOCK_READONLY) {
                textField.setEditable(false);
                textField.setFocusable(false);
            }
        }

        // --- FILTRE ANTI-LETTRES POUR LE SPINNER ---
        if (variant == Variant.BLOCK_NUMBER) {
            ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                        throws BadLocationException {
                    if (string.matches("\\d+"))
                        super.insertString(fb, offset, string, attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    if (text.matches("\\d+"))
                        super.replace(fb, offset, length, text, attrs);
                }
            });
        }
    }

    private void initContainer() {
        innerContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(variant == Variant.SEARCH ? Color.WHITE : AppColors.BG_SURFACE_2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), variant == Variant.SEARCH ? 12 : 8,
                        variant == Variant.SEARCH ? 12 : 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        innerContainer.setOpaque(false);

        int pTopBottom = (variant == Variant.SEARCH) ? 12 : 10;
        int pLeftRight = (variant == Variant.SEARCH) ? 16 : 14;
        innerContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(variant == Variant.SEARCH ? AppColors.ACCENT_AMBER : AppColors.BORDER_DEFAULT, 1),
                new EmptyBorder(pTopBottom, pLeftRight, pTopBottom, pLeftRight)));

        innerContainer.add(textField, BorderLayout.CENTER);

        // --- AJOUT DES CURSEURS ▲ ▼ POUR LA VARIANTE NUMBER ---
        if (variant == Variant.BLOCK_NUMBER) {
            JPanel spinnerControls = new JPanel(new GridLayout(2, 1, 0, 0));
            spinnerControls.setOpaque(false);

            JLabel upArrow = createArrowLabel("▲", 1);
            JLabel downArrow = createArrowLabel("▼", -1);

            spinnerControls.add(upArrow);
            spinnerControls.add(downArrow);
            innerContainer.add(spinnerControls, BorderLayout.EAST);
        }

        add(innerContainer, BorderLayout.CENTER);
    }

    // Fonction utilitaire pour créer et gérer les clics sur les flèches du spinner
    private JLabel createArrowLabel(String symbol, int step) {
        JLabel label = new JLabel(symbol, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 9));
        label.setForeground(AppColors.TEXT_DIM);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!textField.isEnabled())
                    return;
                try {
                    int current = textField.getText().isEmpty() ? 0 : Integer.parseInt(textField.getText());
                    int newVal = Math.max(0, current + step); // Empêche les index négatifs
                    textField.setText(String.valueOf(newVal));

                    // Simule une frappe de touche pour déclencher le recalcul auto de la conso
                    for (KeyListener kl : textField.getKeyListeners()) {
                        kl.keyReleased(new KeyEvent(textField, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0,
                                KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });
        return label;
    }

    private void setupFocusBehavior() {
        if (variant == Variant.BLOCK_READONLY)
            return;

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                int thickness = (variant == Variant.SEARCH) ? 2 : 1;
                innerContainer.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(AppColors.ACCENT_AMBER, thickness),
                        new EmptyBorder((variant == Variant.SEARCH) ? 12 : 10, (variant == Variant.SEARCH) ? 16 : 14,
                                (variant == Variant.SEARCH) ? 12 : 10, (variant == Variant.SEARCH) ? 16 : 14)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                Color borderCol = (variant == Variant.SEARCH) ? AppColors.ACCENT_AMBER : AppColors.BORDER_DEFAULT;
                innerContainer.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(borderCol, 1),
                        new EmptyBorder((variant == Variant.SEARCH) ? 12 : 10, (variant == Variant.SEARCH) ? 16 : 14,
                                (variant == Variant.SEARCH) ? 12 : 10, (variant == Variant.SEARCH) ? 16 : 14)));
            }
        });
    }

    public String getText() {
        return textField.getText().trim();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public void setInputFieldEnabled(boolean enabled) {
        textField.setEnabled(enabled);
        if (variant == Variant.SEARCH) {
            innerContainer.repaint();
            textField.setForeground(enabled ? Color.decode("#111827") : AppColors.TEXT_DIM);
        }
    }

    public void addActionListener(java.awt.event.ActionListener l) {
        textField.addActionListener(l);
    }

    public void addKeyListener(KeyAdapter l) {
        textField.addKeyListener(l);
    }

    @Override
    public boolean requestFocusInWindow() {
        return textField.requestFocusInWindow();
    }
}
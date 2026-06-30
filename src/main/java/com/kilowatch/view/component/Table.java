package com.kilowatch.view.component;

import com.kilowatch.view.theme.AppColors;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseAdapter;
import java.util.HashMap;
import java.util.Map;

public class Table extends JTable {

    private int hoveredRow = -1;
    private int hoveredColumn = -1; // NOUVEAU: Pour savoir quelle cellule exacte est survolée

    private final Map<Integer, Integer> columnAlignments = new HashMap<>();
    private final Map<Integer, ActionListener> actionColumns = new HashMap<>(); // Stocke les actions des boutons

    public Table(TableModel model) {
        super(model);

        // --- 1. CONFIGURATION VISUELLE DE BASE ---
        setRowHeight(48);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(AppColors.BORDER_SOFT);
        setFocusable(false);
        setBackground(AppColors.BG_SURFACE_2);

        // --- 2. STYLE DE L'EN-TÊTE (HEADER) ---
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER_SOFT));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(AppColors.BG_SURFACE_2);
                setForeground(AppColors.TEXT_SECONDARY);
                setFont(new Font("Inter", Font.BOLD, 12));

                int alignment = getColumnAlignment(column);
                setHorizontalAlignment(alignment);
                applyAlignmentPadding(this, alignment);

                return this;
            }
        });

        // --- 3. GESTION DE LA SÉLECTION ---
        setSelectionBackground(new Color(45, 60, 80));
        setSelectionForeground(AppColors.TEXT_PRIMARY);

        // --- 4. ENGINE DE HOVER (CORRIGÉ POUR BOUTONS ET SOURIS) ---
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());

                // Change le curseur si on survole une colonne d'action
                if (actionColumns.containsKey(col)) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

                if (row != hoveredRow || col != hoveredColumn) {
                    int oldHoveredRow = hoveredRow;
                    hoveredRow = row;
                    hoveredColumn = col;

                    if (oldHoveredRow != -1)
                        repaintRow(oldHoveredRow);
                    if (hoveredRow != -1)
                        repaintRow(hoveredRow);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredRow != -1) {
                    int oldHovered = hoveredRow;
                    hoveredRow = -1;
                    hoveredColumn = -1;
                    repaintRow(oldHovered);
                }
            }

            // DÉTECTION DU CLIC POUR LE BOUTON
            @Override
            public void mousePressed(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row != -1 && col != -1 && actionColumns.containsKey(col)) {
                    // Déclenche l'action associée à la colonne, en passant l'index de la ligne
                    // comme Command
                    actionColumns.get(col)
                            .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, String.valueOf(row)));
                }
            }
        });

        // --- 5. RENDERER PAR DÉFAUT ---
        setDefaultRenderer(Object.class, new DefaultCustomRenderer());
    }

    // --- MÉTHODES POUR L'ALIGNEMENT NATIF ---
    public void setColumnAlignment(int columnIndex, int alignment) {
        columnAlignments.put(columnIndex, alignment);
    }

    public int getColumnAlignment(int columnIndex) {
        return columnAlignments.getOrDefault(columnIndex, SwingConstants.LEFT);
    }

    private void applyAlignmentPadding(JComponent comp, int alignment) {
        if (alignment == SwingConstants.RIGHT) {
            comp.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 24));
        } else if (alignment == SwingConstants.CENTER) {
            comp.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        } else {
            comp.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 16));
        }
    }

    // --- NOUVEAU: CONFIGURER UNE COLONNE COMME BOUTON D'ACTION ---
    public void setActionColumn(int columnIndex, String buttonText, ActionListener listener) {
        actionColumns.put(columnIndex, listener);
        getColumnModel().getColumn(columnIndex).setCellRenderer(new ButtonCellRenderer(buttonText));
    }

    // --- UTILITAIRES INTERNES ---
    private void repaintRow(int row) {
        Rectangle rect = getCellRect(row, 0, true);
        rect.width = getWidth();
        repaint(rect);
    }

    public void applyRowBackground(Component comp, int row, boolean isSelected) {
        if (isSelected) {
            comp.setBackground(getSelectionBackground());
        } else if (row == hoveredRow) {
            comp.setBackground(new Color(35, 45, 60));
        } else {
            comp.setBackground(getBackground());
        }
    }

    // ==========================================================
    // RENDERERS (Défaut, Action et Badge)
    // ==========================================================

    private class DefaultCustomRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setOpaque(true);
            applyRowBackground(this, row, isSelected);
            setForeground(isSelected ? table.getSelectionForeground() : AppColors.TEXT_PRIMARY);

            int alignment = getColumnAlignment(column);
            setHorizontalAlignment(alignment);
            applyAlignmentPadding(this, alignment);

            return this;
        }
    }

    // RENDERER DU BOUTON (Gère le survol visuel du bouton ET de la ligne)
    public class ButtonCellRenderer extends DefaultTableCellRenderer {
        private final String buttonText;

        public ButtonCellRenderer(String buttonText) {
            this.buttonText = buttonText;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            if (table instanceof Table) {
                ((Table) table).applyRowBackground(this, row, isSelected);
            }

            // Stocke la ligne et la colonne pour le paintComponent
            putClientProperty("cell.row", row);
            putClientProperty("cell.col", column);
            setText("");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int r = (Integer) getClientProperty("cell.row");
            int c = (Integer) getClientProperty("cell.col");

            // Le bouton est "survolé" si la souris est exactement sur cette ligne et cette
            // colonne
            boolean isButtonHovered = (r == hoveredRow && c == hoveredColumn);

            g2.setFont(new Font("Inter", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(buttonText) + 24;
            int h = 28;
            int x = getWidth() - w - 24; // Alignement à droite
            int y = (getHeight() - h) / 2;

            // Changement de couleur du bouton au survol
            if (isButtonHovered) {
                g2.setColor(AppColors.BORDER_DEFAULT); // Hover comme la sidebar
            } else {
                g2.setColor(AppColors.BG_DEEP); // Fond normal
            }
            g2.fillRoundRect(x, y, w, h, 8, 8);

            g2.setColor(AppColors.BORDER_SOFT);
            g2.drawRoundRect(x, y, w, h, 8, 8);

            g2.setColor(AppColors.ACCENT_AMBER);
            g2.drawString(buttonText, x + 12, y + ((h - fm.getHeight()) / 2) + fm.getAscent());

            g2.dispose();
        }
    }

    public static class BadgeCellRenderer extends DefaultTableCellRenderer {
        private final Map<String, BadgeStyle> styles = new HashMap<>();

        public void registerStyle(String textKey, BadgeStyle style) {
            styles.put(textKey, style);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Inter", Font.BOLD, 11));
            setOpaque(true);

            if (table instanceof Table) {
                ((Table) table).applyRowBackground(this, row, isSelected);
            }

            String text = (value != null) ? value.toString() : "";
            BadgeStyle style = styles.get(text);

            if (style != null) {
                putClientProperty("badge.bg", style.getBackground());
                putClientProperty("badge.fg", style.getForeground());
            } else {
                putClientProperty("badge.bg", null);
                putClientProperty("badge.fg", isSelected ? table.getSelectionForeground() : AppColors.TEXT_SECONDARY);
            }

            putClientProperty("badge.text", text);
            setText("");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Color badgeBg = (Color) getClientProperty("badge.bg");
            Color badgeFg = (Color) getClientProperty("badge.fg");
            String text = (String) getClientProperty("badge.text");
            if (text == null)
                text = "";

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics(getFont());

            int x = 24;

            if (badgeBg != null) {
                int textWidth = fm.stringWidth(text);
                int paddingX = 24;
                int badgeWidth = textWidth + paddingX;
                int badgeHeight = 24;
                int y = (getHeight() - badgeHeight) / 2;

                g2.setColor(badgeBg);
                g2.fillRoundRect(x, y, badgeWidth, badgeHeight, 12, 12);

                g2.setColor(badgeFg);
                int textX = x + paddingX / 2;
                int textY = y + ((badgeHeight - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, textX, textY);
            } else {
                g2.setColor(badgeFg);
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, x, y);
            }
            g2.dispose();
        }
    }

    public static class BadgeStyle {
        private final Color background;
        private final Color foreground;

        public BadgeStyle(Color background, Color foreground) {
            this.background = background;
            this.foreground = foreground;
        }

        public Color getBackground() {
            return background;
        }

        public Color getForeground() {
            return foreground;
        }
    }

    public JPanel createRoundedContainer() {
        RoundedCornerBorder roundedBorder = new RoundedCornerBorder(12, AppColors.BORDER_SOFT);

        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppColors.BG_SURFACE_2);
        scrollPane.getViewport().setOpaque(true);

        javax.swing.plaf.LayerUI<Component> layerUI = new javax.swing.plaf.LayerUI<>() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.clip(new java.awt.geom.RoundRectangle2D.Double(0, 0, c.getWidth(), c.getHeight(), 12, 12));
                super.paint(g2, c);
                g2.dispose();
            }
        };

        JLayer<Component> jlayer = new JLayer<>(scrollPane, layerUI);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(roundedBorder);
        tablePanel.add(jlayer, BorderLayout.CENTER);

        return tablePanel;
    }
}
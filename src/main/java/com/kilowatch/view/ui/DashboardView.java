package com.kilowatch.view.ui;

import com.kilowatch.view.component.Table;
import com.kilowatch.view.theme.AppColors;
import com.kilowatch.view.theme.AppIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DashboardView extends JPanel {

    // --- COMPOSANTS INTERNES À METTRE À JOUR ---
    private KpiCard caCard;
    private KpiCard recouvrementCard;
    private KpiCard abonnesCard;
    private SimpleBarChart barChart;

    public DashboardView(TableModel recentActionsModel) {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_DEEP);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        mainContent.add(createHeaderSection());
        mainContent.add(Box.createVerticalStrut(24));
        mainContent.add(createKpiSection());
        mainContent.add(Box.createVerticalStrut(32));
        mainContent.add(createChartSection());
        mainContent.add(Box.createVerticalStrut(32));
        mainContent.add(createRecentActionsSection(recentActionsModel));

        add(mainContent, BorderLayout.CENTER);
    }

    // =========================================================
    // MÉTHODES PUBLIQUES
    // =========================================================

    public void updateKpiCa(String value, String subtitle) {
        caCard.updateData(value, subtitle);
    }

    public void updateKpiRecouvrement(String value, String subtitle) {
        recouvrementCard.updateData(value, subtitle);
    }

    public void updateKpiAbonnes(String value, String subtitle) {
        abonnesCard.updateData(value, subtitle);
    }

    /**
     * Accepte un tableau d'entiers. Sécurise la taille à exactement 7 jours.
     */
    public void setChartData(int[] data) {
        int[] safeData = new int[7];
        if (data != null) {
            // On copie un maximum de 7 éléments. Si data est trop petit, les autres
            // resteront à 0.
            System.arraycopy(data, 0, safeData, 0, Math.min(data.length, 7));
        }
        barChart.updateChartData(safeData);
    }

    // =========================================================
    // CONSTRUCTION DE L'INTERFACE
    // =========================================================

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Tableau de bord");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 26));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("État de santé du réseau post-payé en temps réel");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(AppColors.TEXT_SECONDARY);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    private JPanel createKpiSection() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 24, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiPanel.setPreferredSize(new Dimension(1000, 110));
        kpiPanel.setMaximumSize(new Dimension(2000, 110));

        caCard = new KpiCard("Chiffre d'affaires du mois (TTC)", "—", "—", AppColors.ACCENT_AMBER, AppIcons.WALLET);
        recouvrementCard = new KpiCard("Taux de recouvrement", "—", "—", AppColors.STATUS_GREEN,
                AppIcons.CIRCLE_CHECK_BIG);
        abonnesCard = new KpiCard("Total Abonnés actifs", "—", "—", AppColors.ACCENT_BLUE, AppIcons.USERS);

        kpiPanel.add(caCard);
        kpiPanel.add(recouvrementCard);
        kpiPanel.add(abonnesCard);

        return kpiPanel;
    }

    private JPanel createChartSection() {
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setOpaque(false);
        chartContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel chartTitle = new JLabel("Évolution des encaissements (7 derniers jours)");
        chartTitle.setFont(new Font("Inter", Font.BOLD, 16));
        chartTitle.setForeground(AppColors.TEXT_PRIMARY);
        chartTitle.setBorder(new EmptyBorder(0, 0, 16, 0));

        chartContainer.add(chartTitle, BorderLayout.NORTH);

        barChart = new SimpleBarChart();
        chartContainer.add(barChart, BorderLayout.CENTER);

        return chartContainer;
    }

    private JPanel createRecentActionsSection(TableModel model) {
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Dernières actions effectuées");
        title.setFont(new Font("Inter", Font.BOLD, 16));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JLabel limitText = new JLabel("10 plus récentes");
        limitText.setFont(new Font("Inter", Font.PLAIN, 12));
        limitText.setForeground(AppColors.TEXT_DIM);

        header.add(title, BorderLayout.WEST);
        header.add(limitText, BorderLayout.EAST);
        tableContainer.add(header, BorderLayout.NORTH);

        Table table = new Table(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(0).setMaxWidth(200);

        tableContainer.add(table.createRoundedContainer(), BorderLayout.CENTER);

        return tableContainer;
    }

    // =========================================================
    // SOUS-COMPOSANTS PERSONNALISÉS
    // =========================================================

    private static class KpiCard extends JPanel {
        private final Color accentColor;
        private final JLabel valueLabel;
        private final JLabel subtitleLabel;

        public KpiCard(String title, String value, String subtitle, Color accentColor, AppIcons iconEnum) {
            this.accentColor = accentColor;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(16, 24, 16, 16));

            JLabel iconLabel = new JLabel(iconEnum.get(28, accentColor));
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 16));
            add(iconLabel, BorderLayout.WEST);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Inter", Font.PLAIN, 13));
            titleLabel.setForeground(AppColors.TEXT_SECONDARY);

            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Inter", Font.BOLD, 28));
            valueLabel.setForeground(AppColors.TEXT_PRIMARY);

            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            subtitleLabel.setForeground(AppColors.TEXT_DIM);

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(4));
            textPanel.add(valueLabel);
            textPanel.add(Box.createVerticalStrut(4));
            textPanel.add(subtitleLabel);

            add(textPanel, BorderLayout.CENTER);
        }

        public void updateData(String value, String subtitle) {
            valueLabel.setText(value);
            subtitleLabel.setText(subtitle);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(AppColors.BG_SURFACE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
            g2.setColor(AppColors.BORDER_SOFT);
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

            g2.setColor(accentColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, 4, getHeight() - 1, 12, 12));
            g2.fillRect(2, 0, 2, getHeight() - 1);
            g2.dispose();
        }
    }

    private static class SimpleBarChart extends JPanel {
        private int[] data = new int[7];
        private String[] labels = new String[7];

        public SimpleBarChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(100, 200));
            generateLabels(); // Génère les labels par défaut au démarrage
        }

        public void updateChartData(int[] newData) {
            this.data = newData;
            generateLabels(); // Recalcule au cas où on changerait de jour à minuit
            repaint();
        }

        // Génère les noms des 7 derniers jours par rapport à aujourd'hui
        private void generateLabels() {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.minusDays(6 - i);
                String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
                // On nettoie le point (ex: "lun." -> "Lun")
                dayName = dayName.replace(".", "");
                labels[i] = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fond
            g2.setColor(AppColors.BG_SURFACE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
            g2.setColor(AppColors.BORDER_SOFT);
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

            // Calcul du max dynamique pour l'échelle
            int maxData = 1; // Évite la division par zéro
            for (int val : data) {
                if (val > maxData)
                    maxData = val;
            }
            maxData = (int) (maxData * 1.1); // +10% de marge en haut

            int padding = 40;
            int chartWidth = getWidth() - padding * 2;
            int chartHeight = getHeight() - padding * 2;
            int barWidth = (chartWidth / 7) - 20;

            g2.setFont(new Font("Inter", Font.PLAIN, 11));

            // Dessin des 7 barres
            for (int i = 0; i < 7; i++) {
                int barHeight = (int) (((double) data[i] / maxData) * chartHeight);
                int x = padding + (i * (chartWidth / 7)) + 10;
                int y = padding + chartHeight - barHeight;

                // MISE EN ÉVIDENCE : Si i == 6, c'est aujourd'hui, on change la couleur !
                if (i == 6) {
                    g2.setColor(AppColors.ACCENT_AMBER);
                } else {
                    g2.setColor(AppColors.ACCENT_BLUE);
                }

                g2.fill(new RoundRectangle2D.Double(x, y, barWidth, barHeight, 6, 6));

                // Labels
                if (i == 6) {
                    // Mettre le texte en gras et ambré pour "Aujourd'hui"
                    g2.setFont(new Font("Inter", Font.BOLD, 12));
                    g2.setColor(AppColors.ACCENT_AMBER);
                } else {
                    g2.setFont(new Font("Inter", Font.PLAIN, 11));
                    g2.setColor(AppColors.TEXT_SECONDARY);
                }

                int stringWidth = g2.getFontMetrics().stringWidth(labels[i]);
                g2.drawString(labels[i], x + (barWidth - stringWidth) / 2, padding + chartHeight + 20);
            }
            g2.dispose();
        }
    }
}
package com.kilowatch;

import com.formdev.flatlaf.FlatDarkLaf;
import com.kilowatch.repositories.AbonneRepository;
import com.kilowatch.service.GestionnaireAbonnes;
import com.kilowatch.service.MoteurFacturation;
import com.kilowatch.view.data.KilowatchDataService;
import com.kilowatch.view.theme.ThemeConfigurator;
import com.kilowatch.view.ui.LoginDialog; // <-- NOUVEL IMPORT
import com.kilowatch.view.ui.MainLayout;
import com.kilowatch.model.Abonne;
import com.kilowatch.model.Facture;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // 1. ACTIVER LES DÉCORATIONS DE FENÊTRE FLATLAF
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        System.setProperty("flatlaf.menuBarEmbedded", "true");

        // 2. Initialiser le thème de base FlatLaf
        FlatDarkLaf.setup();

        // 3. Appliquer la configuration globale (couleurs, polices, hovers...)
        ThemeConfigurator.setupGlobalTheme();

        // 4. Lancer l'interface graphique réelle
        SwingUtilities.invokeLater(() -> {

            // ─── PIPELINE DE PRODUCTION (Persistance & Métier) ───
            AbonneRepository repository = new AbonneRepository();
            GestionnaireAbonnes gestionnaire = new GestionnaireAbonnes();

            // Chargement des données existantes depuis le fichier .ser (si présent)
            AbonneRepository.DonneesKiloWatch donneesChargees = repository.charger();
            if (donneesChargees != null) {
                for (Abonne abonne : donneesChargees.abonnes) {
                    gestionnaire.ajouterAbonne(abonne);
                }
                for (Facture facture : donneesChargees.factures) {
                    gestionnaire.ajouterFacture(facture);
                }
                System.out.println("[Prod] Restauration du système réussie.");
            }

            MoteurFacturation moteur = new MoteurFacturation(gestionnaire);

            // Création de notre Facade qui gère absolument tout
            KilowatchDataService dataService = new KilowatchDataService(gestionnaire, moteur, repository);

            // ─── NOUVEAU : GESTION DE L'AUTHENTIFICATION ───
            LoginDialog login = new LoginDialog((username, password) -> {
                // On délègue la vérification à notre DataService
                return dataService.authentifier(username, password);
            });

            // Affiche la fenêtre et bloque l'exécution jusqu'à ce qu'elle soit fermée
            login.setVisible(true);

            // Si la fenêtre a été fermée sans succès (croix rouge, annuler, etc.)
            if (!login.isSuccess()) {
                System.out.println("[Auth] Connexion annulée ou échouée. Fermeture de Kilowatch.");
                System.exit(0);
            }

            System.out.println("[Auth] Accès autorisé. Démarrage de l'interface principale...");
            // ───────────────────────────────────────────────

            // Injection du service de PROD dans l'IHM
            MainLayout app = new MainLayout(dataService);

            app.setMinimumSize(new Dimension(1150, 720));
            app.setSize(1280, 800);
            app.setLocationRelativeTo(null);

            // Sauvegarde automatique à la fermeture
            app.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            app.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("[Prod] Fermeture de l'application. Sauvegarde...");

                    // Collecte des abonnés (Correction : on utilise getAllAbonnes() direct, c'est
                    // plus fiable)
                    List<Abonne> listeAbonnes = gestionnaire.getAllAbonnes();

                    boolean sauvegardeReussie = repository.sauvegarder(
                            listeAbonnes,
                            gestionnaire.listerToutesFactures());

                    if (sauvegardeReussie) {
                        System.out.println("[Prod] Données sécurisées avec succès.");
                    }

                    app.dispose();
                    System.exit(0);
                }
            });

            app.setVisible(true);
        });
    }
}
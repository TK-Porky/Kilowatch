package com.kilowatch.repositories;

import com.kilowatch.model.Abonne;
import com.kilowatch.model.Facture;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Gère la persistance des données KiloWatch.
 * - Sérialisation : sauvegarde/chargement complet en fichier .ser
 * - Export CSV : exportation des factures impayées en fichier .csv
 */
public class AbonneRepository {

    // ── Chemins des fichiers de données
    private static final String DOSSIER_DATA = "data/";
    private static final String FICHIER_SER = DOSSIER_DATA + "kilowatch_data.ser";
    private static final String FICHIER_CSV = DOSSIER_DATA + "factures_impayees.csv";

    // ── Séparateur CSV
    private static final String SEPARATEUR = "|";

    public static class DonneesKiloWatch implements Serializable {
        private static final long serialVersionUID = 1L;

        public final List<Abonne> abonnes;
        public final List<Facture> factures;

        public DonneesKiloWatch(List<Abonne> abonnes, List<Facture> factures) {
            this.abonnes = abonnes;
            this.factures = factures;
        }
    }

    public boolean sauvegarder(List<Abonne> abonnes, List<Facture> factures) {
        creerDossierSiAbsent();

        DonneesKiloWatch donnees = new DonneesKiloWatch(
                new ArrayList<>(abonnes),
                new ArrayList<>(factures));

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FICHIER_SER))) {

            oos.writeObject(donnees);
            System.out.println("[Persistance] Sauvegarde réussie → " + FICHIER_SER);
            System.out.println("             " + abonnes.size() + " abonné(s), "
                    + factures.size() + " facture(s).");
            return true;

        } catch (IOException e) {
            System.err.println("[Persistance] Erreur sauvegarde : " + e.getMessage());
            return false;
        }
    }

    public DonneesKiloWatch charger() {
        File fichier = new File(FICHIER_SER);

        // Premier lancement : aucun fichier → listes vides, pas d'erreur
        if (!fichier.exists()) {
            System.out.println("[Persistance] Aucune sauvegarde trouvée. Démarrage avec données vides.");
            return new DonneesKiloWatch(new ArrayList<>(), new ArrayList<>());
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(FICHIER_SER))) {

            DonneesKiloWatch donnees = (DonneesKiloWatch) ois.readObject();

            System.out.println("[Persistance] Chargement réussi ← " + FICHIER_SER);
            System.out.println("             " + donnees.abonnes.size() + " abonné(s), "
                    + donnees.factures.size() + " facture(s).");
            return donnees;

        } catch (FileNotFoundException e) {
            System.err.println("[Persistance] Fichier introuvable : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[Persistance] Fichier incompatible (version différente) : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[Persistance] Fichier corrompu : " + e.getMessage());
        }

        // En cas d'erreur → listes vides pour ne pas planter l'application
        return new DonneesKiloWatch(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Version asynchrone de l'export CSV vers un emplacement personnalisé.
     * Le callback `progressCallback` reçoit un entier représentant le pourcentage
     * d'avancement.
     */
    public CompletableFuture<Integer> exporterFacturesImpayeesAsync(File destination, List<Facture> factures,
            Consumer<Integer> progressCallback, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            // S'assurer que les dossiers parents existent (ex: si l'utilisateur crée un
            // nouveau dossier)
            if (destination.getParentFile() != null) {
                destination.getParentFile().mkdirs();
            }

            List<Facture> impayees = new ArrayList<>();
            for (Facture f : factures) {
                if (!f.isPayee())
                    impayees.add(f);
            }

            if (impayees.isEmpty()) {
                if (progressCallback != null)
                    progressCallback.accept(100);
                return 0;
            }

            // On utilise directement le fichier destination choisi par le guichetier
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                writer.write(
                        "id" + SEPARATEUR +
                                "idAbonne" + SEPARATEUR +
                                "categorie" + SEPARATEUR +
                                "consommation_kWh" + SEPARATEUR +
                                "montantHT_FCFA" + SEPARATEUR +
                                "montantTVA_FCFA" + SEPARATEUR +
                                "montantTTC_FCFA" + SEPARATEUR +
                                "dateEmission");
                writer.newLine();

                int total = impayees.size();
                for (int i = 0; i < total; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return 0; // Gestion de l'annulation si nécessaire
                    }
                    Facture f = impayees.get(i);
                    writer.write(
                            f.getId() + SEPARATEUR +
                                    f.getIdAbonne() + SEPARATEUR +
                                    f.getCategorieAbonne() + SEPARATEUR +
                                    String.format("%.2f", f.getConsommationKWh()) + SEPARATEUR +
                                    String.format("%.2f", f.getMontantHT()) + SEPARATEUR +
                                    String.format("%.2f", f.getMontantTVA()) + SEPARATEUR +
                                    String.format("%.2f", f.getMontantTTC()) + SEPARATEUR +
                                    f.getDateEmission().toString());
                    writer.newLine();

                    if (progressCallback != null) {
                        int pct = (int) Math.round(((i + 1) / (double) total) * 100);
                        progressCallback.accept(pct);
                    }
                }

                System.out.println("[Export CSV] Export réussi vers → " + destination.getAbsolutePath());
                return impayees.size();

            } catch (IOException e) {
                System.err.println("[Export CSV] Erreur lors de l'export : " + e.getMessage());
                throw new RuntimeException("Impossible d'écrire le fichier : " + e.getMessage(), e);
            }
        }, executor);
    }

    private void creerDossierSiAbsent() {
        File dossier = new File(DOSSIER_DATA);
        if (!dossier.exists()) {
            dossier.mkdirs();
        }
    }

    public boolean sauvegardeExiste() {
        return new File(FICHIER_SER).exists();
    }

    public boolean supprimerSauvegarde() {
        File fichier = new File(FICHIER_SER);
        if (fichier.exists()) {
            boolean supprime = fichier.delete();
            if (supprime) {
                System.out.println("[Persistance] Sauvegarde supprimée.");
            }
            return supprime;
        }
        return false;
    }
}
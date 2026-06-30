package com.kilowatch.repositories;

import com.kilowatch.model.Abonne;
import com.kilowatch.model.Facture;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la persistance des données KiloWatch.
 * - Sérialisation : sauvegarde/chargement complet en fichier .ser
 * - Export CSV    : exportation des factures impayées en fichier .csv
 */
public class AbonneRepository {

    // ── Chemins des fichiers de données
    private static final String DOSSIER_DATA   = "data/";
    private static final String FICHIER_SER    = DOSSIER_DATA + "kilowatch_data.ser";
    private static final String FICHIER_CSV    = DOSSIER_DATA + "factures_impayees.csv";

    // ── Séparateur CSV
    private static final String SEPARATEUR     = ";";

  
    public static class DonneesKiloWatch implements Serializable {
        private static final long serialVersionUID = 1L;

        public final List<Abonne>  abonnes;
        public final List<Facture> factures;

        public DonneesKiloWatch(List<Abonne> abonnes, List<Facture> factures) {
            this.abonnes  = abonnes;
            this.factures = factures;
        }
    }

    
    public boolean sauvegarder(List<Abonne> abonnes, List<Facture> factures) {
        creerDossierSiAbsent();

        DonneesKiloWatch donnees = new DonneesKiloWatch(
                new ArrayList<>(abonnes),
                new ArrayList<>(factures)
        );

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FICHIER_SER))) {

            oos.writeObject(donnees);
            System.out.println("[Persistance] Sauvegarde réussie → " + FICHIER_SER);
            System.out.println("             " + abonnes.size()  + " abonné(s), "
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

            @SuppressWarnings("unchecked")
            DonneesKiloWatch donnees = (DonneesKiloWatch) ois.readObject();

            System.out.println("[Persistance] Chargement réussi ← " + FICHIER_SER);
            System.out.println("             " + donnees.abonnes.size()  + " abonné(s), "
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

   
    public int exporterFacturesImpayees(List<Facture> factures) {
        creerDossierSiAbsent();

        // Filtrer uniquement les factures impayées
        List<Facture> impayees = new ArrayList<>();
        for (Facture f : factures) {
            if (!f.isPayee()) {
                impayees.add(f);
            }
        }

        if (impayees.isEmpty()) {
            System.out.println("[Export CSV] Aucune facture impayée à exporter.");
            return 0;
        }

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FICHIER_CSV))) {

           
            writer.write(
                "id" + SEPARATEUR +
                "idAbonne" + SEPARATEUR +
                "categorie" + SEPARATEUR +
                "consommation_kWh" + SEPARATEUR +
                "montantHT_FCFA" + SEPARATEUR +
                "montantTVA_FCFA" + SEPARATEUR +
                "montantTTC_FCFA" + SEPARATEUR +
                "dateEmission"
            );
            writer.newLine();

            for (Facture f : impayees) {
                writer.write(
                    f.getId()                              + SEPARATEUR +
                    f.getIdAbonne()                        + SEPARATEUR +
                    f.getCategorieAbonne()                 + SEPARATEUR +
                    String.format("%.2f", f.getConsommationKWh()) + SEPARATEUR +
                    String.format("%.2f", f.getMontantHT())       + SEPARATEUR +
                    String.format("%.2f", f.getMontantTVA())      + SEPARATEUR +
                    String.format("%.2f", f.getMontantTTC())      + SEPARATEUR +
                    f.getDateEmission().toString()
                );
                writer.newLine();
            }

            System.out.println("[Export CSV] Export réussi → " + FICHIER_CSV);
            System.out.println("             " + impayees.size() + " facture(s) impayée(s) exportée(s).");
            return impayees.size();

        } catch (IOException e) {
            System.err.println("[Export CSV] Erreur lors de l'export : " + e.getMessage());
            return 0;
        }
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
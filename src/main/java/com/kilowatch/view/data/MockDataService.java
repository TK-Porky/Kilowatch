package com.kilowatch.view.data;

import com.kilowatch.view.data.ViewDto.Abonne;
import com.kilowatch.view.data.ViewDto.FactureEnAttente;
import com.kilowatch.view.data.ViewDto.ReleveSession;
import com.kilowatch.view.data.ViewDto.ActionLog;
import com.kilowatch.view.data.ViewDto.DashboardStats;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockDataService implements ViewDataService {

    private final List<Abonne> abonnesDb;
    private final List<FactureEnAttente> facturesDb;

    // --- NOUVELLES DONNÉES DE SESSION POUR LE DASHBOARD ---
    private final List<ActionLog> recentActions = new ArrayList<>();
    private double chiffreAffairesSession = 0.0;
    private int abonnesInscritsCeMois = 0;

    // On simule un historique de départ pour le graphique (les 6 premiers jours de
    // la semaine)
    private final int[] encaissementsSemaine = { 12000, 45000, 32000, 78000, 21000, 65000, 0 };

    public MockDataService() {
        // Initialisation de nos fausses données
        abonnesDb = new ArrayList<>(List.of(
                new Abonne("AB-1000", "Ngo Bilong A.", "CMP-20000", "Social", 2803, "En attente"),
                new Abonne("AB-1001", "Etoa Ekani C.", "CMP-20007", "Résidentiel", 1433, "Impayée"),
                new Abonne("AB-1002", "Fouda Mballa P.", "CMP-20014", "Résidentiel", 4955, "Impayée"),
                new Abonne("AB-1003", "Atangana R.", "CMP-20021", "Industriel", 2940, "En attente"),
                new Abonne("AB-1004", "Mendomo S.", "CMP-20028", "Résidentiel", 3769, "Payée"),
                new Abonne("AB-1005", "Talla J.", "CMP-20035", "Social", 3558, "Impayée")));

        facturesDb = new ArrayList<>(List.of(
                new FactureEnAttente("Njoya K.", "CMP-20098", "Résidentiel", 303, 34326.0),
                new FactureEnAttente("Fouda Mballa P.", "CMP-20014", "Résidentiel", 274, 31041.0),
                new FactureEnAttente("Sané Aïcha", "CMP-20133", "Résidentiel", 271, 30701.0)));

        // Initialisation du log d'actions
        registrarAction("Démarrage du système Kilowatch");
    }

    // =========================================================
    // NOUVELLES MÉTHODES DU DASHBOARD
    // =========================================================

    @Override
    public void registrarAction(String description) {
        String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        recentActions.add(0, new ActionLog(heure, description));

        if (recentActions.size() > 10) {
            recentActions.remove(recentActions.size() - 1);
        }
    }

    @Override
    public List<ActionLog> getRecentActions() {
        return new ArrayList<>(recentActions); // Copie pour éviter les modifications externes
    }

    @Override
    public DashboardStats getDashboardStats() {
        int totalAbonnes = abonnesDb.size();

        // Simulation dynamique du taux de recouvrement
        int facturesPayees = 4 + (int) (chiffreAffairesSession / 15000);
        int totalFactures = 13 + (int) (chiffreAffairesSession / 15000) + facturesDb.size();

        // Le 7ème jour (index 6) représente aujourd'hui, on y met le CA de la session
        encaissementsSemaine[6] = (int) chiffreAffairesSession;

        return new DashboardStats(
                chiffreAffairesSession,
                totalFactures,
                facturesPayees,
                totalAbonnes,
                abonnesInscritsCeMois,
                encaissementsSemaine.clone());
    }

    // =========================================================
    // MÉTHODES MÉTIER EXISTANTES (Modifiées pour le Dashboard)
    // =========================================================

    @Override
    public List<Abonne> getAllAbonnes() {
        return abonnesDb;
    }

    @Override
    public List<Abonne> searchAbonnes(String keyword) {
        String lowerKw = keyword.toLowerCase();
        return abonnesDb.stream()
                .filter(a -> a.nom().toLowerCase().contains(lowerKw)
                        || a.numeroCompteur().toLowerCase().contains(lowerKw))
                .collect(Collectors.toList());
    }

    @Override
    public List<FactureEnAttente> getFacturesEnAttente() {
        return facturesDb;
    }

    @Override
    public boolean encaisserFacture(String numeroCompteur) {
        // 1. Trouver le montant avant de l'effacer pour l'ajouter au CA
        double montantSaisi = 0;
        for (FactureEnAttente f : facturesDb) {
            if (f.numeroCompteur().equals(numeroCompteur)) {
                montantSaisi = f.montantTtc();
                break;
            }
        }

        // 2. Tenter de supprimer la facture (l'encaisser)
        boolean success = facturesDb.removeIf(f -> f.numeroCompteur().equals(numeroCompteur));

        // 3. Mettre à jour les stats du Dashboard
        if (success) {
            chiffreAffairesSession += montantSaisi;
            registrarAction("Encaissement enregistré : " + numeroCompteur + " (+" + String.format("%,.0f", montantSaisi)
                    + " FCFA)");
        }

        return success;
    }

    @Override
    public Abonne findAbonneForReleve(String query) {
        return abonnesDb.stream()
                .filter(a -> a.numeroCompteur().equalsIgnoreCase(query)
                        || a.nom().toLowerCase().contains(query.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ReleveSession validerNouvelIndex(String numeroCompteur, int nouvelIndex) throws IllegalArgumentException {
        Abonne abonne = findAbonneForReleve(numeroCompteur);
        if (abonne == null)
            throw new IllegalArgumentException("Compteur introuvable.");

        int conso = nouvelIndex - abonne.ancienIndex();
        if (conso < 0) {
            throw new IllegalArgumentException("Impossible de valider : Le nouvel index (" + nouvelIndex
                    + ") est inférieur à l'ancien (" + abonne.ancienIndex() + ") !");
        }

        double prixKwh = abonne.categorie().equalsIgnoreCase("Social") ? 50.0 : 113.0;
        double montantCalcule = conso * prixKwh;

        facturesDb.add(0, new FactureEnAttente(
                abonne.nom(),
                abonne.numeroCompteur(),
                abonne.categorie(),
                conso,
                montantCalcule));

        // MAJ Dashboard
        registrarAction("Index validé pour le " + abonne.numeroCompteur() + " (+" + conso + " kWh)");

        String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        return new ReleveSession(heure, abonne.nom(), abonne.numeroCompteur(), conso);
    }

    @Override
    public Abonne saveAbonne(Abonne nouvelAbonne) {
        abonnesDb.add(0, nouvelAbonne);

        // MAJ Dashboard
        abonnesInscritsCeMois++;
        registrarAction("Nouvel abonné créé : " + nouvelAbonne.nom() + " (" + nouvelAbonne.numeroCompteur() + ")");

        return nouvelAbonne;
    }
}
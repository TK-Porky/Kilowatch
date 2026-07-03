package com.kilowatch.view.data;

import com.kilowatch.view.data.ViewDto.Abonne;
import com.kilowatch.view.data.ViewDto.FactureEnAttente;
import com.kilowatch.view.data.ViewDto.ReleveSession;
import com.kilowatch.view.data.ViewDto.ActionLog;
import com.kilowatch.view.data.ViewDto.DashboardStats;
import com.kilowatch.view.data.ViewDto.StatusbarInfo;
import com.kilowatch.view.data.ViewEnum.CategorieAbonne;
import com.kilowatch.view.data.ViewEnum.StatutFacture;
import com.kilowatch.view.data.ViewEnum.FiltreAbonne;
import com.kilowatch.view.data.ViewEnum.FiltreCaisse;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executor;

public class MockDataService implements ViewDataService {

    private final List<Abonne> abonnesDb;
    private final List<FactureEnAttente> facturesDb;
    // thread-safe list for recent actions
    private final List<ActionLog> recentActions = new CopyOnWriteArrayList<>();

    // single-threaded executor to serialize IO / mutations
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private double chiffreAffairesSession = 51450.0;
    private int abonnesInscritsCeMois = 0;

    // --- NOUVEAUX ATTRIBUTS DYNAMIQUES POUR LE DASHBOARD ---
    private int facturesPayees = 7;
    private int totalFactures = 16;

    private final int[] encaissementsSemaine = { 15000, 30000, 25000, 48000, 12000, 42000, 51450 };

    public MockDataService() {
        abonnesDb = new ArrayList<>(List.of(
                new Abonne("AB-1000", "Ngo Bilong A.", "CMP-20000", CategorieAbonne.SOCIAL.getLibelle(), 2803,
                        StatutFacture.EN_ATTENTE.getLibelle()),
                new Abonne("AB-1001", "Etoa Ekani C.", "CMP-20007", CategorieAbonne.RESIDENTIEL.getLibelle(), 1433,
                        StatutFacture.IMPAYEE.getLibelle()),
                new Abonne("AB-1002", "Fouda Mballa P.", "CMP-20014", CategorieAbonne.RESIDENTIEL.getLibelle(), 4955,
                        StatutFacture.IMPAYEE.getLibelle()),
                new Abonne("AB-1003", "Atangana R.", "CMP-20021", CategorieAbonne.INDUSTRIEL.getLibelle(), 2940,
                        StatutFacture.EN_ATTENTE.getLibelle()),
                new Abonne("AB-1004", "Mendomo S.", "CMP-20028", CategorieAbonne.RESIDENTIEL.getLibelle(), 3769,
                        StatutFacture.PAYEE.getLibelle()),
                new Abonne("AB-1005", "Talla J.", "CMP-20035", CategorieAbonne.SOCIAL.getLibelle(), 3558,
                        StatutFacture.IMPAYEE.getLibelle())));

        facturesDb = new ArrayList<>(List.of(
                new FactureEnAttente("Fouda Mballa P.", "CMP-20014", CategorieAbonne.RESIDENTIEL.getLibelle(), 274,
                        31041.0),
                new FactureEnAttente("Etoa Ekani C.", "CMP-20007", CategorieAbonne.RESIDENTIEL.getLibelle(), 120,
                        13560.0),
                new FactureEnAttente("Talla J.", "CMP-20035", CategorieAbonne.SOCIAL.getLibelle(), 137, 6849.0)));

        registrarAction("Démarrage du système Kilowatch");
        registrarAction("Encaissement enregistré : CMP-20014 (+31 041 FCFA)");
        registrarAction("Index validé pour le CMP-20014 (+23 kWh)");
        registrarAction("Encaissement enregistré : CMP-20007 (+13 560 FCFA)");
        registrarAction("Encaissement enregistré : CMP-20014 (+31 041 FCFA)");
    }

    @Override
    public List<Abonne> getFilteredAbonnes(String query, String filterId) {
        String lowerKw = (query == null) ? "" : query.toLowerCase().trim();

        return abonnesDb.stream()
                .filter(a -> lowerKw.isEmpty() ||
                        a.nom().toLowerCase().contains(lowerKw) ||
                        a.numeroCompteur().toLowerCase().contains(lowerKw))
                .filter(a -> {
                    if (FiltreAbonne.FILTER_GROS_CONS.name().equals(filterId)) {
                        return a.categorie().equalsIgnoreCase(CategorieAbonne.INDUSTRIEL.getLibelle())
                                || getConsoEnAttente(a.numeroCompteur()) >= 250;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public int getConsoEnAttente(String numeroCompteur) {
        return facturesDb.stream()
                .filter(f -> f.numeroCompteur().equalsIgnoreCase(numeroCompteur))
                .mapToInt(FactureEnAttente::consoKwh)
                .findFirst()
                .orElse(0);
    }

    @Override
    public List<FactureEnAttente> getFilteredFactures(String filterId, boolean sortDesc) {
        return facturesDb.stream()
                .filter(f -> {
                    if (FiltreCaisse.FILTER_SOCIAL.name().equals(filterId))
                        return f.categorie().equalsIgnoreCase(CategorieAbonne.SOCIAL.getLibelle());
                    if (FiltreCaisse.FILTER_RESIDENTIEL.name().equals(filterId))
                        return f.categorie().equalsIgnoreCase(CategorieAbonne.RESIDENTIEL.getLibelle());
                    if (FiltreCaisse.FILTER_INDUSTRIEL.name().equals(filterId))
                        return f.categorie().equalsIgnoreCase(CategorieAbonne.INDUSTRIEL.getLibelle());
                    return true;
                })
                .sorted((f1, f2) -> {
                    int cmp = Double.compare(f1.montantTtc(), f2.montantTtc());
                    return sortDesc ? -cmp : cmp;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Abonne> getAllAbonnes() {
        return getFilteredAbonnes("", FiltreAbonne.FILTER_ALL.name());
    }

    @Override
    public List<Abonne> searchAbonnes(String keyword) {
        return getFilteredAbonnes(keyword, FiltreAbonne.FILTER_ALL.name());
    }

    @Override
    public List<FactureEnAttente> getFacturesEnAttente() {
        return getFilteredFactures(FiltreCaisse.FILTER_ALL.name(), true);
    }

    @Override
    public boolean encaisserFacture(String numeroCompteur) {
        // perform mutation on single-thread executor to avoid concurrent writes
        CompletableFuture<Boolean> fut = CompletableFuture.supplyAsync(() -> {
            double montantSaisi = 0;
            for (FactureEnAttente f : facturesDb) {
                if (f.numeroCompteur().equals(numeroCompteur)) {
                    montantSaisi = f.montantTtc();
                    break;
                }
            }

            boolean success = facturesDb.removeIf(f -> f.numeroCompteur().equals(numeroCompteur));

            if (success) {
                chiffreAffairesSession += montantSaisi;
                facturesPayees++;

                for (int i = 0; i < abonnesDb.size(); i++) {
                    Abonne a = abonnesDb.get(i);
                    if (a.numeroCompteur().equals(numeroCompteur)) {
                        abonnesDb.set(i, new Abonne(a.id(), a.nom(), a.numeroCompteur(), a.categorie(), a.ancienIndex(),
                                StatutFacture.PAYEE.getLibelle()));
                    }
                }
                registrarAction("Encaissement enregistré : " + numeroCompteur + " (+" + String.format("%,.0f", montantSaisi)
                        + " FCFA)");
            }
            return success;
        }, ioExecutor);

        try {
            return fut.get();
        } catch (Exception e) {
            return false;
        }
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
            throw new IllegalArgumentException("Index inférieur à l'ancien !");
        }

        double prixKwh = abonne.categorie().equalsIgnoreCase(CategorieAbonne.SOCIAL.getLibelle()) ? 50.0 : 113.0;
        double montantCalcule = conso * prixKwh;

        // serialize mutation through executor to keep invariants
        ioExecutor.execute(() -> {
            facturesDb.add(0,
                new FactureEnAttente(abonne.nom(), abonne.numeroCompteur(), abonne.categorie(), conso, montantCalcule));

            totalFactures++;

            for (int i = 0; i < abonnesDb.size(); i++) {
            if (abonnesDb.get(i).numeroCompteur().equals(numeroCompteur)) {
                Abonne old = abonnesDb.get(i);
                abonnesDb.set(i, new Abonne(old.id(), old.nom(), old.numeroCompteur(), old.categorie(),
                    old.ancienIndex(), StatutFacture.IMPAYEE.getLibelle()));
            }
            }

            registrarAction("Index validé pour le " + abonne.numeroCompteur() + " (+" + conso + " kWh)");
        });

        return new ReleveSession(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), abonne.nom(),
            abonne.numeroCompteur(), conso);
    }

    @Override
    public Abonne saveAbonne(Abonne nouvelAbonne) {
        abonnesDb.add(0, nouvelAbonne);
        abonnesInscritsCeMois++;
        registrarAction("Nouvel abonné créé : " + nouvelAbonne.nom() + " (" + nouvelAbonne.numeroCompteur() + ")");
        return nouvelAbonne;
    }

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
        return new ArrayList<>(recentActions);
    }

    @Override
    public DashboardStats getDashboardStats() {
        int totalAbonnes = abonnesDb.size();

        // Mise à jour de la dernière barre du graphique avec le CA en cours
        encaissementsSemaine[6] = (int) chiffreAffairesSession;

        return new DashboardStats(
                chiffreAffairesSession,
                totalFactures, // Valeur dynamique
                facturesPayees, // Valeur dynamique
                totalAbonnes,
                abonnesInscritsCeMois,
                encaissementsSemaine.clone());
    }

    @Override
    public StatusbarInfo getStatusbarInfo() {
        return new StatusbarInfo("J. Dupont", "Juillet 2026", "● CSV Connecté", "v1.0.0 - kilowatch");
    }

    /**
     * Retourne un objet repository (ici même service) pour compatibilité avec la
     * découverte par réflexion dans MainLayout.
     */
    public Object getRepository() {
        return this;
    }

    /**
     * Export CSV asynchrone (utilisé par MainLayout via réflexion). Prend une
     * liste (ignorée, on utilise la liste interne), un callback de progression
     * et un executor.
     */
    public CompletableFuture<Integer> exporterFacturesImpayeesAsync(List<?> ignored, Consumer<Integer> progressCallback, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            // assure dossier
            File dossier = new File("data");
            if (!dossier.exists()) dossier.mkdirs();

            List<FactureEnAttente> impayees = new ArrayList<>(facturesDb);
            if (impayees.isEmpty()) {
                if (progressCallback != null) progressCallback.accept(100);
                return 0;
            }

            File out = new File(dossier, "factures_impayees.csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(out))) {
                writer.write("nom;numeroCompteur;categorie;consommation_kWh;montantTTC_FCFA");
                writer.newLine();

                int total = impayees.size();
                for (int i = 0; i < total; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return 0;
                    }
                    FactureEnAttente f = impayees.get(i);
                    writer.write(f.nomAbonne() + ";" + f.numeroCompteur() + ";" + f.categorie() + ";" + f.consoKwh() + ";" + String.format("%,.0f", f.montantTtc()).replace(',', ' '));
                    writer.newLine();

                    if (progressCallback != null) {
                        int pct = (int) Math.round(((i + 1) / (double) total) * 100);
                        progressCallback.accept(pct);
                    }

                    // slight pause to make progress visible during tests
                    try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return 0; }
                }

                return impayees.size();
            } catch (IOException e) {
                System.err.println("[MockDataService] Erreur export CSV: " + e.getMessage());
                return 0;
            }
        }, executor);
    }

    // allow access to the executor for async operations
    public ExecutorService getIoExecutor() {
        return ioExecutor;
    }
}
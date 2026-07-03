package com.kilowatch.view.data;

import com.kilowatch.model.Abonne; // <-- Import normal de ton modèle métier
import com.kilowatch.model.AbonneIndustriel;
import com.kilowatch.model.AbonneResidentiel;
import com.kilowatch.model.AbonneSocial;
import com.kilowatch.model.Facture;
import com.kilowatch.repositories.AbonneRepository;
import com.kilowatch.repositories.ConfigStore;
import com.kilowatch.service.GestionnaireAbonnes;
import com.kilowatch.service.MoteurFacturation;
import com.kilowatch.view.data.ViewDto.ActionLog;
import com.kilowatch.view.data.ViewDto.DashboardStats;
import com.kilowatch.view.data.ViewDto.FactureEnAttente;
import com.kilowatch.view.data.ViewDto.ReleveSession;
import com.kilowatch.view.data.ViewDto.StatusbarInfo;
import com.kilowatch.view.data.ViewEnum.CategorieAbonne;
import com.kilowatch.view.data.ViewEnum.FiltreAbonne;
import com.kilowatch.view.data.ViewEnum.FiltreCaisse;
import com.kilowatch.view.data.ViewEnum.StatutFacture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KilowatchDataService implements ViewDataService {

    // --- DÉPENDANCES VERS LE VRAI BACKEND ---
    private final GestionnaireAbonnes gestionnaire;
    private final MoteurFacturation moteurFacturation;
    private final AbonneRepository repository;

    // --- VARIABLES DE SESSION (POUR LE DASHBOARD) ---
    private final List<ActionLog> recentActions = new ArrayList<>();
    private int abonnesInscritsSession = 0;
    private int facturesPayeesSession = 0;
    private int facturesGenereesSession = 0;

    // NOUVEAU : On garde en mémoire l'utilisateur connecté
    private String utilisateurCourant = "";

    public KilowatchDataService(GestionnaireAbonnes gestionnaire, MoteurFacturation moteurFacturation,
            AbonneRepository repository) {
        this.gestionnaire = gestionnaire;
        this.moteurFacturation = moteurFacturation;
        this.repository = repository;

        // Initialisation de l'historique
        registrarAction("Démarrage du système connecté au Backend");
    }

    // =========================================================
    // UTILITAIRES : SAUVEGARDE & MAPPING
    // =========================================================

    private void saveState() {
        // Appelle le repository pour persister les données en fichier
        repository.sauvegarder(gestionnaire.getAllAbonnes(), gestionnaire.listerToutesFactures());
    }

    private ViewDto.Abonne mapToAbonneDto(Abonne a) {
        List<Facture> facturesAbonne = gestionnaire.listerFacturesParAbonne(a.getId());

        boolean hasImpayes = facturesAbonne.stream().anyMatch(f -> !f.isPayee());
        boolean hasPayees = facturesAbonne.stream().anyMatch(Facture::isPayee);

        String statut = StatutFacture.EN_ATTENTE.getLibelle();
        if (hasImpayes)
            statut = StatutFacture.IMPAYEE.getLibelle();
        else if (hasPayees)
            statut = StatutFacture.PAYEE.getLibelle();

        // Harmonisation de la catégorie (Le backend renvoie "INDUSTRIE", l'UI attend
        // "Industriel")
        String catLabel = a.getCategorie().equalsIgnoreCase("INDUSTRIE") ? CategorieAbonne.INDUSTRIEL.getLibelle()
                : a.getCategorie().equalsIgnoreCase("SOCIAL") ? CategorieAbonne.SOCIAL.getLibelle()
                        : CategorieAbonne.RESIDENTIEL.getLibelle();

        return new ViewDto.Abonne(a.getId(), a.getNomComplet(), a.getNumeroCompteur(), catLabel,
                (int) a.getDernierIndex(), statut);
    }

    private FactureEnAttente mapToFactureDto(Facture f) {
        Abonne a = gestionnaire.rechercherParId(f.getIdAbonne());

        String catLabel = a.getCategorie().equalsIgnoreCase("INDUSTRIE") ? CategorieAbonne.INDUSTRIEL.getLibelle()
                : a.getCategorie().equalsIgnoreCase("SOCIAL") ? CategorieAbonne.SOCIAL.getLibelle()
                        : CategorieAbonne.RESIDENTIEL.getLibelle();

        return new FactureEnAttente(a.getNomComplet(), a.getNumeroCompteur(), catLabel, (int) f.getConsommationKWh(),
                f.getMontantTTC());
    }

    // =========================================================
    // ABONNÉS
    // =========================================================

    @Override
    public List<ViewDto.Abonne> getAllAbonnes() {
        return getFilteredAbonnes("", FiltreAbonne.FILTER_ALL.name());
    }

    @Override
    public List<ViewDto.Abonne> searchAbonnes(String keyword) {
        return getFilteredAbonnes(keyword, FiltreAbonne.FILTER_ALL.name());
    }

    @Override
    public List<ViewDto.Abonne> getFilteredAbonnes(String query, String filterId) {
        String lowerKw = (query == null) ? "" : query.toLowerCase().trim();

        return gestionnaire.getAllAbonnes().stream()
                .filter(a -> lowerKw.isEmpty() || a.getNomComplet().toLowerCase().contains(lowerKw)
                        || a.getNumeroCompteur().toLowerCase().contains(lowerKw))
                .filter(a -> {
                    if (FiltreAbonne.FILTER_GROS_CONS.name().equals(filterId)) {
                        return a.getCategorie().equalsIgnoreCase("INDUSTRIE")
                                || getConsoEnAttente(a.getNumeroCompteur()) >= 250;
                    }
                    return true;
                })
                .map(this::mapToAbonneDto)
                .collect(Collectors.toList());
    }

    @Override
    public ViewDto.Abonne saveAbonne(ViewDto.Abonne dto) {
        Abonne nouvelAbonne;
        String adresseDefaut = "Non renseignée";

        if (dto.categorie().equalsIgnoreCase(CategorieAbonne.SOCIAL.getLibelle())) {
            nouvelAbonne = new AbonneSocial(dto.id(), dto.nom(), adresseDefaut, dto.numeroCompteur());
        } else if (dto.categorie().equalsIgnoreCase(CategorieAbonne.INDUSTRIEL.getLibelle())) {
            nouvelAbonne = new AbonneIndustriel(dto.id(), dto.nom(), adresseDefaut, dto.numeroCompteur(), 50.0);
        } else {
            nouvelAbonne = new AbonneResidentiel(dto.id(), dto.nom(), adresseDefaut, dto.numeroCompteur());
        }

        gestionnaire.ajouterAbonne(nouvelAbonne);
        abonnesInscritsSession++;

        // Journalisation de l'action
        registrarAction("Nouvel abonné créé : " + dto.nom() + " (" + dto.numeroCompteur() + ")");

        // Sauvegarde de l'état
        saveState();

        return mapToAbonneDto(nouvelAbonne);
    }

    @Override
    public int getConsoEnAttente(String numeroCompteur) {
        return gestionnaire.getFacturesParCompteur(numeroCompteur).stream()
                .filter(f -> !f.isPayee())
                .mapToInt(f -> (int) f.getConsommationKWh())
                .sum();
    }

    // =========================================================
    // CAISSE & FACTURES
    // =========================================================

    @Override
    public List<FactureEnAttente> getFacturesEnAttente() {
        return getFilteredFactures(FiltreCaisse.FILTER_ALL.name(), true);
    }

    @Override
    public List<FactureEnAttente> getFilteredFactures(String filterId, boolean sortDesc) {
        return gestionnaire.listerFacturesImpayees().stream()
                .filter(f -> {
                    Abonne a = gestionnaire.rechercherParId(f.getIdAbonne());
                    if (FiltreCaisse.FILTER_SOCIAL.name().equals(filterId))
                        return a.getCategorie().equalsIgnoreCase("SOCIAL");
                    if (FiltreCaisse.FILTER_RESIDENTIEL.name().equals(filterId))
                        return a.getCategorie().equalsIgnoreCase("RESIDENTIEL");
                    if (FiltreCaisse.FILTER_INDUSTRIEL.name().equals(filterId))
                        return a.getCategorie().equalsIgnoreCase("INDUSTRIE");
                    return true;
                })
                .sorted((f1, f2) -> sortDesc ? Double.compare(f2.getMontantTTC(), f1.getMontantTTC())
                        : Double.compare(f1.getMontantTTC(), f2.getMontantTTC()))
                .map(this::mapToFactureDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean encaisserFacture(String numeroCompteur) {
        Optional<Facture> factureOpt = gestionnaire.getFacturesParCompteur(numeroCompteur).stream()
                .filter(f -> !f.isPayee())
                .findFirst();

        if (factureOpt.isPresent()) {
            Facture f = factureOpt.get();
            f.marquerPayee();

            facturesPayeesSession++;

            registrarAction(
                    "Encaissement : " + numeroCompteur + " (+" + String.format("%,.0f", f.getMontantTTC()) + " FCFA)");

            // Sauvegarde de l'état
            saveState();

            return true;
        }
        return false;
    }

    // =========================================================
    // RELEVÉS
    // =========================================================

    @Override
    public ViewDto.Abonne findAbonneForReleve(String query) {
        return gestionnaire.findAbonneParCompteurOuNom(query)
                .map(this::mapToAbonneDto)
                .orElse(null);
    }

    @Override
    public ReleveSession validerNouvelIndex(String numeroCompteur, int nouvelIndex) throws IllegalArgumentException {
        Abonne vraiAbonne = gestionnaire.findAbonneParCompteurOuNom(numeroCompteur)
                .orElseThrow(() -> new IllegalArgumentException("Compteur introuvable."));

        // Le moteur génère la vraie facture en BDD !
        Facture nouvelleFacture = moteurFacturation.saisirReleveEtFacturer(vraiAbonne.getId(), nouvelIndex, "TECH-001");

        facturesGenereesSession++;

        registrarAction(
                "Index validé pour " + numeroCompteur + " (+" + (int) nouvelleFacture.getConsommationKWh() + " kWh)");

        // Sauvegarde de l'état
        saveState();

        return new ReleveSession(
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                vraiAbonne.getNomComplet(),
                vraiAbonne.getNumeroCompteur(),
                (int) nouvelleFacture.getConsommationKWh());
    }

    // =========================================================
    // NOUVEAU : AUTHENTIFICATION
    // =========================================================

    @Override
    public boolean authentifier(String username, String password) {
        // On interroge notre fourre-tout typé
        String expectedUser = ConfigStore.data.auth.username;
        String expectedPass = ConfigStore.data.auth.password;

        if (username.equals(expectedUser) && password.equals(expectedPass)) {
            this.utilisateurCourant = username;
            registrarAction("Connexion réussie : " + username);
            return true;
        }

        registrarAction("Tentative de connexion échouée (" + username + ")");
        return false;
    }

    @Override
    public void deconnecter() {
        registrarAction("Déconnexion de l'utilisateur : " + this.utilisateurCourant);
        this.utilisateurCourant = "";
    }

    // =========================================================
    // DASHBOARD & STATUS BAR & LOGS
    // =========================================================

    @Override
    public DashboardStats getDashboardStats() {
        int totalAbonnes = gestionnaire.nombreAbonnes();

        // 1. CALCUL DYNAMIQUE DU VRAI CHIFFRE D'AFFAIRES (Toutes les factures payées en
        // BDD)
        double vraiChiffreAffaires = gestionnaire.calculerRecetteEncaisseeDuMois();

        // Calcul dynamique des encaissements sur les 7 derniers jours
        int[] historique = new int[7];
        LocalDate aujourdHui = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate jourCible = aujourdHui.minusDays(6 - i);
            double totalJour = gestionnaire.listerFacturesPayees().stream()
                    .filter(f -> f.getDateEmission().equals(jourCible))
                    .mapToDouble(Facture::getMontantTTC)
                    .sum();
            historique[i] = (int) totalJour;
        }

        // On injecte le vrai chiffre d'affaires calculé au lieu de la variable de
        // session
        return new DashboardStats(
                vraiChiffreAffaires,
                gestionnaire.listerToutesFactures().size(),
                gestionnaire.listerFacturesPayees().size(),
                totalAbonnes,
                abonnesInscritsSession,
                historique);
    }

    @Override
    public void registrarAction(String description) {
        String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        recentActions.add(0, new ActionLog(heure, description));
        // On ne garde que les 10 dernières actions pour l'UI
        if (recentActions.size() > 10) {
            recentActions.remove(recentActions.size() - 1);
        }
    }

    @Override
    public List<ActionLog> getRecentActions() {
        // Retourne une copie de la liste pour éviter des modifications externes
        return new ArrayList<>(recentActions);
    }

    @Override
    public StatusbarInfo getStatusbarInfo() {
        boolean isConnected = !utilisateurCourant.isEmpty() && utilisateurCourant != null;
        return new StatusbarInfo(
                !isConnected ? "Déconnecté" : utilisateurCourant,
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                isConnected ? "● Connecté (Live)" : "● Déconnecté",
                "v2.0.0 - kilowatch");
    }

    // =========================================================
    // EXPORT CSV ASYNCHRONE
    // =========================================================
    @Override
    public java.util.concurrent.CompletableFuture<Integer> exporterFacturesImpayees(java.io.File destination,
            java.util.function.Consumer<Integer> progressCallback) {
        return repository.exporterFacturesImpayeesAsync(
                destination,
                gestionnaire.listerToutesFactures(),
                progressCallback,
                java.util.concurrent.ForkJoinPool.commonPool());
    }
}
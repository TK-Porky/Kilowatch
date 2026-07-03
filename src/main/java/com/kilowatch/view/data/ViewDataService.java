package com.kilowatch.view.data;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.kilowatch.view.data.ViewDto.*;

public interface ViewDataService {

    // --- VUE STATUSBAR ---
    StatusbarInfo getStatusbarInfo();

    // --- VUE ABONNÉS ---
    List<Abonne> getAllAbonnes();

    List<Abonne> getFilteredAbonnes(String query, String filterId);

    List<Abonne> searchAbonnes(String keyword);

    Abonne saveAbonne(Abonne nouvelAbonne);

    int getConsoEnAttente(String numeroCompteur);

    // --- VUE CAISSE ---
    List<FactureEnAttente> getFacturesEnAttente();

    boolean encaisserFacture(String numeroCompteur);

    List<FactureEnAttente> getFilteredFactures(String filterId, boolean sortDesc);

    // --- VUE RELEVÉS ---
    Abonne findAbonneForReleve(String query);

    ReleveSession validerNouvelIndex(String numeroCompteur, int nouvelIndex) throws IllegalArgumentException;

    // --- VUE DASHBOARD ---
    void registrarAction(String description);

    List<ActionLog> getRecentActions();

    DashboardStats getDashboardStats();

    // --- VUE EXPORT ---
    /**
     * Lance l'exportation asynchrone des factures impayées au format CSV.
     * 
     * @param progressCallback Reçoit le pourcentage d'avancement (0 à 100)
     * @return Un CompletableFuture contenant le nombre de factures exportées
     */
    CompletableFuture<Integer> exporterFacturesImpayees(File destination, Consumer<Integer> progressCallback);

    // --- VUE SECURITÉ ---
    boolean authentifier(String username, String password);

    void deconnecter();
}
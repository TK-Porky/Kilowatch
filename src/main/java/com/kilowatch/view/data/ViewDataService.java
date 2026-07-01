package com.kilowatch.view.data;

import java.util.List;

import com.kilowatch.view.data.ViewDto.Abonne;
import com.kilowatch.view.data.ViewDto.ActionLog;
import com.kilowatch.view.data.ViewDto.DashboardStats;
import com.kilowatch.view.data.ViewDto.FactureEnAttente;
import com.kilowatch.view.data.ViewDto.ReleveSession;

public interface ViewDataService {

    // --- VUE ABONNÉS ---
    List<Abonne> getAllAbonnes();

    List<Abonne> searchAbonnes(String keyword);

    // Permet de sauvegarder le nouvel abonné créé via le formulaire
    Abonne saveAbonne(Abonne nouvelAbonne);

    // --- VUE CAISSE ---
    List<FactureEnAttente> getFacturesEnAttente();

    boolean encaisserFacture(String numeroCompteur);

    // --- VUE RELEVÉS ---
    // Renvoie l'abonné si trouvé, ou lance une exception / renvoie null si
    // introuvable
    Abonne findAbonneForReleve(String query);

    // Enregistre le nouvel index et renvoie le relevé généré pour mettre à jour
    // l'historique UI
    ReleveSession validerNouvelIndex(String numeroCompteur, int nouvelIndex) throws IllegalArgumentException;

    // --- VUE DASHBOARD ---
    void registrarAction(String description);

    List<ActionLog> getRecentActions();

    DashboardStats getDashboardStats();
}
package com.kilowatch.service;

import com.kilowatch.exception.AbonneNotFoundException;
import com.kilowatch.model.Abonne;
import com.kilowatch.model.Facture;
import com.kilowatch.model.Releve;
import com.kilowatch.util.IdGenerator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Algorithme central de facturation.
 * Reçoit une relève, retrouve l'abonné correspondant, et délègue
 * le calcul du montant à l'implémentation polymorphe de chaque
 * sous-classe d'Abonne (Social, Résidentiel, Industriel).
 */
public class MoteurFacturation {

    private final GestionnaireAbonnes gestionnaire;

    public MoteurFacturation(GestionnaireAbonnes gestionnaire) {
        this.gestionnaire = gestionnaire;
    }

    /**
     * Génère une facture à partir d'une nouvelle relève.
     *
     * @param releve la relève saisie par le technicien
     * @return la facture générée et enregistrée
     * @throws AbonneNotFoundException si l'abonné n'existe pas
     */
    public Facture genererFacture(Releve releve) {
        Abonne abonne = gestionnaire.rechercherParId(releve.getIdAbonne());

        abonne.ajouterReleve(releve);

        String idFacture = IdGenerator.nextFactureId();
        Facture facture = new Facture(idFacture, abonne, releve);
        gestionnaire.ajouterFacture(facture);

        return facture;
    }

    /**
     * Raccourci : crée la relève ET génère la facture en une seule étape,
     * en se basant automatiquement sur le dernier index connu de l'abonné.
     */
    public Facture saisirReleveEtFacturer(String idAbonne, double nouvelIndex, String idTechnicien) {
        Abonne abonne = gestionnaire.rechercherParId(idAbonne);
        double indexPrecedent = abonne.getDernierIndex();

        Releve releve = new Releve(
                IdGenerator.nextReleveId(),
                idAbonne,
                indexPrecedent,
                nouvelIndex,
                idTechnicien
        );

        return genererFacture(releve);
    }

    /**
     * Facturation en masse : applique la relève à plusieurs abonnés
     * d'une même catégorie en une seule opération (utile pour une
     * campagne de relève mensuelle).
     */
    public List<Facture> facturerLot(List<Releve> releves) {
        return releves.stream()
                .map(this::genererFacture)
                .collect(Collectors.toList());
    }
}
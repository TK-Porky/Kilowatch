package com.kilowatch.view.data;

public class ViewDto {
    // Ces classes seront instanciées par l'équipe métier et lues par ton UI
    public record Abonne(
            String id,
            String nom,
            String numeroCompteur,
            String categorie,
            int ancienIndex,
            String statut) {
    }

    public record FactureEnAttente(
            String nomAbonne,
            String numeroCompteur,
            String categorie,
            int consoKwh,
            double montantTtc) {
    }

    public record ReleveSession(
            String heure,
            String nomAbonne,
            String numeroCompteur,
            int consoKwh) {
    }

    // Représente l'état complet des compteurs du Dashboard à un instant T
    public record DashboardStats(
            double chiffreAffaires,
            int totalFactures,
            int facturesPayees,
            int totalAbonnes,
            int abonnesInscrits,
            int[] encaissements7DerniersJours // Le tableau de taille 7 sécurisé !
    ) {
    }

    // Représente une ligne d'action historique
    public record ActionLog(
            String horodatage,
            String description) {
    }

    public record StatusbarInfo(
            String nomAgent,
            String periodeActive,
            String statutConnexion,
            String version) {
    }
}

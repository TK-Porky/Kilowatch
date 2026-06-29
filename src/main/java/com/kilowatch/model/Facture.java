package com.kilowatch.model;

import com.kilowatch.interfaces.Identifiable;

import java.io.Serializable;
import java.time.LocalDate;

public class Facture implements Identifiable, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String idAbonne;
    private final String idReleve;
    private final String categorieAbonne;
    private final double consommationKWh;
    private final double montantHT;
    private final double montantTVA;
    private final double montantTTC;
    private final LocalDate dateEmission;
    private boolean payee;

    public Facture(String id, Abonne abonne, Releve releve) {
        this.id = id;
        this.idAbonne = abonne.getId();
        this.idReleve = releve.getId();
        this.categorieAbonne = abonne.getCategorie();
        this.consommationKWh = releve.getConsommation();

        this.montantHT = abonne.calculerMontantHT(consommationKWh);
        this.montantTVA = abonne.calculerMontantTVA(consommationKWh);
        this.montantTTC = abonne.calculerMontantTTC(consommationKWh);

        this.dateEmission = LocalDate.now();
        this.payee = false;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getIdAbonne() {
        return idAbonne;
    }

    public String getIdReleve() {
        return idReleve;
    }

    public String getCategorieAbonne() {
        return categorieAbonne;
    }

    public double getConsommationKWh() {
        return consommationKWh;
    }

    public double getMontantHT() {
        return montantHT;
    }

    public double getMontantTVA() {
        return montantTVA;
    }

    public double getMontantTTC() {
        return montantTTC;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public boolean isPayee() {
        return payee;
    }

    public void marquerPayee() {
        this.payee = true;
    }

    @Override
    public String toString() {
        return String.format(
                "Facture %s%n" +
                "Abonné       : %s (%s)%n" +
                "Consommation : %.2f kWh%n" +
                "Montant HT   : %.2f FCFA%n" +
                "TVA          : %.2f FCFA%n" +
                "Montant TTC  : %.2f FCFA%n" +
                "Date         : %s%n" +
                "Statut       : %s",
                id, idAbonne, categorieAbonne, consommationKWh,
                montantHT, montantTVA, montantTTC, dateEmission,
                payee ? "Payée" : "Impayée");
    }
}
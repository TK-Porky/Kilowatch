package com.kilowatch.model;

import com.kilowatch.util.TarifConstantes;

public class AbonneSocial extends Abonne {

    public AbonneSocial(String id, String nomComplet, String adresse, String numeroCompteur) {
        super(id, nomComplet, adresse, numeroCompteur);
    }

    @Override
    public String getCategorie() {
        return "SOCIAL";
    }

    @Override
    public double calculerMontantHT(double consommationKWh) {
        return consommationKWh * TarifConstantes.PRIX_KWH_SOCIAL;
    }

    @Override
    public double getTauxTVA() {
        return TarifConstantes.TVA_SOCIAL;
    }
}
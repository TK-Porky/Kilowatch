package com.kilowatch.model;

import com.kilowatch.util.TarifConstantes;

public class AbonneResidentiel extends Abonne {

    public AbonneResidentiel(String id, String nomComplet, String adresse, String numeroCompteur) {
        super(id, nomComplet, adresse, numeroCompteur);
    }

    @Override
    public String getCategorie() {
        return "RESIDENTIEL";
    }

    @Override
    public double calculerMontantHT(double consommationKWh) {
        double seuil = TarifConstantes.SEUIL_RESIDENTIEL_T1;

        if (consommationKWh <= seuil) {
            return consommationKWh * TarifConstantes.PRIX_KWH_RESIDENTIEL_T1;
        }

        double montantTranche1 = seuil * TarifConstantes.PRIX_KWH_RESIDENTIEL_T1;
        double montantTranche2 = (consommationKWh - seuil) * TarifConstantes.PRIX_KWH_RESIDENTIEL_T2;
        return montantTranche1 + montantTranche2;
    }

    @Override
    public double getTauxTVA() {
        return TarifConstantes.TVA_STANDARD;
    }
}
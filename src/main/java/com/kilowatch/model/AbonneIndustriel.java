package com.kilowatch.model;

import com.kilowatch.util.TarifConstantes;

public class AbonneIndustriel extends Abonne {

    private double puissanceSouscriteKVA;

    public AbonneIndustriel(String id, String nomComplet, String adresse,
                             String numeroCompteur, double puissanceSouscriteKVA) {
        super(id, nomComplet, adresse, numeroCompteur);
        this.puissanceSouscriteKVA = puissanceSouscriteKVA;
    }

    public double getPuissanceSouscriteKVA() {
        return puissanceSouscriteKVA;
    }

    public void setPuissanceSouscriteKVA(double puissanceSouscriteKVA) {
        this.puissanceSouscriteKVA = puissanceSouscriteKVA;
    }

    @Override
    public String getCategorie() {
        return "INDUSTRIE";
    }

    @Override
    public double calculerMontantHT(double consommationKWh) {
        double montantEnergie = consommationKWh * TarifConstantes.PRIX_KWH_INDUSTRIEL;
        double primeFixe = puissanceSouscriteKVA * TarifConstantes.PRIME_PUISSANCE_KVA;
        return montantEnergie + primeFixe;
    }

    @Override
    public double getTauxTVA() {
        return TarifConstantes.TVA_STANDARD;
    }
}
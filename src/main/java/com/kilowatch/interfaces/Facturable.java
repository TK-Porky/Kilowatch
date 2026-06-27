package com.kilowatch.interfaces;

public interface Facturable {

    double calculerMontantHT(double consommationKWh);

    double getTauxTVA();

    default double calculerMontantTVA(double consommationKWh) {
        return calculerMontantHT(consommationKWh) * getTauxTVA() / 100.0;
    }

    default double calculerMontantTTC(double consommationKWh) {
        return calculerMontantHT(consommationKWh) + calculerMontantTVA(consommationKWh);
    }
}
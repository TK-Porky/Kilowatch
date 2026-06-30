package models;

public class AbonneIndustriel extends Abonne {
    // Tarif ENEO pour les industries (ex: 95 FCFA le kWh)
    private static final double TARIF_KWH = 95.0; 

    public AbonneIndustriel(String idAbonne, String nom, double ancienIndex) {
        super(idAbonne, nom, ancienIndex);
    }

    @Override
    public double calculerMontantHT() {
        return getConsommation() * TARIF_KWH;
    }
}
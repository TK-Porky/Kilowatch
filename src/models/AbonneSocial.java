package models;

public class AbonneSocial extends Abonne {
    // Tarif ENEO pour la tranche sociale (ex: 50 FCFA le kWh)
    private static final double TARIF_KWH = 50.0; 

    public AbonneSocial(String idAbonne, String nom, double ancienIndex) {
        super(idAbonne, nom, ancienIndex);
    }

    @Override
    public double calculerMontantHT() {
        return getConsommation() * TARIF_KWH;
    }
}
package models;

public class AbonneResidentiel extends Abonne {
    // Tarif ENEO pour la tranche résidentielle classique (ex: 79 FCFA le kWh)
    private static final double TARIF_KWH = 79.0; 

    public AbonneResidentiel(String idAbonne, String nom, double ancienIndex) {
        super(idAbonne, nom, ancienIndex);
    }

    @Override
    public double calculerMontantHT() {
        return getConsommation() * TARIF_KWH;
    }
}
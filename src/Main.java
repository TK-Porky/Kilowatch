import exceptions.IndexIncoherentException;
import models.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== TEST DE LA PLATEFORME KILOWATCH (ENEO) ===");

        // 1. Initialisation du gestionnaire de l'équipe Core & Métier
        GestionnaireMetier gestionnaire = new GestionnaireMetier();

        // 2. Création de trois abonnés différents (Ancien index à 1000 kWh)
        Abonne social = new AbonneSocial("AB01", "Client Social", 1000);
        Abonne residentiel = new AbonneResidentiel("AB02", "Client Residentiel", 1000);
        Abonne industriel = new AbonneIndustriel("AB03", "Client Industriel", 1000);

        // 3. Simulation des relevés des index par le technicien
        try {
            social.setNouvelIndex(1150);       // Consommation : 150 kWh
            residentiel.setNouvelIndex(1600);  // Consommation : 600 kWh (Gros consommateur)
            industriel.setNouvelIndex(1200);   // Consommation : 200 kWh

            // Ajout dans notre collection
            gestionnaire.ajouterAbonne(social);
            gestionnaire.ajouterAbonne(residentiel);
            gestionnaire.ajouterAbonne(industriel);

            System.out.println("\n--- Liste des abonnés après saisie correcte : ---");
            for (Abonne a : gestionnaire.getListeAbonnes()) {
                System.out.println(a.getNom() + " | Consommation : " + a.getConsommation() 
                    + " kWh | Facture TTC : " + String.format("%.2f", a.calculerMontantTTC()) + " FCFA");
            }

        } catch (IndexIncoherentException e) {
            System.out.println("\n[ERREUR CAPTURÉE] : " + e.getMessage());
        }

        // 4. Test des algorithmes fondamentaux (Streams et Lambdas)
        System.out.println("\n--- Tri des factures par montant décroissant (Stream.sorted) : ---");
        List<Abonne> listeTriee = gestionnaire.trierAbonnesParFactureDecroissante();
        listeTriee.forEach(a -> System.out.println(a.getNom() + " : " + String.format("%.2f", a.calculerMontantTTC()) + " FCFA"));

        System.out.println("\n--- Filtrage des gros consommateurs > 500 kWh (Stream.filter) : ---");
        List<Abonne> grosConsommateurs = gestionnaire.filtrerGrosConsommateurs(500);
        grosConsommateurs.forEach(a -> System.out.println(a.getNom() + " a consommé " + a.getConsommation() + " kWh"));

        // 5. TEST DE LA SÉCURITÉ : Tentative de saisie d'un index incohérent
        System.out.println("\n--- Test du système de sécurité (Index Incohérent) : ---");
        try {
            System.out.println("Tentative de passer l'index du client Social de 1150 à 900...");
            social.setNouvelIndex(900); // Devrait lever l'exception
        } catch (IndexIncoherentException e) {
            System.out.println("[SUCCÈS DU TEST DE SÉCURITÉ] L'exception a bien bloqué l'application : " + e.getMessage());
        }
    }
}
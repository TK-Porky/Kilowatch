package models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GestionnaireMetier {
    // La collection qui stocke tous les abonnés d'ENEO
    private List<Abonne> listeAbonnes;

    // Constructeur
    public GestionnaireMetier() {
        this.listeAbonnes = new ArrayList<>();
    }

    // Méthode pour ajouter un abonné dans la collection
    public void ajouterAbonne(Abonne abonne) {
        this.listeAbonnes.add(abonne);
    }

    // Getter pour récupérer toute la liste
    public List<Abonne> getListeAbonnes() {
        return this.listeAbonnes;
    }

    /**
     * TRAITEMENT 1 : Trier les abonnés par montant de facture décroissant
     * Utilisation de l'API Stream et d'une expression Lambda
     */
    public List<Abonne> trierAbonnesParFactureDecroissante() {
        return this.listeAbonnes.stream()
            // Lambda : on compare les montants TTC de manière inversée (décroissante)
            .sorted((a1, a2) -> Double.compare(a2.calculerMontantTTC(), a1.calculerMontantTTC()))
            .collect(Collectors.toList());
    }

    /**
     * TRAITEMENT 2 : Filtrer les gros consommateurs
     * On considère par exemple qu'un gros consommateur consomme plus de 500 kWh
     */
    public List<Abonne> filtrerGrosConsommateurs(double seuilKWh) {
        return this.listeAbonnes.stream()
            // Lambda : on garde uniquement ceux qui dépassent le seuil
            .filter(abonne -> abonne.getConsommation() > seuilKWh)
            .collect(Collectors.toList());
    }
}
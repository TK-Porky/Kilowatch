package models;

import exceptions.IndexIncoherentException;

public abstract class Abonne {
    private String idAbonne;
    private String nom;
    private double ancienIndex;
    private double nouvelIndex;

    // Constructeur
    public Abonne(String idAbonne, String nom, double ancienIndex) {
        this.idAbonne = idAbonne;
        this.nom = nom;
        this.ancienIndex = ancienIndex;
        this.nouvelIndex = ancienIndex; 
    }

    // Getters et Setters (Encapsulation stricte)
    public String getIdAbonne() { return idAbonne; }
    public void setIdAbonne(String idAbonne) { this.idAbonne = idAbonne; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getAncienIndex() { return ancienIndex; }
    public void setAncienIndex(double ancienIndex) { this.ancienIndex = ancienIndex; }

    public double getNouvelIndex() { return nouvelIndex; }

    // Contrôle de l'index avec levée d'exception
    public void setNouvelIndex(double nouvelIndex) throws IndexIncoherentException {
        if (nouvelIndex < this.ancienIndex) {
            throw new IndexIncoherentException("Index incohérent : " + nouvelIndex + " est inférieur à l'ancien (" + this.ancienIndex + ").");
        }
        this.nouvelIndex = nouvelIndex;
    }

    public double getConsommation() {
        return this.nouvelIndex - this.ancienIndex;
    }

    // Méthode abstraite pour le calcul des tarifs ENEO
    public abstract double calculerMontantHT();

    public double calculerMontantTTC() {
        double ht = calculerMontantHT();
        return ht + (ht * 0.1925); // TVA de 19.25%
    }
}
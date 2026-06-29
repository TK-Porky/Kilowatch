package com.kilowatch.model;

import com.kilowatch.interfaces.Facturable;
import com.kilowatch.interfaces.Identifiable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Abonne implements Identifiable, Facturable, Serializable {

    private static final long serialVersionUID = 1L;

    protected final String id;
    protected String nomComplet;
    protected String adresse;
    protected String numeroCompteur;
    protected final List<Releve> historiqueReleves;

    protected Abonne(String id, String nomComplet, String adresse, String numeroCompteur) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.adresse = adresse;
        this.numeroCompteur = numeroCompteur;
        this.historiqueReleves = new ArrayList<>();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumeroCompteur() {
        return numeroCompteur;
    }

    public List<Releve> getHistoriqueReleves() {
        return Collections.unmodifiableList(historiqueReleves);
    }

    public void ajouterReleve(Releve releve) {
        historiqueReleves.add(releve);
    }

    /**
     * Renvoie le dernier index relevé (0 si aucune relève n'existe encore).
     */
    public double getDernierIndex() {
        if (historiqueReleves.isEmpty()) {
            return 0.0;
        }
        return historiqueReleves.get(historiqueReleves.size() - 1).getIndexActuel();
    }

    public abstract String getCategorie();

    @Override
    public String toString() {
        return String.format("[%s] %s - %s | Compteur: %s | Adresse: %s",
                getCategorie(), id, nomComplet, numeroCompteur, adresse);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Abonne)) return false;
        return id.equals(((Abonne) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
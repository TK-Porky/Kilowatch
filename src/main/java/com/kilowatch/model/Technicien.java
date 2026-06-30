package com.kilowatch.model;

import com.kilowatch.interfaces.Identifiable;
import java.io.Serializable;

import java.io.Serializable;

public class Technicien implements Identifiable, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String nom;
    private String zoneAffectation;

    public Technicien(String id, String nom, String zoneAffectation) {
        this.id = id;
        this.nom = nom;
        this.zoneAffectation = zoneAffectation;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getZoneAffectation() {
        return zoneAffectation;
    }

    public void setZoneAffectation(String zoneAffectation) {
        this.zoneAffectation = zoneAffectation;
    }

    @Override
    public String toString() {
        return String.format("Technicien %s - %s (Zone: %s)", id, nom, zoneAffectation);
    }
}
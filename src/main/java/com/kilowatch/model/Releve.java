package com.kilowatch.model;

import com.kilowatch.exception.ReleveInvalideException;
import com.kilowatch.interfaces.Identifiable;

import java.io.Serializable;
import java.time.LocalDate;

public class Releve implements Identifiable, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String idAbonne;
    private final double indexPrecedent;
    private final double indexActuel;
    private final LocalDate dateReleve;
    private final String idTechnicien;

    public Releve(String id, String idAbonne, double indexPrecedent,
                   double indexActuel, String idTechnicien) {

        if (indexActuel < indexPrecedent) {
            throw new ReleveInvalideException(
                    "L'index actuel (" + indexActuel +
                    ") ne peut pas être inférieur à l'index précédent (" + indexPrecedent + ").");
        }

        this.id = id;
        this.idAbonne = idAbonne;
        this.indexPrecedent = indexPrecedent;
        this.indexActuel = indexActuel;
        this.idTechnicien = idTechnicien;
        this.dateReleve = LocalDate.now();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getIdAbonne() {
        return idAbonne;
    }

    public double getIndexPrecedent() {
        return indexPrecedent;
    }

    public double getIndexActuel() {
        return indexActuel;
    }

    public LocalDate getDateReleve() {
        return dateReleve;
    }

    public String getIdTechnicien() {
        return idTechnicien;
    }

    public double getConsommation() {
        return indexActuel - indexPrecedent;
    }

    @Override
    public String toString() {
        return String.format("Relève %s | Abonné: %s | %.2f -> %.2f kWh | Conso: %.2f kWh | Date: %s",
                id, idAbonne, indexPrecedent, indexActuel, getConsommation(), dateReleve);
    }
}
package com.kilowatch.service;

import com.kilowatch.exception.AbonneDejaExistantException;
import com.kilowatch.exception.AbonneNotFoundException;
import com.kilowatch.model.Abonne;
import com.kilowatch.model.Facture;

import java.util.*;
import java.util.stream.Collectors;

public class GestionnaireAbonnes {

    private final Map<String, Abonne> abonnes = new LinkedHashMap<>();
    private final List<Facture> factures = new ArrayList<>();

    // ---------- CRUD ABONNES ----------

    public void ajouterAbonne(Abonne abonne) {
        if (abonnes.containsKey(abonne.getId())) {
            throw new AbonneDejaExistantException(abonne.getId());
        }
        abonnes.put(abonne.getId(), abonne);
    }

    public Abonne rechercherParId(String id) {
        Abonne abonne = abonnes.get(id);
        if (abonne == null) {
            throw new AbonneNotFoundException(id);
        }
        return abonne;
    }

    public Optional<Abonne> rechercherParIdOptionnel(String id) {
        return Optional.ofNullable(abonnes.get(id));
    }

    public boolean supprimerAbonne(String id) {
        return abonnes.remove(id) != null;
    }

    public void modifierAbonne(String id, String nouveauNom, String nouvelleAdresse) {
        Abonne abonne = rechercherParId(id);
        abonne.setNomComplet(nouveauNom);
        abonne.setAdresse(nouvelleAdresse);
    }

    public List<Abonne> listerTous() {
        return new ArrayList<>(abonnes.values());
    }

    public List<Abonne> filtrerParCategorie(String categorie) {
        return abonnes.values().stream()
                .filter(a -> a.getCategorie().equalsIgnoreCase(categorie))
                .collect(Collectors.toList());
    }

    public List<Abonne> rechercherParNom(String motCle) {
        String recherche = motCle.toLowerCase();
        return abonnes.values().stream()
                .filter(a -> a.getNomComplet().toLowerCase().contains(recherche))
                .collect(Collectors.toList());
    }

    public int nombreAbonnes() {
        return abonnes.size();
    }

    // ---------- GESTION FACTURES ----------

    public void ajouterFacture(Facture facture) {
        factures.add(facture);
    }

    public List<Facture> listerToutesFactures() {
        return new ArrayList<>(factures);
    }

    public List<Facture> listerFacturesParAbonne(String idAbonne) {
        return factures.stream()
                .filter(f -> f.getIdAbonne().equals(idAbonne))
                .collect(Collectors.toList());
    }

    public List<Facture> listerFacturesImpayees() {
        return factures.stream()
                .filter(f -> !f.isPayee())
                .collect(Collectors.toList());
    }

    public List<Facture> listerFacturesPayees() {
        return factures.stream()
                .filter(Facture::isPayee)
                .collect(Collectors.toList());
    }

    public Optional<Facture> rechercherFactureParId(String idFacture) {
        return factures.stream()
                .filter(f -> f.getId().equals(idFacture))
                .findFirst();
    }

    // ---------- STATISTIQUES ----------

    public double calculerRecetteTotale() {
        return factures.stream()
                .mapToDouble(Facture::getMontantTTC)
                .sum();
    }

    public double calculerRecetteEncaissee() {
        return factures.stream()
                .filter(Facture::isPayee)
                .mapToDouble(Facture::getMontantTTC)
                .sum();
    }

    public double calculerRecetteEnAttente() {
        return factures.stream()
                .filter(f -> !f.isPayee())
                .mapToDouble(Facture::getMontantTTC)
                .sum();
    }

    public Map<String, Double> recetteParCategorie() {
        return factures.stream()
                .collect(Collectors.groupingBy(
                        Facture::getCategorieAbonne,
                        Collectors.summingDouble(Facture::getMontantTTC)
                ));
    }

    public Map<String, Long> nombreAbonnesParCategorie() {
        return abonnes.values().stream()
                .collect(Collectors.groupingBy(Abonne::getCategorie, Collectors.counting()));
    }

    public double consommationTotaleKWh() {
        return factures.stream()
                .mapToDouble(Facture::getConsommationKWh)
                .sum();
    }
}
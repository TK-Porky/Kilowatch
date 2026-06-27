package com.kilowatch.service;

import com.kilowatch.exception.AbonneDejaExistantException;
import com.kilowatch.exception.AbonneNotFoundException;
import com.kilowatch.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GestionnaireAbonnesTest {

    private GestionnaireAbonnes gestionnaire;

    @BeforeEach
    void setUp() {
        gestionnaire = new GestionnaireAbonnes();
        gestionnaire.ajouterAbonne(new AbonneSocial("AB001", "Marie Etoa", "Bafoussam", "CPT-1001"));
        gestionnaire.ajouterAbonne(new AbonneResidentiel("AB002", "Jean Mballa", "Yaoundé", "CPT-2002"));
        gestionnaire.ajouterAbonne(new AbonneIndustriel("AB003", "Usine X", "Douala", "CPT-3003", 10));
    }

    @Test
    void testAjouterEtRechercherAbonne() {
        Abonne a = gestionnaire.rechercherParId("AB001");
        assertEquals("Marie Etoa", a.getNomComplet());
    }

    @Test
    void testAjouterAbonneDejaExistantLeveException() {
        Abonne doublon = new AbonneSocial("AB001", "Autre Nom", "Adresse", "CPT-9999");
        assertThrows(AbonneDejaExistantException.class, () -> gestionnaire.ajouterAbonne(doublon));
    }

    @Test
    void testRechercherAbonneInexistantLeveException() {
        assertThrows(AbonneNotFoundException.class, () -> gestionnaire.rechercherParId("INEXISTANT"));
    }

    @Test
    void testSupprimerAbonne() {
        assertTrue(gestionnaire.supprimerAbonne("AB001"));
        assertThrows(AbonneNotFoundException.class, () -> gestionnaire.rechercherParId("AB001"));
    }

    @Test
    void testSupprimerAbonneInexistant() {
        assertFalse(gestionnaire.supprimerAbonne("INEXISTANT"));
    }

    @Test
    void testListerTous() {
        List<Abonne> tous = gestionnaire.listerTous();
        assertEquals(3, tous.size());
    }

    @Test
    void testFiltrerParCategorie() {
        List<Abonne> sociaux = gestionnaire.filtrerParCategorie("SOCIAL");
        assertEquals(1, sociaux.size());
        assertEquals("AB001", sociaux.get(0).getId());
    }

    @Test
    void testRechercherParNom() {
        List<Abonne> resultats = gestionnaire.rechercherParNom("mballa");
        assertEquals(1, resultats.size());
        assertEquals("AB002", resultats.get(0).getId());
    }

    @Test
    void testModifierAbonne() {
        gestionnaire.modifierAbonne("AB001", "Marie Etoa Modifiée", "Nouvelle Adresse");
        Abonne a = gestionnaire.rechercherParId("AB001");
        assertEquals("Marie Etoa Modifiée", a.getNomComplet());
        assertEquals("Nouvelle Adresse", a.getAdresse());
    }

    @Test
    void testRecetteTotaleEtParCategorie() {
        Releve r1 = new Releve("REL001", "AB001", 0, 50, "TECH01");
        Facture f1 = new Facture("FACT001", gestionnaire.rechercherParId("AB001"), r1);
        gestionnaire.ajouterFacture(f1);

        Releve r2 = new Releve("REL002", "AB002", 0, 100, "TECH01");
        Facture f2 = new Facture("FACT002", gestionnaire.rechercherParId("AB002"), r2);
        gestionnaire.ajouterFacture(f2);

        double total = gestionnaire.calculerRecetteTotale();
        assertEquals(f1.getMontantTTC() + f2.getMontantTTC(), total, 0.01);

        Map<String, Double> parCategorie = gestionnaire.recetteParCategorie();
        assertEquals(f1.getMontantTTC(), parCategorie.get("SOCIAL"), 0.01);
        assertEquals(f2.getMontantTTC(), parCategorie.get("RESIDENTIEL"), 0.01);
    }

    @Test
    void testFacturesImpayeesEtPayees() {
        Releve r1 = new Releve("REL003", "AB003", 0, 30, "TECH01");
        Facture f1 = new Facture("FACT003", gestionnaire.rechercherParId("AB003"), r1);
        gestionnaire.ajouterFacture(f1);

        assertEquals(1, gestionnaire.listerFacturesImpayees().size());
        assertEquals(0, gestionnaire.listerFacturesPayees().size());

        f1.marquerPayee();

        assertEquals(0, gestionnaire.listerFacturesImpayees().size());
        assertEquals(1, gestionnaire.listerFacturesPayees().size());
    }
}
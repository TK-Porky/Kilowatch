package com.kilowatch.service;

import com.kilowatch.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoteurFacturationTest {

    private GestionnaireAbonnes gestionnaire;
    private MoteurFacturation moteur;

    @BeforeEach
    void setUp() {
        gestionnaire = new GestionnaireAbonnes();
        moteur = new MoteurFacturation(gestionnaire);
    }

    @Test
    void testFacturationResidentielSansTranche() {
        Abonne a = new AbonneResidentiel("AB001", "Jean Mballa", "Yaoundé", "CPT-1001");
        gestionnaire.ajouterAbonne(a);

        Facture f = moteur.saisirReleveEtFacturer("AB001", 100, "TECH01");

        assertEquals(6000.0, f.getMontantHT(), 0.01); // 100 * 60
        assertFalse(f.isPayee());
    }

    @Test
    void testFacturationResidentielAvecTranche() {
        Abonne a = new AbonneResidentiel("AB002", "Paul Eto", "Douala", "CPT-2002");
        gestionnaire.ajouterAbonne(a);

        Facture f = moteur.saisirReleveEtFacturer("AB002", 150, "TECH01");

        // 110 * 60 + 40 * 80 = 6600 + 3200 = 9800
        assertEquals(9800.0, f.getMontantHT(), 0.01);
    }

    @Test
    void testAbonneSocialExonereTVA() {
        Abonne a = new AbonneSocial("AB003", "Marie Etoa", "Bafoussam", "CPT-3003");
        gestionnaire.ajouterAbonne(a);

        Facture f = moteur.saisirReleveEtFacturer("AB003", 80, "TECH01");

        assertEquals(0.0, f.getMontantTVA(), 0.01);
    }

    @Test
    void testFacturationIndustriel() {
        Abonne a = new AbonneIndustriel("AB004", "Usine SOTRA", "Douala", "CPT-4004", 10);
        gestionnaire.ajouterAbonne(a);

        Facture f = moteur.saisirReleveEtFacturer("AB004", 200, "TECH01");

        // 200 * 95 + 10 * 500 = 19000 + 5000 = 24000
        assertEquals(24000.0, f.getMontantHT(), 0.01);
    }
}
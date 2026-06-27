package com.kilowatch.service;

import com.kilowatch.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoteurFacturationExtraTest {

    private GestionnaireAbonnes gestionnaire;
    private MoteurFacturation moteur;

    @BeforeEach
    void setUp() {
        gestionnaire = new GestionnaireAbonnes();
        moteur = new MoteurFacturation(gestionnaire);
    }

    @Test
    void testReleveEnchaineeUtiliseDernierIndex() {
        gestionnaire.ajouterAbonne(new AbonneSocial("AB001", "Marie Etoa", "Bafoussam", "CPT-1001"));

        Facture f1 = moteur.saisirReleveEtFacturer("AB001", 50, "TECH01");
        assertEquals(50.0, f1.getConsommationKWh(), 0.01);

        // Deuxième relève : doit repartir de l'index 50, pas de 0
        Facture f2 = moteur.saisirReleveEtFacturer("AB001", 90, "TECH01");
        assertEquals(40.0, f2.getConsommationKWh(), 0.01);
    }

    @Test
    void testFacturerLot() {
        gestionnaire.ajouterAbonne(new AbonneSocial("AB001", "Marie Etoa", "Bafoussam", "CPT-1001"));
        gestionnaire.ajouterAbonne(new AbonneResidentiel("AB002", "Jean Mballa", "Yaoundé", "CPT-2002"));

        Releve r1 = new Releve("REL001", "AB001", 0, 50, "TECH01");
        Releve r2 = new Releve("REL002", "AB002", 0, 100, "TECH01");

        List<Facture> factures = moteur.facturerLot(List.of(r1, r2));

        assertEquals(2, factures.size());
        assertEquals(2, gestionnaire.listerToutesFactures().size());
    }
}
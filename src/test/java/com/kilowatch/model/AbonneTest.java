package com.kilowatch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbonneTest {

    @Test
    void testCategorieSocial() {
        Abonne a = new AbonneSocial("AB001", "Marie Etoa", "Bafoussam", "CPT-1001");
        assertEquals("SOCIAL", a.getCategorie());
    }

    @Test
    void testCategorieResidentiel() {
        Abonne a = new AbonneResidentiel("AB002", "Jean Mballa", "Yaoundé", "CPT-2002");
        assertEquals("RESIDENTIEL", a.getCategorie());
    }

    @Test
    void testCategorieIndustriel() {
        Abonne a = new AbonneIndustriel("AB003", "Usine SOTRA", "Douala", "CPT-3003", 25);
        assertEquals("INDUSTRIE", a.getCategorie());
    }

    @Test
    void testDernierIndexSansReleve() {
        Abonne a = new AbonneSocial("AB004", "Paul Eto", "Douala", "CPT-4004");
        assertEquals(0.0, a.getDernierIndex(), 0.01);
    }

    @Test
    void testDernierIndexApresReleve() {
        Abonne a = new AbonneSocial("AB005", "Alice Nguema", "Yaoundé", "CPT-5005");
        Releve releve = new Releve("REL001", "AB005", 0, 50, "TECH01");
        a.ajouterReleve(releve);
        assertEquals(50.0, a.getDernierIndex(), 0.01);
    }

    @Test
    void testCalculMontantHTSocial() {
        Abonne a = new AbonneSocial("AB006", "Marc Biya", "Yaoundé", "CPT-6006");
        assertEquals(2500.0, a.calculerMontantHT(50), 0.01); // 50 * 50
    }

    @Test
    void testTauxTVAIndustriel() {
        Abonne a = new AbonneIndustriel("AB007", "Usine X", "Douala", "CPT-7007", 5);
        assertEquals(19.25, a.getTauxTVA(), 0.01);
    }
}
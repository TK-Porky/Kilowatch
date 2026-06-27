package com.kilowatch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactureTest {

    @Test
    void testFactureSocialeMontants() {
        Abonne a = new AbonneSocial("AB001", "Marie Etoa", "Bafoussam", "CPT-1001");
        Releve r = new Releve("REL001", "AB001", 0, 50, "TECH01");
        Facture f = new Facture("FACT001", a, r);

        assertEquals(2500.0, f.getMontantHT(), 0.01);  // 50 * 50
        assertEquals(0.0, f.getMontantTVA(), 0.01);     // exonéré
        assertEquals(2500.0, f.getMontantTTC(), 0.01);
        assertFalse(f.isPayee());
    }

    @Test
    void testFactureResidentielleAvecTVA() {
        Abonne a = new AbonneResidentiel("AB002", "Jean Mballa", "Yaoundé", "CPT-2002");
        Releve r = new Releve("REL002", "AB002", 0, 100, "TECH01");
        Facture f = new Facture("FACT002", a, r);

        assertEquals(6000.0, f.getMontantHT(), 0.01);          // 100 * 60
        assertEquals(6000.0 * 0.1925, f.getMontantTVA(), 0.01);
        assertEquals(f.getMontantHT() + f.getMontantTVA(), f.getMontantTTC(), 0.01);
    }

    @Test
    void testMarquerPayee() {
        Abonne a = new AbonneSocial("AB003", "Paul Eto", "Douala", "CPT-3003");
        Releve r = new Releve("REL003", "AB003", 0, 10, "TECH01");
        Facture f = new Facture("FACT003", a, r);

        assertFalse(f.isPayee());
        f.marquerPayee();
        assertTrue(f.isPayee());
    }

    @Test
    void testCategorieEtConsommationStockees() {
        Abonne a = new AbonneIndustriel("AB004", "Usine X", "Douala", "CPT-4004", 5);
        Releve r = new Releve("REL004", "AB004", 0, 20, "TECH01");
        Facture f = new Facture("FACT004", a, r);

        assertEquals("INDUSTRIE", f.getCategorieAbonne());
        assertEquals(20.0, f.getConsommationKWh(), 0.01);
        assertEquals("AB004", f.getIdAbonne());
        assertEquals("REL004", f.getIdReleve());
    }
}
package com.kilowatch.model;

import com.kilowatch.exception.ReleveInvalideException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReleveTest {

    @Test
    void testConsommationNormale() {
        Releve r = new Releve("REL001", "AB001", 100, 150, "TECH01");
        assertEquals(50.0, r.getConsommation(), 0.01);
    }

    @Test
    void testConsommationNulle() {
        Releve r = new Releve("REL002", "AB001", 100, 100, "TECH01");
        assertEquals(0.0, r.getConsommation(), 0.01);
    }

    @Test
    void testIndexActuelInferieurLeveException() {
        assertThrows(ReleveInvalideException.class, () -> {
            new Releve("REL003", "AB001", 200, 150, "TECH01");
        });
    }

    @Test
    void testGetters() {
        Releve r = new Releve("REL004", "AB002", 0, 75, "TECH02");
        assertEquals("REL004", r.getId());
        assertEquals("AB002", r.getIdAbonne());
        assertEquals("TECH02", r.getIdTechnicien());
        assertNotNull(r.getDateReleve());
    }
}
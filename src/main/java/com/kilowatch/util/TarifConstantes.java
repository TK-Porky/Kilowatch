package com.kilowatch.util;

public final class TarifConstantes {

    private TarifConstantes() {
        // empêche l'instanciation
    }

    // --- Catégorie SOCIAL ---
    public static final double PRIX_KWH_SOCIAL = 50.0;
    public static final double TVA_SOCIAL = 0.0; // exonéré

    // --- Catégorie RESIDENTIEL (tarification par tranches) ---
    public static final double SEUIL_RESIDENTIEL_T1 = 110.0;  // kWh
    public static final double PRIX_KWH_RESIDENTIEL_T1 = 60.0;
    public static final double PRIX_KWH_RESIDENTIEL_T2 = 80.0;

    // --- Catégorie INDUSTRIEL ---
    public static final double PRIX_KWH_INDUSTRIEL = 95.0;
    public static final double PRIME_PUISSANCE_KVA = 500.0;

    // --- Commun ---
    public static final double TVA_STANDARD = 19.25; // %
}
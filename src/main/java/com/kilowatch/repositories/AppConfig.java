package com.kilowatch.repositories;

import java.io.Serializable;

/**
 * Notre arbre de configuration fortement typé.
 */
public class AppConfig implements Serializable {

    // Accès JS-like : ConfigStore.data.prefs...
    public AppPreferences prefs = new AppPreferences();

    // Accès JS-like : ConfigStore.data.auth...
    public AuthData auth = new AuthData();

    // --- SOUS-CATÉGORIES ---

    public static class AppPreferences implements Serializable {
        public String theme = "DARK";
        public String emailContact = "support@kilowatch.com";
        public boolean autoSave = true;
    }

    public static class AuthData implements Serializable {
        public String username = "admin";
        public String password = "admin123"; // Identifiants par défaut
    }
}
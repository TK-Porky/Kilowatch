package com.kilowatch.view.data;

public class ViewEnum {

    // --- ENUMS MÉTIER ---

    public enum CategorieAbonne {
        SOCIAL("Social"),
        RESIDENTIEL("Résidentiel"),
        INDUSTRIEL("Industriel");

        private final String libelle;

        CategorieAbonne(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }

        @Override
        public String toString() {
            return libelle;
        }
    }

    public enum StatutFacture {
        PAYEE("Payée"),
        IMPAYEE("Impayée"),
        EN_ATTENTE("En Attente");

        private final String libelle;

        StatutFacture(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }

        @Override
        public String toString() {
            return libelle;
        }
    }

    // --- ENUMS VISUELS & FILTRES ---

    /**
     * Centralise les écrans (Vues) de l'application, leurs identifiants CardLayout
     * et leurs titres.
     */
    public enum AppView {
        DASHBOARD("VIEW_DASHBOARD", "Tableau de bord"),
        ABONNES("VIEW_ABONNES", "Abonnés & Contrats"),
        RELEVES("VIEW_RELEVES", "Relevés"),
        CAISSE("VIEW_CAISSE", "Caisse & Facturation");

        private final String id;
        private final String titre;

        AppView(String id, String titre) {
            this.id = id;
            this.titre = titre;
        }

        public String getId() {
            return id;
        }

        public String getTitre() {
            return titre;
        }

        public static AppView fromId(String id) {
            for (AppView view : values()) {
                if (view.id.equals(id))
                    return view;
            }
            throw new IllegalArgumentException("ID de vue inconnu : " + id);
        }
    }

    /**
     * Filtres applicables à la Caisse (Factures Impayées filtrées par Catégorie)
     */
    public enum FiltreCaisse {
        FILTER_ALL("Toutes"),
        FILTER_SOCIAL("Social"),
        FILTER_RESIDENTIEL("Résidentiel"),
        FILTER_INDUSTRIEL("Industriel");

        private final String libelle;

        FiltreCaisse(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    /**
     * Filtres applicables à la liste des Abonnés
     */
    public enum FiltreAbonne {
        FILTER_ALL("Tous les abonnés"),
        FILTER_GROS_CONS("Gros Consommateurs");

        private final String libelle;

        FiltreAbonne(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }
}
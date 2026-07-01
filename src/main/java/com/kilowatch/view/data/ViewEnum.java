package com.kilowatch.view.data;

public class ViewEnum {
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

        // Très utile pour que le JComboBox affiche directement le bon texte !
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

        // Très utile pour que le JComboBox affiche directement le bon texte !
        @Override
        public String toString() {
            return libelle;
        }
    }

}

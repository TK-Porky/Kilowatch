package com.kilowatch.exception;

public class AbonneNotFoundException extends RuntimeException {

    public AbonneNotFoundException(String idAbonne) {
        super("Aucun abonné trouvé avec l'identifiant : " + idAbonne);
    }
}
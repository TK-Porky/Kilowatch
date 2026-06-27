package com.kilowatch.exception;

public class AbonneDejaExistantException extends RuntimeException {

    public AbonneDejaExistantException(String idAbonne) {
        super("Un abonné avec l'identifiant " + idAbonne + " existe déjà.");
    }
}
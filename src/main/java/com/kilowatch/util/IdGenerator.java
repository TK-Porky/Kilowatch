package com.kilowatch.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

    private static final AtomicInteger compteurFacture = new AtomicInteger(1);
    private static final AtomicInteger compteurReleve = new AtomicInteger(1);
    private static final AtomicInteger compteurAbonne = new AtomicInteger(1);

    private IdGenerator() {}

    public static String nextFactureId() {
        return String.format("FACT-%04d", compteurFacture.getAndIncrement());
    }

    public static String nextReleveId() {
        return String.format("REL-%04d", compteurReleve.getAndIncrement());
    }

    public static String nextAbonneId() {
        return String.format("AB-%04d", compteurAbonne.getAndIncrement());
    }
}
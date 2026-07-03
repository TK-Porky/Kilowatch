package com.kilowatch.repositories;

import java.io.*;

public class ConfigStore {
    private static final String FILE_PATH = "data/app_config.ser";

    // C'est lui notre objet "JS-like" global !
    public static AppConfig data = new AppConfig();

    // Bloc statique : se lance automatiquement à la première utilisation de la
    // classe
    static {
        load();
    }

    public static void load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                data = (AppConfig) ois.readObject();
            } catch (Exception e) {
                System.err.println("Erreur de lecture de la config. Restauration par défaut.");
                data = new AppConfig();
            }
        } else {
            save(); // Création du fichier vierge avec les valeurs par défaut
        }
    }

    public static void save() {
        new File("data").mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
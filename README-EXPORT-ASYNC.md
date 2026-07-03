Changements réalisés — Export CSV asynchrone et thread-safety
=============================================================

Résumé
------
J'ai implémenté un export CSV asynchrone via `CompletableFuture` et un `Executor` dédié, ajouté une barre de progression dans la `Statusbar`, et rendu le `MockDataService` plus sûr pour les accès concurrents.

Fichiers modifiés
-----------------
- `src/main/java/com/kilowatch/view/ui/Statusbar.java`
  - Ajout d'un `JProgressBar`, d'un bouton `Annuler` et des méthodes publiques `startTask`, `updateProgress`, `endTask`.

- `src/main/java/com/kilowatch/repositories/AbonneRepository.java`
  - Ajout de la méthode `exporterFacturesImpayeesAsync(List<Facture>, Consumer<Integer>, Executor)` qui écrit le CSV en arrière-plan, publie la progression et respecte l'annulation via `Thread.interrupted()`.

- `src/main/java/com/kilowatch/view/data/MockDataService.java`
  - Utilisation d'un `ExecutorService` `ioExecutor` pour sérialiser les écritures, `CopyOnWriteArrayList` pour `recentActions` et mutations exécutées sur l'executor.
  - Exposition de `getIoExecutor()` pour l'intégration.

- `src/main/java/com/kilowatch/view/ui/MainLayout.java`
  - Remplacement du message "en développement" par l'appel asynchrone à l'export CSV (découvert via réflexion) et intégration de la `Statusbar` pour afficher la progression et permettre l'annulation.

Comment ça marche
-----------------
1. L'utilisateur clique sur "Exporter impayés (CSV)" dans la `Topbar`.
2. `MainLayout.triggerCsvExport()` tente d'obtenir le repository et l'exécuteur depuis `dataService` et appelle `exporterFacturesImpayeesAsync(...)`.
3. `AbonneRepository.exporterFacturesImpayeesAsync(...)` écrit le fichier CSV en itérant les factures impayées et appelle `progressCallback.accept(percent)`.
4. Le `Statusbar` affiche une barre de progression et un bouton "Annuler"; annulation tente d'arrêter l'executor.

Remarques et améliorations futures
---------------------------------
- Le code utilise la réflexion pour détecter `getRepository()` et `getIoExecutor()` sur `dataService`. Il est préférable d'étendre `ViewDataService` ou de définir une interface dédiée exposant ces méthodes afin d'avoir un lien fort au moment de la compilation.
- L'annulation actuelle appelle `shutdownNow()` sur l'executor de backing ; pour un comportement plus fin, conservez une référence au `Future` retourné par `CompletableFuture` et appelez `cancel(true)`.
- Ajouter des tests unitaires pour vérifier l'annulation et la progression, ainsi que des tests de concurrence pour éviter les `ConcurrentModificationException`.

Usage
-----
- Lancer l'application normalement.
- Menu `Fichier > Exporter impayés (CSV)` pour démarrer l'export asynchrone.

Questions / suivi
-----------------
Souhaitez-vous que je remplace l'utilisation de la réflexion par une extension de l'interface `ViewDataService` (ajout de méthodes `getRepository()` et `getIoExecutor()`), et que je crée des tests unitaires pour l'export et les scénarios concurrents ?

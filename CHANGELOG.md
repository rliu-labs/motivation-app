# Journal des versions

Toutes les évolutions notables de Sentier. Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/)
et les versions suivent le [versionnage sémantique](https://semver.org/lang/fr/).

## [0.2.1] - 2026-11-26

### Modifié
- APK bien plus léger : le code et les ressources inutilisés sont retirés à la construction (R8).

### Projet
- Chaque APK est installé sur un émulateur et parcouru automatiquement avant d'être publié.
- Mises à jour des dépendances regroupées une fois par mois.

## [0.2.0] - 2026-11-25

Étape 2 : habitudes et pas.

### Ajouté
- Habitudes à la manière de Loop Habit Tracker : oui/non ou chiffrées, fréquence souple
  (chaque jour, X fois par semaine ou par mois), cases des 5 derniers jours, 6 modèles prêts.
- Score de force de chaque habitude, calculé avec la formule de Loop.
- Pas lus dans Health Connect, avec le capteur du téléphone en secours : objectif réglable,
  kilomètres du jour, 7 derniers jours, distance totale.
- Pas et habitudes du jour sur l'écran Aujourd'hui.

### Modifié
- Base de données en version 2, avec migration qui conserve les tâches.

### Projet
- README de présentation avec captures d'écran générées automatiquement.
- Versions numérotées par étape, notes de version tirées de ce journal.
- APK d'aperçu des pull requests supprimés automatiquement à leur fermeture.
- Clé de signature fournie par les secrets du dépôt.

## [0.1.0] - 2026-11-25

Étape 1 : les tâches.

### Ajouté
- Écran Aujourd'hui avec progression du jour et saisie rapide.
- Tâches avec priorité en couleur, échéance, sous-tâches et listes, à la manière de Tasks.org.
- Répétition : chaque jour, tous les N jours, certains jours de la semaine, chaque mois, à partir de la fin.
- Stockage local (Room), page Crédits et licence GPL-3.0.
- Construction et publication automatiques de l'APK avec GitHub Actions.

[0.2.1]: https://github.com/rliu-labs/motivation-app/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/rliu-labs/motivation-app/releases/tag/v0.2.0

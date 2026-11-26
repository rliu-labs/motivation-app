# Journal des versions

Toutes les évolutions notables de Sentier. Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/)
et les versions suivent le [versionnage sémantique](https://semver.org/lang/fr/).

## [0.5.0] - 2026-11-26

Le voyage virtuel.

### Ajouté
- Onglet « Voyage » : tes pas te font avancer sur de vrais itinéraires, l'un après l'autre
  (tour du lac d'Annecy, chemin de Stevenson, canal du Midi, de Paris au Mont-Saint-Michel, environ 870 km).
- Carte dessinée par l'appli à partir des coordonnées des étapes, sans service en ligne : chemin parcouru, position, prochaine étape.
- Chaque étape atteinte s'annonce avec une anecdote sur le lieu et rapporte 15 XP et 3 pièces d'or.
- 2 nouveaux badges : « En route » (5 étapes) et « Grand voyageur » (25 étapes).

### Corrigé
- La cloche des rappels est alignée à droite de l'écran Aujourd'hui.

## [0.4.0] - 2026-11-26

Étape 4 : les rappels.

### Ajouté
- Rappel du matin : le programme de la journée (tâches, retards, habitudes, série en cours).
- Rappel du soir : ce qui reste à faire, seulement s'il reste quelque chose, avec une alerte quand la série est en jeu.
- Bouton « Fait » dans le rappel du soir pour valider une tâche ou une habitude sans ouvrir l'appli, XP et or compris.
- Écran « Rappels » (cloche en haut de l'écran Aujourd'hui) : activer chaque rappel, choisir son heure, recevoir un essai.
- Les habitudes à fréquence souple (3 fois par semaine…) ne sont rappelées que si leur quota n'est pas atteint.
- Les rappels sont désactivés par défaut ; l'autorisation des notifications n'est demandée qu'en les activant.

## [0.3.0] - 2026-11-26

Étape 3 : le jeu.

### Ajouté
- Expérience et or à chaque tâche validée, habitude réussie et objectif de pas atteint, selon la difficulté.
- Niveaux, avec la progression visible en haut de l'écran Aujourd'hui.
- Boutique de récompenses personnelle : ajouter, modifier, prendre une récompense avec son or.
- Série de jours avec jours de repos : un jour raté ne casse pas la série (1 jour de repos au départ, 1 de plus tous les 7 jours actifs, 2 au plus).
- 10 badges à débloquer.
- Choix de la difficulté des tâches et des habitudes.

### Modifié
- Base de données en version 3, avec migration qui conserve tout.
- Annuler une validation retire aussi l'XP et l'or gagnés.
- APK bien plus léger (4 Mo au lieu de 48) : le code et les ressources inutilisés sont retirés à la construction (R8).

### Projet
- Chaque APK est installé sur un émulateur et parcouru automatiquement avant d'être publié.
- Mises à jour des dépendances regroupées une fois par mois.
- README en anglais.

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

[0.5.0]: https://github.com/rliu-labs/motivation-app/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/rliu-labs/motivation-app/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/rliu-labs/motivation-app/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/rliu-labs/motivation-app/releases/tag/v0.2.0

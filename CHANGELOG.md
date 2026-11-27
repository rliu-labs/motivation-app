# Journal des versions

Toutes les évolutions notables de Sentier. Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/)
et les versions suivent le [versionnage sémantique](https://semver.org/lang/fr/).

## [0.16.0] - 2026-11-27

Cinq nouveaux pays d'Europe.

### Ajouté
- Pays-Bas : Amsterdam, les moulins de la Zaanse Schans et de Kinderdijk, les tulipes du Keukenhof, Delft, Rotterdam,
  Utrecht, la Hoge Veluwe et Giethoorn.
- Belgique : Bruxelles et l'Atomium, Anvers, Gand, Bruges, Ypres, Waterloo, Dinant, Liège et Spa.
- Suisse : Genève, Lavaux, Montreux, Zermatt, la Jungfraujoch, Berne, Lucerne, Zurich et Appenzell.
- Autriche : Vienne, Melk, Linz, Hallstatt, Salzbourg, Innsbruck, le Grossglockner, Graz et le lac de Neusiedl.
- Grèce : Athènes, Corinthe, Mycènes, Olympie, Delphes, les Météores, le mont Olympe, Thessalonique et Santorin.
- 17 nouvelles illustrations : moulins, champs de tulipes, maisons cubes, Atomium, jet d'eau, train de montagne,
  Hallstatt, tremplin de saut, canal de Corinthe, porte des Lionnes, Acropole, Météores, Santorin…
- Ces pays se choisissent, comme les cinq premiers, une fois le tour de France bouclé.

## [0.15.0] - 2026-11-27

Passeport de voyage, défis saisonniers, sommeil et sport automatiques.

### Ajouté
- Passeport de voyage (onglet Voyage, « Mon passeport ») : un tampon pour chaque escale atteinte, tous voyages confondus,
  daté du jour où l'escale a été atteinte et à l'encre du pays. Une page par voyage, les escales à venir en pointillé,
  et sur la couverture le nombre de tampons, de pays et de voyages bouclés. Les escales déjà atteintes ont leur tampon dès la mise à jour.
- Défis saisonniers (onglet Progrès) : dix défis qui reviennent chaque année, du Nouvel An à la trêve des confiseurs,
  en passant par la Saint-Valentin, le printemps, l'été, la rentrée, Halloween, novembre et le calendrier de l'Avent.
  Chacun rapporte un bonus d'XP et d'or et un badge rare ; quand aucun n'est en cours, le prochain est annoncé.
- Badges rares : un par défi saisonnier, avec le nombre de fois où il a été gagné. Ils s'ajoutent aux 14 badges
  sans changer les trésors déjà gagnés.
- Sommeil et sport automatiques (onglet Habitudes, avec Health Connect) : les nuits remplissent l'habitude de sommeil
  (heures dormies) et les séances de sport cochent l'habitude de sport, sur les 7 derniers jours. Réglage facultatif ;
  une valeur saisie à la main n'est jamais baissée.

### Modifié
- L'appli reste entièrement en français même sur un téléphone réglé dans une autre langue : le calendrier du choix de date,
  l'horloge du choix d'heure et les nombres suivent maintenant le français.

### Conservé
- Toutes les données (tâches, habitudes, XP, voyages) sont gardées à la mise à jour ; la base de données ne change pas.

## [0.14.0] - 2026-11-27

Voyages par pays : le tour de France, puis cinq premiers pays d'Europe.

### Ajouté
- Tour de France, débloqué une fois le tour du monde bouclé : 14 escales de Notre-Dame à l'arc de triomphe,
  par Reims, Strasbourg, Chamonix, Lyon, Avignon, Marseille, Carcassonne, Biarritz, Bordeaux, Chambord, Carnac,
  le Mont-Saint-Michel et Lille (environ 210 km de marche).
- Après la France, on choisit le pays suivant : Italie, Espagne, Portugal, Royaume-Uni ou Allemagne,
  de 8 à 9 escales chacun, avec architecture, culture, anecdote et souvenir à chaque étape.
- Les pas faits après la fin d'un voyage comptent déjà pour le suivant.
- Une carte détaillée du pays pour les voyages en Europe, toujours dessinée par l'appli à partir de Natural Earth.
- 36 nouvelles illustrations de monuments (cathédrales, châteaux, phare, trulli, Colisée, tour penchée, aqueduc…).
- « Mes voyages » : la liste de tous les voyages ; ceux qui sont finis se rouvrent avec leurs escales et leurs souvenirs.
- Le voyage en cours et les voyages finis sont compris dans les sauvegardes (les anciennes restent lisibles).

### Conservé
- La progression du tour du monde, ses escales déjà récompensées et ses souvenirs ne changent pas à la mise à jour.

## [0.13.0] - 2026-11-27

Bilan de la semaine, calendrier de l'année et journal des habitudes.

### Ajouté
- Bilan de la semaine : chaque dimanche à 18 h (heure réglable, ou désactivable dans Rappels), une notification
  résume la semaine. Elle ouvre un écran avec les jours actifs, les habitudes et les tâches comparés à la semaine d'avant,
  les moments forts (meilleure journée, habitude phare, record sur 3 mois, record de pas) et un objectif un peu plus haut
  pour la semaine suivante, suivi au fil des jours. Le bilan est aussi dans l'onglet Progrès.
- Calendrier de chaleur dans les statistiques : toute l'année d'un coup d'œil, une case par jour, plus foncée
  quand la journée a été remplie. Toucher une case affiche son détail.
- Note du jour pour chaque habitude, comme un petit journal : un bouton sur l'écran Aujourd'hui pour l'écrire,
  la note s'affiche sous l'habitude, et toutes les notes se retrouvent dans la fiche de l'habitude.
  Les notes sont comprises dans les sauvegardes (les anciennes sauvegardes restent lisibles).

## [0.12.0] - 2026-11-27

Tablettes, illustrations et finitions.

### Ajouté
- Mise en page pour tablette, pliable ouvert et mode paysage : la navigation passe dans un rail sur le côté
  et le contenu reste centré au lieu de s'étirer.
- Une illustration pour chaque escale du tour du monde (tour Eiffel, Parthénon, Taj Mahal, pagode de Kyoto,
  opéra de Sydney, aurore boréale de Reykjavik…), dessinée par l'appli, sans image téléchargée.
- Petite vibration de confirmation quand on coche une tâche ou une habitude (elle suit le réglage du téléphone).
- Aperçus des widgets dans le sélecteur, en thème clair ou sombre (Android 12 et plus).

## [0.11.0] - 2026-11-27

Sauvegarde automatique et gel de série.

### Ajouté
- Sauvegarde automatique chaque semaine dans un dossier choisi (sur le téléphone, une carte SD ou un dossier synchronisé),
  avec une première sauvegarde tout de suite ; les quatre dernières sont gardées.
- Gels de série : quand il ne reste plus de jour de repos, un gel protège la série pour un jour manqué.
  Un gel est offert au départ puis chaque trimestre, et la boutique en vend pour 40 pièces d'or (deux en réserve au plus).
  Un gel ne protège que les jours qui suivent son arrivée.

## [0.10.0] - 2026-11-27

Recherche, ordre à la main et calendrier des tâches.

### Ajouté
- Recherche dans les tâches (titres et notes), sans tenir compte des majuscules ni des accents.
- Trois présentations de l'onglet Tâches, mémorisées : par date, « Mon ordre » et calendrier.
- « Mon ordre » : on range les tâches en faisant glisser leur poignée ; avec un lecteur d'écran,
  les actions « Monter » et « Descendre » font de même.
- Calendrier du mois : un point par tâche prévue chaque jour, la liste du jour choisi en dessous,
  et la saisie rapide ajoute la tâche à ce jour-là.

## [0.9.0] - 2026-11-27

Accueil et rappels par habitude.

### Ajouté
- Accueil au premier lancement, en quatre écrans : bienvenue, choix des premières habitudes parmi les modèles,
  objectif de pas (fixe ou adaptatif) et rappels du matin et du soir. Il peut être passé d'un geste
  et ne s'affiche pas après une mise à jour.
- Rappel par habitude, à l'heure choisie dans sa fiche : seulement si elle n'est pas encore faite
  et si son quota de la période n'est pas atteint, avec un bouton « Fait » pour les habitudes oui/non.
- Les rappels des habitudes font partie de la sauvegarde ; les sauvegardes des versions précédentes restent lisibles.

## [0.8.0] - 2026-11-27

Statistiques, objectif adaptatif, thème et animations.

### Ajouté
- Écran « Statistiques » (onglet Progrès) sur 7 jours, 30 jours ou 12 mois : graphiques des pas (avec la ligne d'objectif),
  des habitudes réussies et de l'XP gagnée, jours actifs, record, moyenne, et taux de réussite de chaque habitude.
- Objectif de pas adaptatif, au choix : chaque jour, il suit tes pas des deux dernières semaines (la médiane, plus 10 %),
  sans bouger de plus de 1 000 pas d'un jour à l'autre, entre 3 000 et 20 000 pas.
- Choix du thème : comme le téléphone, toujours clair ou toujours sombre (onglet Progrès > Thème).
- Fêtes à l'écran avec des confettis : nouveau niveau, badge obtenu, défi relevé et nouveau trésor.
  Les animations sont coupées si elles le sont dans les réglages d'accessibilité du téléphone.

## [0.7.0] - 2026-11-27

Widgets et sauvegarde.

### Ajouté
- Widget « Aujourd'hui » pour l'écran d'accueil : la série, les pas du jour et les choses à faire,
  à cocher d'un geste sans ouvrir l'appli (XP et or compris).
- Petit widget « Pas et série » : les pas du jour avec leur objectif, la série et le niveau.
- Les widgets se mettent à jour dès qu'une tâche, une habitude ou les pas changent, et au moins toutes les 30 minutes.
- Sauvegarde (onglet Progrès) : exporter toutes les données (tâches, habitudes, pas, jeu, voyage, réglages)
  dans un fichier JSON rangé où l'on veut, et les restaurer sur ce téléphone ou un nouveau.
- La restauration demande confirmation, vérifie le fichier et se fait en tout ou rien : un fichier abîmé ne change rien.

## [0.6.0] - 2026-11-26

Défis et trésors.

### Ajouté
- Défis de la semaine, du lundi au dimanche, dans l'onglet Progrès : 70 000 pas (« La route de la soie »),
  sport 7 jours d'affilée (« Esprit olympique »), actif 5 jours (« Kaizen »), 10 tâches, 3 tâches difficiles
  (« Les travaux d'Hercule ») et 15 habitudes (« Le rituel du maté »).
- Chaque défi relevé rapporte un bonus de 30 à 80 XP, avec l'or qui va avec, versé aussitôt.
- 17 trésors rares venus des cultures du monde (boussole portugaise, bol raku, verre de Murano, œuf de Fabergé, moaï…),
  gagnés avec les niveaux, les séries, les badges et les défis. Rares, épiques ou légendaires, chacun a son histoire
  et son anecdote « Le savais-tu ? ».
- Le carnet de voyage, dans l'onglet Voyage, réunit les souvenirs des escales et les trésors.
- Un nouveau trésor est annoncé dès qu'il est gagné, où que l'on soit dans l'appli.
- 2 nouveaux badges : « Défi relevé » (1 défi) et « Aventurier » (10 défis).

## [0.5.0] - 2026-11-26

Le tour du monde.

### Ajouté
- Onglet « Voyage » : tes pas te font faire le tour du monde vers l'est, de Paris à Paris,
  en 25 escales (environ 700 km de marche ; les premières escales sont proches).
- Pour chaque escale : son architecture, sa culture et une anecdote « Le savais-tu ? ».
- Un souvenir typique à rapporter de chaque pays (daruma, babouches, tapis persan, pandeiro…), réunis dans une collection.
- Carte du monde en pointillés dessinée par l'appli, sans service en ligne : trajets en arcs, chemin parcouru, position, prochaine escale.
- Chaque escale atteinte rapporte 15 XP et 3 pièces d'or.
- 2 nouveaux badges : « En route » (5 escales) et « Tour du monde » (les 25 escales).

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

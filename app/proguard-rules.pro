# Règles R8 de Sentier.
#
# R8 retire le code et les ressources que l'appli n'utilise pas. Les bibliothèques
# (Room, WorkManager, Health Connect, Compose, DataStore) fournissent leurs propres règles.
# Les règles ci-dessous sont des sécurités en plus.

# On garde tous les noms de classes, champs et méthodes : R8 retire seulement ce qui est
# inutilisé. Les données enregistrées par leur nom (source des pas, fréquence de répétition,
# types d'habitudes…) restent lisibles d'une version à l'autre, et les rapports de plantage
# restent compréhensibles.
-dontobfuscate

# Énumérations relues par leur nom (valueOf) dans les réglages et la base.
-keepclassmembers enum app.sentier.** { *; }

# Entités et requêtes Room de l'appli.
-keep class app.sentier.data.** { *; }

# Tâches de fond créées par WorkManager à partir du nom de leur classe.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Activités déclarées dans le manifeste (dont l'explication Health Connect).
-keep class app.sentier.*Activity { *; }

# Actions des widgets, créées par Glance à partir du nom de leur classe.
-keep class * implements androidx.glance.appwidget.action.ActionCallback { <init>(); }

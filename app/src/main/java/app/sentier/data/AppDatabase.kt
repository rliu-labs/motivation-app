/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.tr
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Task::class, TaskList::class, Habit::class, HabitLog::class, DailySteps::class,
        GameEvent::class, Reward::class, HabitNote::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun gameDao(): GameDao

    companion object {
        const val NAME = "sentier.db"

        fun create(context: Context, inMemory: Boolean = false, name: String = NAME): AppDatabase {
            val builder = if (inMemory) {
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            } else {
                Room.databaseBuilder(context, AppDatabase::class.java, name)
            }
            return builder.addCallback(SeedCallback).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()
        }

        /** Étape 2 : habitudes et pas. Les tâches existantes sont conservées. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `habit` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `color` INTEGER NOT NULL, `icon` TEXT NOT NULL, `type` INTEGER NOT NULL, " +
                        "`target` REAL NOT NULL, `unit` TEXT NOT NULL, `freq_num` INTEGER NOT NULL, `freq_den` INTEGER NOT NULL, " +
                        "`difficulty` INTEGER NOT NULL, `position` INTEGER NOT NULL, `archived` INTEGER NOT NULL, " +
                        "`created_day` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `habit_log` (`habit_id` INTEGER NOT NULL, `day` INTEGER NOT NULL, " +
                        "`value` REAL NOT NULL, PRIMARY KEY(`habit_id`, `day`), FOREIGN KEY(`habit_id`) " +
                        "REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_log_day` ON `habit_log` (`day`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `daily_steps` (`day` INTEGER NOT NULL, `steps` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`day`))"
                )
            }
        }
        /** Étape 3 : le jeu. Les tâches, habitudes et pas existants sont conservés. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `game_event` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`kind` INTEGER NOT NULL, `ref_id` INTEGER NOT NULL, `day` INTEGER NOT NULL, `xp` INTEGER NOT NULL, " +
                        "`gold` INTEGER NOT NULL, `created_at` INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_game_event_kind_ref_id_day` ON `game_event` (`kind`, `ref_id`, `day`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_game_event_day` ON `game_event` (`day`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reward` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `cost` INTEGER NOT NULL, `icon` TEXT NOT NULL, `position` INTEGER NOT NULL)"
                )
                seedRewards(db)
            }
        }

        /** Rappel à l'heure choisie pour chaque habitude. Rien n'est perdu. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `habit` ADD COLUMN `reminder_minute` INTEGER")
            }
        }

        /** Note du jour attachée aux habitudes. Rien n'est perdu. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `habit_note` (`habit_id` INTEGER NOT NULL, `day` INTEGER NOT NULL, " +
                        "`text` TEXT NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`habit_id`, `day`), " +
                        "FOREIGN KEY(`habit_id`) REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_note_day` ON `habit_note` (`day`)")
            }
        }

        /** Récompenses d'exemple de la boutique (modifiables). */
        internal fun seedRewards(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO reward (name, cost, icon, position) VALUES (?, 20, 'tv', 0)", arrayOf<Any>(tr("Un épisode de série", "An episode of a series")))
            db.execSQL("INSERT INTO reward (name, cost, icon, position) VALUES (?, 30, 'cake', 1)", arrayOf<Any>(tr("Un dessert", "A dessert")))
            db.execSQL("INSERT INTO reward (name, cost, icon, position) VALUES (?, 60, 'game', 2)", arrayOf<Any>(tr("Une soirée jeu vidéo", "A video game night")))
        }
    }

    /** Listes de départ, créées au premier lancement. */
    private object SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO task_list (name, color, position) VALUES (?, 4281236827, 0)", arrayOf<Any>(tr("Perso", "Personal")))
            db.execSQL("INSERT INTO task_list (name, color, position) VALUES (?, 4294940672, 1)", arrayOf<Any>(tr("Maison", "Home")))
            db.execSQL("INSERT INTO task_list (name, color, position) VALUES (?, 4282557941, 2)", arrayOf<Any>(tr("Travail", "Work")))
            seedRewards(db)
        }
    }
}

/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Task::class, TaskList::class, Habit::class, HabitLog::class, DailySteps::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao

    companion object {
        const val NAME = "sentier.db"

        fun create(context: Context, inMemory: Boolean = false, name: String = NAME): AppDatabase {
            val builder = if (inMemory) {
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            } else {
                Room.databaseBuilder(context, AppDatabase::class.java, name)
            }
            return builder.addCallback(SeedCallback).addMigrations(MIGRATION_1_2).build()
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
    }

    /** Listes de départ, créées au premier lancement. */
    private object SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO task_list (name, color, position) VALUES ('Perso', 4281236827, 0)")
            db.execSQL("INSERT INTO task_list (name, color, position) VALUES ('Maison', 4294940672, 1)")
            db.execSQL("INSERT INTO task_list (name, color, position) VALUES ('Travail', 4282557941, 2)")
        }
    }
}

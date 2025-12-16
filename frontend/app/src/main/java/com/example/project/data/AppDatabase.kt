package com.example.project.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RoomEntity::class, Booking::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration() // For development; remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to 5 (initial setup)
        val MIGRATION_1_5 = object : Migration(1, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS rooms")
                database.execSQL(
                    """
                    CREATE TABLE rooms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        roomnumber TEXT NOT NULL,
                        block TEXT NOT NULL DEFAULT 'Unknown',
                        floor INTEGER NOT NULL DEFAULT 0,
                        capacity INTEGER NOT NULL,
                        status TEXT,
                        createdAt INTEGER,
                        updatedAt INTEGER
                    )
                    """
                )

                database.execSQL(
                    """
                    CREATE TABLE bookings (
                        bookingid INTEGER PRIMARY KEY AUTOINCREMENT,
                        roomid INTEGER NOT NULL,
                        userid INTEGER NOT NULL,
                        bookingdate TEXT NOT NULL,
                        starttime TEXT NOT NULL,
                        endtime TEXT,
                        purpose TEXT,
                        attendees INTEGER,
                        createdAt TEXT,
                        updatedAt TEXT,
                        FOREIGN KEY (roomid) REFERENCES rooms(id),
                        FOREIGN KEY (userid) REFERENCES users(id)
                    )
                    """
                )
            }
        }

        // Migration from version 5 to 6 (fix roomNumber -> roomnumber)
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE rooms_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        roomnumber TEXT NOT NULL,
                        block TEXT NOT NULL DEFAULT 'Unknown',
                        floor INTEGER NOT NULL DEFAULT 0,
                        capacity INTEGER NOT NULL,
                        status TEXT,
                        createdAt INTEGER,
                        updatedAt INTEGER
                    )
                    """
                )

                database.execSQL(
                    """
                    INSERT INTO rooms_temp (id, roomnumber, block, floor, capacity, status, createdAt, updatedAt)
                    SELECT id, roomNumber, block, floor, capacity, status, createdAt, updatedAt FROM rooms
                    """
                )

                database.execSQL("DROP TABLE rooms")
                database.execSQL("ALTER TABLE rooms_temp RENAME TO rooms")
            }
        }

        // Migration from version 6 to 7 (align schema with RoomEntity and Booking)
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check if columns exist in rooms table
                val cursor = database.query("PRAGMA table_info(rooms)")
                var hasCategory = false
                var hasSubCategory = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (columnName == "category") hasCategory = true
                    if (columnName == "subCategory") hasSubCategory = true
                }
                cursor.close()

                // Add columns only if they don’t exist
                if (!hasCategory) {
                    database.execSQL("ALTER TABLE rooms ADD COLUMN category TEXT")
                }
                if (!hasSubCategory) {
                    database.execSQL("ALTER TABLE rooms ADD COLUMN subCategory TEXT")
                }

                // Create a new bookings table with the correct schema
                database.execSQL(
                    """
                    CREATE TABLE bookings_temp (
                        bookingid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        roomid INTEGER NOT NULL,
                        userid INTEGER NOT NULL,
                        bookingdate TEXT NOT NULL,
                        starttime TEXT,
                        endtime TEXT,
                        purpose TEXT,
                        status TEXT NOT NULL,
                        attendees INTEGER
                    )
                    """
                )

                // Copy data from old bookings table, providing a default status
                database.execSQL(
                    """
                    INSERT INTO bookings_temp (bookingid, roomid, userid, bookingdate, starttime, endtime, purpose, status, attendees)
                    SELECT bookingid, roomid, userid, bookingdate, starttime, endtime, purpose, 'Pending', attendees FROM bookings
                    """
                )

                // Drop old bookings table
                database.execSQL("DROP TABLE bookings")

                // Rename temp table to bookings
                database.execSQL("ALTER TABLE bookings_temp RENAME TO bookings")
            }
        }
    }
}
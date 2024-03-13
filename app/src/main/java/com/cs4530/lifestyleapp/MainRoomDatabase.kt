package com.cs4530.lifestyleapp

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlin.jvm.Volatile
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WeatherTable::class, UserTable::class], version = 1, exportSchema = false)
abstract class MainRoomDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun weatherDao(): WeatherDao

    // Make the db singleton. Could in theory
    // make this an object class, but the companion object approach
    // is nicer (imo)
    companion object {
        @Volatile
        private var mInstance: MainRoomDatabase? = null

        fun getDatabase(context: Context, scope : CoroutineScope): MainRoomDatabase {
            return mInstance?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, MainRoomDatabase::class.java, "main.db")
                    .addCallback(RoomDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                mInstance = instance
                instance
            }
        }

        private class RoomDatabaseCallback(private val scope: CoroutineScope): RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                mInstance?.let { database ->
                    scope.launch(Dispatchers.IO){
                        populateDbTask(database.weatherDao())
                    }
                }
            }
        }

        // TODO: add UserDao as a param
        suspend fun populateDbTask (weatherDao: WeatherDao) {
            //TODO: Modify insert to fit data used in the two entities(tables)

            weatherDao.insert(WeatherTable(temperature = 100, tempHigh = 200, tempLow = 0, humidity = 100))
            //userDao.insert(UserTable("dummy_info", "dummy_info"))
        }
    }
}
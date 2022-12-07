package com.example.zadanie.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Contact
// abstractna classa  - vytvori Room DB
// abstractna lebo Room vytvori implementaciu za nas
//export schema aby sa neukladali zalohy historie schemy
@Database(entities = [BarItem::class,Contact::class], version = 4, exportSchema = false)
abstract class AppRoomDatabase: RoomDatabase() {
//    room vytvori implementaciu dao za nas
    abstract fun appDao(): DbDao
//  umoznuje pristup k metodam na vytvaranie alebo ziskavanie DB
    companion object{
//    volatile variable will never be cached
        @Volatile
//        inicializacia INSTANCE - keep reference to DB
        private var INSTANCE: AppRoomDatabase? = null
//      Vracia Instance
//    ak je null inicializuje sa v synchronize blocku
        fun getInstance(context: Context): AppRoomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {  INSTANCE = it }
            }
//      vytvorenie DB - pass context, class DB a jej meno
//    pri zmene sa zavola destructive migration - destroy and rebuild DB
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppRoomDatabase::class.java, "barsDatabase"
            ).fallbackToDestructiveMigration()
                .build()
    }
}
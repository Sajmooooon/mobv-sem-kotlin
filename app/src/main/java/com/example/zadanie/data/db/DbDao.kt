package com.example.zadanie.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Contact

@Dao
interface DbDao {

    // Friends
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<Contact>)

    @Query("DELETE FROM friends")
    suspend fun deleteFriends()

    @Query("SELECT * FROM friends order by user_name DESC")
    fun getFriends(): LiveData<List<Contact>?>

    //Bars
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBars(bars: List<BarItem>)

    @Query("DELETE FROM bars")
    suspend fun deleteBars()

    @Query("SELECT * FROM bars order by users DESC, name ASC")
    fun getBars(): LiveData<List<BarItem>?>

    @Query("SELECT users FROM bars WHERE id =:id")
    fun getUsers(id: String?): Int?

//    sort

    @Query("SELECT * FROM bars order by users DESC, name ASC")
    fun getBarsUsersDesc(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by users ASC, name ASC")
    fun getBarsUsersAsc(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by name DESC")
    fun getBarsNameDesc(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by name ASC")
    fun getBarsNameAsc(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by type DESC")
    fun getBarsTypeDesc(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by type ASC")
    fun getBarsTypeAsc(): LiveData<List<BarItem>?>
}
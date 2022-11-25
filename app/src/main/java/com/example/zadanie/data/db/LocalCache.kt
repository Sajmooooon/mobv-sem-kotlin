package com.example.zadanie.data.db

import androidx.lifecycle.LiveData
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Contact

class LocalCache(private val dao: DbDao) {
    suspend fun insertBars(bars: List<BarItem>){
        dao.insertBars(bars)
    }

    suspend fun insertFriends(friends: List<Contact>){
        dao.insertFriends(friends)
    }
//
    suspend fun deleteFriends(){ dao.deleteFriends() }

    suspend fun deleteBars(){ dao.deleteBars() }

    fun getBars(): LiveData<List<BarItem>?> = dao.getBars()

    fun getFriends(): LiveData<List<Contact>?> = dao.getFriends()

    fun getUsers(id: String?): Int?{
        return dao.getUsers(id)
    }
}
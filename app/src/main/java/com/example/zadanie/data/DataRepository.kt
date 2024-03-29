package com.example.zadanie.data

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.example.zadanie.data.api.*
import com.example.zadanie.data.db.LocalCache
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Contact
import com.example.zadanie.ui.viewmodels.data.MyLocation
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DataRepository private constructor(
    private val service: RestApi,
    private val cache: LocalCache
) {

//    pridanie priatela
    suspend fun apiAddFriend(
        name: String,
        onError: (error: String) -> Unit,
        onSuccess: (success: Boolean) -> Unit
    ) {
        try {
            val resp = service.addFriend(AddContactRequest(contact = name))
            when (resp.code()) {
                500 -> onError("User doesn't exist.")
                400 -> onError("Wrong request.")
                401 -> onError("Non authorized.")
                404 -> onError("Wrong endpoint.")
                200 -> onSuccess(true)
                else -> onError("Failed to add Friend, try again later.")

            }

        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Friend add failed, check internet connection")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Login in failed, error.")
        }
    }

//  zobrazenie priatelov - nacitanie a ulozenie do DB
    suspend fun apiFriendsList(
        onError: (error: String) -> Unit
    ) {
        try {
            Log.d("friend-more", "in2")
            val resp = service.friendList()
            if (resp.isSuccessful) {
                resp.body()?.let { bars ->
                    val b = bars.map {
                        Contact(
                            it.user_id,
                            it.user_name,
                            it.bar_id,
                            it.bar_name,
//                            it.time,
                            it.bar_lat,
                            it.bar_lon
                        )
                    }
                    cache.deleteFriends()
                    cache.insertFriends(b)
                } ?: onError("Failed to load friends")
            } else {
                onError("Failed to read friends")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Failed to load friends, check internet connection")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Failed to load friends, error.")
        }
    }

//    register pouzivatela
    suspend fun apiUserCreate(
        name: String,
        password: String,
        onError: (error: String) -> Unit,
        onStatus: (success: UserResponse?) -> Unit
    ) {
        try {
            val resp = service.userCreate(UserCreateRequest(name = name, password = password))
            if (resp.isSuccessful) {
                resp.body()?.let { user ->
                    if (user.uid == "-1") {
                        onStatus(null)
                        onError("Name already exists. Choose another.")
                    } else {
                        onStatus(user)
                    }
                }
            } else {
                onError("Failed to sign up, try again later.")
                onStatus(null)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Sign up failed, check internet connection")
            onStatus(null)
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Sign up failed, error.")
            onStatus(null)
        }
    }

//    login pouzivatela
    suspend fun apiUserLogin(
        name: String,
        password: String,
        onError: (error: String) -> Unit,
        onStatus: (success: UserResponse?) -> Unit
    ) {
        try {
            val resp = service.userLogin(UserLoginRequest(name = name, password = password))
            if (resp.isSuccessful) {
                resp.body()?.let { user ->
                    if (user.uid == "-1") {
                        onStatus(null)
                        onError("Wrong name or password.")
                    } else {
                        onStatus(user)
                    }
                }
            } else {
                onError("Failed to login, try again later.")
                onStatus(null)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Login failed, check internet connection")
            onStatus(null)
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Login in failed, error.")
            onStatus(null)
        }
    }

//    zapisanie do baru
    suspend fun apiBarCheckin(
        bar: NearbyBar,
        onError: (error: String) -> Unit,
        onSuccess: (success: Boolean) -> Unit
    ) {
        try {
            val resp = service.barMessage(
                BarMessageRequest(
                    bar.id.toString(),
                    bar.name,
                    bar.type,
                    bar.lat,
                    bar.lon
                )
            )
            if (resp.isSuccessful) {
                resp.body()?.let {
                    onSuccess(true)
                }
            } else {
                onError("Failed to login, try again later.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Login failed, check internet connection")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Login in failed, error.")
        }
    }

//  ziskanie a ulozenie barlistu do DB
    suspend fun apiBarList(
        onError: (error: String) -> Unit
    ) {
        try {
            val resp = service.barList()
            if (resp.isSuccessful) {
                resp.body()?.let { bars ->

                    val b = bars.map {
                        BarItem(
                            it.bar_id,
                            it.bar_name,
                            it.bar_type,
                            it.lat,
                            it.lon,
                            it.users
                        )
                    }
                    cache.deleteBars()
                    cache.insertBars(b)
                } ?: onError("Failed to load bars")
            } else {
                onError("Failed to read bars")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Failed to load bars, check internet connection")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Failed to load bars, error.")
        }
    }

    //pridanie s webservicom
//    suspend fun apiDistanceBarsServ(
//        lat: Double, lon: Double,
//    ) {
//        try {
////            var bar = listOf<BarItem>()
//            val resp = service.barList()
//            if (resp.isSuccessful) {
//                resp.body()?.let { bars ->
//
//
//                    val bar = bars.map {
//                        BarItem(
//                            it.bar_id,
//                            it.bar_name,
//                            it.bar_type,
//                            it.lat,
//                            it.lon,
//                            it.users,
//
//                            ).apply {
//                            distance = distanceTo(MyLocation(lat, lon))
//                        }
//                    }
//                    cache.deleteBars()
//                    cache.insertBars(bar)
//                }
//
//
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            print("exception ${e}")
//        }
//    }


//    bary v okolo od nasej vzdialenosti
    suspend fun apiNearbyBars(
        lat: Double, lon: Double,
        onError: (error: String) -> Unit
    ): List<NearbyBar> {
        var nearby = listOf<NearbyBar>()
        try {
            val q =
                "[out:json];node(around:250,$lat,$lon);(node(around:250)[\"amenity\"~\"^pub$|^bar$|^restaurant$|^cafe$|^fast_food$|^stripclub$|^nightclub$\"];);out body;>;out skel;"
            val resp = service.barNearby(q)
            if (resp.isSuccessful) {
                resp.body()?.let { bars ->
                    nearby = bars.elements.map {
                        NearbyBar(
                            it.id,
                            it.tags.getOrDefault("name", ""),
                            it.tags.getOrDefault("amenity", ""),
                            it.lat,
                            it.lon,
                            it.tags
                        ).apply {
                            distance = distanceTo(MyLocation(lat, lon))
                        }
                    }
                    nearby = nearby.filter { it.name.isNotBlank() }.sortedBy { it.distance }
                } ?: onError("Failed to load bars")
            } else {
                onError("Failed to read bars")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Failed to load bars, check internet connection")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Failed to load bars, error.")
        }
        return nearby
    }

//    ziskanie detialu baru
    suspend fun apiBarDetail(
        id: String,
        onError: (error: String) -> Unit
    ): NearbyBar? {
        var nearby: NearbyBar? = null
        try {
            val q = "[out:json];node($id);out body;>;out skel;"
            val resp = service.barDetail(q)
            if (resp.isSuccessful) {
                resp.body()?.let { bars ->
                    if (bars.elements.isNotEmpty()) {
                        val b = bars.elements.get(0)
                        nearby = NearbyBar(
                            b.id,
                            b.tags.getOrDefault("name", ""),
                            b.tags.getOrDefault("amenity", ""),
                            b.lat,
                            b.lon,
                            b.tags,
                        )
                    }
                } ?: onError("Failed to load bars")
            } else {
                onError("Failed to read bars")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onError("Failed to load bars, check internet connection")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError("Failed to load bars, error.")
        }
        return nearby
    }

//    nacitanie barov
    fun dbBars(): LiveData<List<BarItem>?> {
        return cache.getBars()
    }
//    sort
//
//    fun getBarsDesc(): LiveData<List<BarItem>?> {
//        return cache.getBarsNameDesc()
//    }
//
//    fun getBarsAsc(): LiveData<List<BarItem>?> {
//        return cache.getBarsNameAsc()
//    }
//
//    fun getUsersDesc(): LiveData<List<BarItem>?> {
//        return cache.getUsersDesc()
//    }
//
//    fun getUsersAsc(): LiveData<List<BarItem>?> {
//        return cache.getUsersAsc()
//    }
//
//    fun getDistanceDesc(): LiveData<List<BarItem>?> {
//        return cache.getDistanceDesc()
//    }
//
//    fun getDistanceAsc(): LiveData<List<BarItem>?> {
//        return cache.getDistanceAsc()
//    }

//  nacitanie priatelov
    fun dbFriends(): LiveData<List<Contact>?> {
        return cache.getFriends()
    }

//  ziskanie pocet pouzivatelov v danom bare
    fun getDbUsers(id: String?, onError: (error: String) -> Unit): Int? {
        return cache.getUsers(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(service: RestApi, cache: LocalCache): DataRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: DataRepository(service, cache).also { INSTANCE = it }
            }
//
//        @SuppressLint("SimpleDateFormat")
//        fun dateToTimeStamp(date: String): Long {
//            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)?.time ?: 0L
//        }
//
//        @SuppressLint("SimpleDateFormat")
//        fun timestampToDate(time: Long): String {
//            val netDate = Date(time * 1000)
//            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(netDate)
//        }
    }
}
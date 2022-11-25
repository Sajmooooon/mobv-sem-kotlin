package com.example.zadanie.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "friends")
data class Contact(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
//    @Json(name = "user_id")
    val user_id: String,

    @ColumnInfo(name = "user_name")
//    @Json(name = "user_name")
    val user_name: String,

    @ColumnInfo(name = "bar_id")
//    @Json(name = "bar_id")
    val bar_id: String? = null,

    @ColumnInfo(name = "bar_name")
//    @Json(name = "bar_name")
    val bar_name: String? = null,

//    val time: String? = null,

    @ColumnInfo(name = "bar_lat")
//    @Json(name = "bar_lat")
    val bar_lat: String? = null,

    @ColumnInfo(name = "bar_lon")
//    @Json(name = "bar_lon")
    val bar_lon: String? = null
)

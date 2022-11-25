package com.example.zadanie.data.api

import android.content.Context
import com.example.zadanie.data.api.helper.AuthInterceptor
import com.example.zadanie.data.api.helper.TokenAuthenticator
import com.example.zadanie.data.db.model.Contact
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface RestApi {

    @GET("https://overpass-api.de/api/interpreter?")
    suspend fun barDetail(@Query("data") data: String): Response<BarDetailResponse>

    @GET("https://overpass-api.de/api/interpreter?")
    suspend fun barNearby(@Query("data") data: String): Response<BarDetailResponse>

    @POST("user/create.php")
    suspend fun userCreate(@Body user: UserCreateRequest): Response<UserResponse>

    @POST("user/login.php")
    suspend fun userLogin(@Body user: UserLoginRequest): Response<UserResponse>

//    @POST("user/refresh.php")
//    suspend fun userRefresh(@Body user: UserRefreshRequest) : Response<UserResponse>

    @POST("user/refresh.php")
    fun userRefresh(@Body user: UserRefreshRequest) : Call<UserResponse>

    @GET("bar/list.php")
    @Headers("mobv-auth: accept")
    suspend fun barList() : Response<List<BarListResponse>>

    @POST("bar/message.php")
    @Headers("mobv-auth: accept")
    suspend fun barMessage(@Body bar: BarMessageRequest) : Response<Any>

    @POST("contact/message.php")
    @Headers("mobv-auth: accept")
    suspend fun addFriend(@Body contact: AddContactRequest) : Response<Void>

    @GET("contact/list.php")
    @Headers("mobv-auth: accept")
    suspend fun friendList() : Response<List<Contact>>

//    @GET("contact/list.php")
//    @Headers("mobv-auth: accept")
//    suspend fun showFriends() : Response<List<Contact>>



    companion object{
        const val BASE_URL = "https://zadanie.mpage.sk/"

        fun create(context: Context): RestApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .authenticator(TokenAuthenticator(context))
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val retrofitBuilder: RestApi by lazy { retrofit.create(RestApi::class.java) }
//          return retrofit.create(RestApi::class.java)
            return retrofitBuilder
        }
    }
}
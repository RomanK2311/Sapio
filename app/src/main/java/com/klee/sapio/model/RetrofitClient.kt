package com.klee.sapio.model

import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.42:1337/api/"

    val okHttpClient = OkHttpClient()
        .newBuilder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    fun getClient(): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(JacksonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
}

interface RemoteApplicationsApi {
   @GET("applications")
   fun getApplicationsAsync(): Deferred<StrapiAnswer>

}

class ApplicationService {

    private val retrofit = RetrofitClient.getClient()
    private val applicationsApi = retrofit.create(RemoteApplicationsApi::class.java)

    suspend fun getRemoteApplications(): List<RemoteApplication> {
        val list = ArrayList<RemoteApplication>()
        var strapiAnswer: StrapiAnswer
        
        withContext(Dispatchers.IO) {
            strapiAnswer = applicationsApi.getApplicationsAsync().await()
        }

        strapiAnswer.data.map {
            list.add(it.attributes)
        }

        return list
    }
}
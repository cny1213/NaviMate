package com.psg.navimate.util

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://donw8-app-66374785524.asia-northeast3.run.app/"

    fun <T> create(service: Class<T>): T {
        // 1) 로깅 인터셉터 준비
        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                // 모든 HTTP 메시지를 “API_CALL” 태그로 찍어준다
                Log.d("API_CALL", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2) OkHttpClient 에 붙이기
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        // 3) Retrofit 에 클라이언트 연결
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(service)
    }
}


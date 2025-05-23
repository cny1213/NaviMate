package com.psg.navimate.util

import com.psg.navimate.Model.NaverLocalResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverApiService {
    @GET("v1/search/local.json")
    suspend fun searchLocal(
        @Header("X-Naver-Client-Id")     clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query")                  query: String,
        @Query("display")                display: Int = 10,
        @Query("start")                  start: Int = 1
    ): Response<NaverLocalResponse>
}
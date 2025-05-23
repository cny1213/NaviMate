package com.psg.navimate.util

import com.psg.navimate.Model.RouteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// POST /find_route/ 로 JSON 바디({ origin, destination })를 전송하고
// RouteResponse 전체(JSON)를 받아옵니다.
interface RouteApiService {
    @POST("find_route/")
    suspend fun findRoute(
        @Body body: FindRouteRequest
    ): Response<RouteResponse>    // ← 여기
}

data class FindRouteRequest(val origin: String, val destination: String)

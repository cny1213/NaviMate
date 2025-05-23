package com.psg.navimate.ViewModel

import android.util.Log
import androidx.lifecycle.liveData
import com.psg.navimate.util.Result
import com.psg.navimate.util.Status
import androidx.lifecycle.ViewModel
import com.psg.navimate.util.RetrofitClient
import com.psg.navimate.util.RouteApiService
import com.psg.navimate.Model.Route
import com.psg.navimate.Model.RouteResponse
import com.psg.navimate.util.FindRouteRequest

class FindWayViewModel : ViewModel() {
    private val service = RetrofitClient.create(RouteApiService::class.java)

    /**
     * 출발지/도착지 주소(또는 명)로 경로 검색
     * 반환: LiveData<List<Route>>
     */
    fun searchRoutes(origin: String, destination: String) = liveData {
        // 1) 로딩 상태 전달
        emit(Result.loading<List<Route>>())

        try {
            // 2) POST 바디에 origin/destination 담아서 호출
            val request = FindRouteRequest(origin = origin, destination = destination)
            val resp = service.findRoute(request)

            // 🚩 여기에 URL 로그 추가
            val url = resp.raw().request.url.toString()
            Log.d("FindWayFrag", "▶ [API] 요청 URL: $url")

            if (resp.isSuccessful) {
                // 3) RouteResponse 전체에서 routes만 꺼내서
                val body: RouteResponse = resp.body()!!
                val routes: List<Route> = body.routes

                Log.d("FindWayFrag", "▶ body: $body")
                Log.d("FindWayFrag", "▶ routes: $routes")

                // 4) 성공 상태로 emit
                emit(Result.success(routes))
            } else {
                emit(Result.error<List<Route>>("서버 오류: ${resp.code()}"))
            }
        } catch (e: Exception) {
            emit(Result.error<List<Route>>(e.localizedMessage ?: "네트워크 오류"))
        }
    }
}
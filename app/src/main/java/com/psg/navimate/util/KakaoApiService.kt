package com.psg.navimate.util

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApiService {
    /**
     * @param KakaoAK {REST_API_KEY} 형태로 넘겨주세요
     * @param query 유저가 입력한 주소 키워드
     */
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Header("Authorization") authorization: String,
        @Query("query") query: String
    ): Response<KakaoAddressResponse>
}

/** API 전체 응답에서 우리가 쓸 부분만 뽑습니다 */
data class KakaoAddressResponse(
    val documents: List<KakaoAddressDoc>
)

/** 이걸 Address 로 변환할 거예요 */
data class KakaoAddressDoc(
    @SerializedName("address_name") val address_name: String,      // 전체 지번/도로명
    @SerializedName("road_address")  val road_address: Road?,      // 도로명주소 객체 (null 가능)
    @SerializedName("x")             val x: String,                // 경도 (문자열)
    @SerializedName("y")             val y: String                 // 위도 (문자열)
)

data class Road(
    @SerializedName("address_name") val address_name: String       // 도로명주소
)
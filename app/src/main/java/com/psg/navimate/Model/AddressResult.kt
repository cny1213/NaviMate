package com.psg.navimate.Model

import com.google.gson.annotations.SerializedName

// Address.kt
data class Address(
    val id: String,
    val title: String, // 이름
    val address: String, // 지번주소
    val roadAddress: String, // 도로명주소
    val mapx: Double, // 위도
    val mapy: Double // 경도
)

// API 응답 최상위
data class NaverLocalResponse(
    @SerializedName("items") val items: List<NaverLocalItem>
)

data class NaverLocalItem(
    @SerializedName("title") val title: String,
    @SerializedName("link") val link: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("telephone") val telephone: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String,
    @SerializedName("mapx") val mapx: String,
    @SerializedName("mapy") val mapy: String
)

// Model/NaverAddressResponse.kt
data class NaverAddressResponse(
    val items: List<NaverAddressItem>
)
data class NaverAddressItem(
    @SerializedName("address")     val address: String,
    @SerializedName("roadAddress") val roadAddress: String,
    @SerializedName("mapx")        val mapx: String,
    @SerializedName("mapy")        val mapy: String
)

package com.psg.navimate.Model

import com.google.gson.annotations.SerializedName
import com.naver.maps.geometry.LatLng

//data class Route(
//    val route: Int,
//    val legs: List<Leg>
//)
//
//data class Leg(
//    val duration: String,
//    val steps: List<Step>
//)
//
//data class Step(
//    val duration:   String,
//    val mode:       String,
//    val description:String? = null,
//    val line:       String? = null,
//    val from:       String? = null,
//    val to:         String? = null,
//    @SerializedName("stn_index")
//    val stnIndex:   StationIndex? = null
//)
//
//data class StationIndex(
//    @SerializedName("STATION_ID") val stationId: String?,
//    @SerializedName("X")          val x: Double?,
//    @SerializedName("Y")          val y: Double?,
//    val order:      Int?,
//    val distance:   Double?
//)

data class RouteResponse(
    val origin: List<String>,       // ["37.36178", "126.96708"]
    val destination: List<String>,  // ["37.38501", "127.12343"]
    val routes: List<Route>
)

data class Route(
    @SerializedName("route_number")
    val routeNumber: String,

    /** 전체 소요시간(초 단위) **/
    val duration: Int,

    /** 경로 내 각 구간(steps) **/
    val steps: List<Step>,

    /** 경로를 그릴 폴리라인 좌표 목록 [[lat,lng], …] **/
    @SerializedName("overview_polyline")
    val overviewPolyline: List<List<Double>>
)

data class Step(
    /** 해당 구간 소요시간(초) **/
    val duration: Int,

    /** "walk", "Bus", "Subway" 등 **/
    val mode: String,

    /** 보행(step.mode=="walk") 시 텍스트 설명 **/
    val description: String? = null,

    /** 버스/지하철 노선명 **/
    val line: String? = null,

    /** 탑승(버스/지하철) 시작 지점 정보 **/
    val from: Place? = null,

    /** 탑승(버스/지하철) 하차 지점 정보 **/
    val to: Place? = null
)

data class Place(
    /** [longitude, latitude] 또는 [latitude, longitude] 순서인지 서버 문서 확인! **/
    val coord: List<Double>,

    /** 정류장/역 이름 **/
    val name: String
)

// DisplayItem (그 경로들어가서 있는 항목들) ex) 버스, 농수산물시장, 좌표
data class DisplayItem(
    val title1: String,
    val title2: String,
    val coord: LatLng
)

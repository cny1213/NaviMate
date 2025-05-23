package com.psg.navimate.View

import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.psg.navimate.MainActivity
import com.psg.navimate.Model.Address
import com.psg.navimate.Model.DisplayItem
import com.psg.navimate.Model.Route
import com.psg.navimate.Model.RouteResponse
import com.psg.navimate.R
import com.psg.navimate.View.adapter.StepListAdapter
import com.psg.navimate.databinding.FragmentRouteMapBinding

class RouteMapFragment : Fragment(), OnMapReadyCallback {

    lateinit var fragmentRouteMapBindng:FragmentRouteMapBinding
    lateinit var mainActivity: MainActivity
    lateinit var naverMap:NaverMap
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var displayItems: List<DisplayItem>

    // 번들로 얻은 데이터들
    lateinit var origin :List<String> // 출발지 좌표(위도,경도)
    lateinit var destination : List<String> // 도착지 좌표(위도,경도)
    lateinit var startAddress: Address // 출발지 명
    lateinit var endAddress: Address // 도착지 명
    lateinit var route: Route // 번들로 받은 단일 경로 객체

    companion object {
        private const val ARG_ROUTE_JSON = "arg_route_json"
        private const val ARG_START_ADDRESS   = "arg_start_address"
        private const val ARG_END_ADDRESS     = "arg_end_address"
        private const val ARG_ROUTE_INDEX     = "arg_route_index"

        /**
         * @param routeJson    : 길찾기 API(JSON→RouteResponse) 전체
         * @param startJson    : 시작지 Address JSON
         * @param endJson      : 도착지 Address JSON
         */

        fun newInstance(routeJson: String, startJson: String, endJson: String, routeIndex: Int) = RouteMapFragment().apply {
            arguments = bundleOf(
                ARG_ROUTE_JSON    to routeJson,
                ARG_START_ADDRESS to startJson,
                ARG_END_ADDRESS   to endJson,
                ARG_ROUTE_INDEX   to routeIndex
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val args = requireArguments()

        // 1) 전체 응답을 RouteResponse 로 파싱
        val routeJson  = args.getString(ARG_ROUTE_JSON)!!
        val resp       = Gson().fromJson(routeJson, RouteResponse::class.java)
        origin         = resp.origin
        destination    = resp.destination

        val routeIndex = args.getInt(ARG_ROUTE_INDEX)               // 여기서 index 꺼내고
        origin         = resp.origin
        destination    = resp.destination
        route          = resp.routes[routeIndex]                    // 선택된 경로만 사용

        // 2) 출발지/도착지 Address 파싱
        val startJson = args.getString(ARG_START_ADDRESS)!!
        val endJson   = args.getString(ARG_END_ADDRESS)!!
        startAddress  = Gson().fromJson(startJson, Address::class.java)
        endAddress    = Gson().fromJson(endJson,   Address::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentRouteMapBindng = FragmentRouteMapBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        return fragmentRouteMapBindng.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()

        Log.d("RouteMap", "▶ onViewCreated")

        // 1) "알고리즘 적용 최적경로" 텍스트뷰 여부 표시
        if (route.routeNumber.contains("-")) {
            fragmentRouteMapBindng.textViewOptimized.visibility = View.VISIBLE
        } else {
            fragmentRouteMapBindng.textViewOptimized.visibility = View.GONE
        }

        // 출발지·도착지 텍스트 채우기
        fragmentRouteMapBindng.textView3.text = "출발지: ${startAddress.title}"
        fragmentRouteMapBindng.textView5.text = "도착지: ${endAddress.title}"

        // 2) MapView 라이프사이클 콜백 등록
        //fragmentRouteMapBindng.mapView.onCreate(savedInstanceState)
        // ★ this 가 OnMapReadyCallback 을 구현하므로 넘어갑니다
        fragmentRouteMapBindng.mapView.getMapAsync(this)

        // 3) BottomSheet 세팅
        bottomSheetBehavior = BottomSheetBehavior.from(fragmentRouteMapBindng.bottomSheetContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 120

        // 3) 필드 displayItems 에 경유지 목록 채우기
        displayItems = buildDisplayItems()


        // origin / destination 은 JSON parsing 할 때 저장해두셨죠?
        val originLat = origin[0].toDouble()
        val originLng = origin[1].toDouble()
        val destLat   = destination[0].toDouble()
        val destLng   = destination[1].toDouble()

        val stepAdapter = StepListAdapter(
            originName  = "출발지명",   // 예: Address.title
            originLat   = originLat,
            originLng   = originLng,
            steps       = route.steps,
            destName    = "도착지명",   // 예: Address.title
            destLat     = destLat,
            destLng     = destLng
        ) { lat, lng ->
            // 클릭된 좌표로 카메라 이동
            val target = LatLng(lat, lng)  // ensure import com.naver.maps.geometry.LatLng
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(target, 15.0))
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        fragmentRouteMapBindng.recyclerViewSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stepAdapter
        }
    }

    private fun setToolbar() {
        fragmentRouteMapBindng.toolbarRouteMap.apply {
            // 뒤로가기 버튼
            fragmentRouteMapBindng.imageViewToolbar.apply {
                setColorFilter(ContextCompat.getColor(context, android.R.color.white)
                )
                setOnClickListener { parentFragmentManager.popBackStack() }
            }
            // 툴바 제목
            fragmentRouteMapBindng.textViewToolbar.text = "경로"
        }
    }

    private fun buildDisplayItems(): List<DisplayItem> {
        val list = mutableListOf<DisplayItem>()
        // 출발지
//        list += DisplayItem(
//            title1 = "출발지",
//            title2 = startAddress.title,
//            coord  = LatLng(origin[0].toDouble(), origin[1].toDouble())
//        )
        // steps
        route.steps.forEachIndexed { idx, step ->
            when (step.mode) {
                "walk" -> route.steps.getOrNull(idx + 1)?.from?.coord?.let { coord ->
                    val lat = coord[0]
                    val lng = coord[1]
                    Log.d("RouteMap","[walk] lat=$lat, lng=$lng")
                            list += DisplayItem(
                            title1 = "걷기 ${step.duration/60}분",
                            title2 = step.description.orEmpty().removePrefix("Walk to "),
                            coord  = LatLng(lat, lng)
                        )
                    }
                "Bus", "Subway" -> step.to?.coord?.let { coord ->
                    val lat = coord[0]
                    val lng = coord[1]
                    Log.d("RouteMap","[transit] lat=$lat, lng=$lng")
                        list += DisplayItem(
                            title1 = "${step.mode} ${step.duration/60}분 ${step.line}",
                            title2 = step.to.name,
                            coord  = LatLng(lat, lng)
                        )
                    }
            }
        }
        // 도착지
//        list += DisplayItem(
//            title1 = "도착지",
//            title2 = endAddress.title,
//            coord  = LatLng(destination[0].toDouble(), destination[1].toDouble())
//        )
        return list
    }

    override fun onMapReady(naverMap: NaverMap){
        this.naverMap = naverMap

        Log.d("RouteMap", "▶ onMapReady 호출 – displayItems size=${displayItems.size}")
        // route 객체 안에 있는 좌쵸 리스트로 PolylineOverlay 그리기

        // 1) overviewPolyline 에 들어있는 [위도, 경도] 쌍을 LatLng 로 변환
        val coords: List<LatLng> = route.overviewPolyline.map { pair ->
            LatLng(pair[0], pair[1])
        }

        // 2) PathOverLay 로 지도에 그리기
        val path = PathOverlay().apply {
            this.coords = coords
            width = 10                  // 선 굵기 (픽셀)
            outlineWidth = 5          // 테두리 굵기 (dp 단위)
            outlineColor = Color.WHITE  // 테두리 색
            color = Color.BLUE          // 내부 선 색
            map = naverMap              // 지도에 붙이기
        }

        // 3) 카메라가 polyline 전체를 보여주도록 이동
        if (coords.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            coords.forEach { latLng ->
                boundsBuilder.include(latLng)
            }
            val bounds = boundsBuilder.build()
            val update = CameraUpdate.fitBounds(bounds, 100) // padding 100px
            naverMap.moveCamera(update)
        }

//        // --- 출발지 마커 ---
//        origin.getOrNull(0)?.toDoubleOrNull()?.takeIf { it.isFinite() }?.let { lat ->
//            origin.getOrNull(1)?.toDoubleOrNull()?.takeIf { it.isFinite() }?.let { lng ->
//                Marker().apply {
//                    position = LatLng(lat, lng)
//                    icon = OverlayImage.fromResource(R.drawable.map_point_blue)
//                    width = 74; height = 122
//                    captionText = "출발지"
//                    map = naverMap
//                }
//            }
//        } ?: Log.w(TAG, "출발지 좌표 유효하지 않아 스킵")
//
//        // --- 도착지 마커 ---
//        destination.getOrNull(0)?.toDoubleOrNull()?.takeIf { it.isFinite() }?.let { lat ->
//            destination.getOrNull(1)?.toDoubleOrNull()?.takeIf { it.isFinite() }?.let { lng ->
//                Marker().apply {
//                    position = LatLng(lat, lng)
//                    icon = OverlayImage.fromResource(R.drawable.map_point_blue)
//                    width = 74; height = 122
//                    captionText = "도착지"
//                    map = naverMap
//                }
//            }
//        } ?: Log.w(TAG, "도착지 좌표 유효하지 않아 스킵")


        // 3) polyline 시작점(출발지) 마커
        coords.firstOrNull()?.let { startLatLng ->
            Marker().apply {
                position = startLatLng
                icon = OverlayImage.fromResource(R.drawable.map_point_blue)
                width = 74; height = 122
                captionText = startAddress.title    // Address.title 이나 원하는 텍스트
                map = naverMap
            }
        }

        // 4) polyline 끝점(도착지) 마커
        coords.lastOrNull()?.let { endLatLng ->
            Marker().apply {
                position = endLatLng
                icon = OverlayImage.fromResource(R.drawable.map_point_blue)
                width = 74; height = 122
                captionText = endAddress.title      // Address.title 이나 원하는 텍스트
                map = naverMap
            }
        }

        // --- 중간 경유지(markers) ---
        val viaItems = displayItems.drop(1).dropLast(1)
        viaItems.forEachIndexed { idx, item ->
            // displayItems 는 buildDisplayItems()에서 LatLng 생성 시 Double 값만 들어가므로 안전
            Marker().apply {
                position = item.coord
                icon = OverlayImage.fromResource(R.drawable.map_point_orange)
                width = 74; height = 122
                captionText = item.title2
                map = naverMap
            }
        }

        // --- 출발지 / 도착지 먼저 찍기 ---
//        val originLatLng = LatLng(origin[0].toDouble(), origin[1].toDouble())
//        Marker().apply {
//            position = originLatLng
//            icon = OverlayImage.fromResource(R.drawable.map_point_blue)  // 파란 아이콘
//            width       = 74
//            height      = 122
//            captionText = "출발지"
//            map = naverMap
//        }
//
//        val destLatLng = LatLng(destination[0].toDouble(), destination[1].toDouble())
//        Marker().apply {
//            position = destLatLng
//            icon = OverlayImage.fromResource(R.drawable.map_point_blue)  // 파란 아이콘
//            width       = 74
//            height      = 122
//            captionText = "도착지"
//            map = naverMap
//        }
//
//        // --- 중간 경유지(via)만 주황색으로 찍기 ---
//        // displayItems 에서 첫(출발지)·마지막(도착지) 제외
//        val viaItems = if (displayItems.size > 2)
//            displayItems.subList(1, displayItems.size - 1)
//        else
//            emptyList()
//
//        viaItems.forEachIndexed { idx, item ->
//            Log.d("RouteMap", "  via #$idx: ${item.title1} at ${item.coord}")
//            Marker().apply {
//                position = item.coord
//                icon = OverlayImage.fromResource(R.drawable.map_point_orange)
//                width       = 74
//                height      = 122
//                captionText = item.title2
//                map = naverMap
//            }
//        }
    }

    // MapView lifecycle hook 들도 잊지 말고 호출해 주세요
    override fun onStart() {
        super.onStart()
        fragmentRouteMapBindng.mapView.onStart()
    }
    override fun onResume() {
        super.onResume()
        fragmentRouteMapBindng.mapView.onResume()
    }
    override fun onPause() {
        fragmentRouteMapBindng.mapView.onPause()
        super.onPause()
    }
    override fun onStop() {
        fragmentRouteMapBindng.mapView.onStop()
        super.onStop()
    }
    override fun onDestroyView() {
        fragmentRouteMapBindng.mapView.onDestroy()
        super.onDestroyView()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        fragmentRouteMapBindng.mapView.onLowMemory()
    }
}
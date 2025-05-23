package com.psg.navimate.View

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.psg.navimate.MainActivity
import com.psg.navimate.Model.Address
import com.psg.navimate.Model.Route
import com.psg.navimate.Model.RouteResponse
import com.psg.navimate.R
import com.psg.navimate.View.adapter.RouteListAdapter
import com.psg.navimate.ViewModel.FindWayViewModel
import com.psg.navimate.databinding.FragmentFindWayBinding
import com.psg.navimate.util.Status
import kotlinx.coroutines.launch


class FindWayFragment : Fragment() {

    lateinit var fragmentFindWayBinding: FragmentFindWayBinding
    lateinit var mainActivity: MainActivity
    lateinit var viewModel: FindWayViewModel
    lateinit var routeAdapter: RouteListAdapter

    // 선택된 주소 저장용
    private var startAddress: Address? = null
    private var endAddress: Address? = null

    // 더미 JSON 원본을 저장할 프로퍼티/ PI 호출 후 받은 JSON 전체를 저장해두고, MapFragment에 넘깁니다.
    private var rawResponseJson: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentFindWayBinding = FragmentFindWayBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        viewModel = ViewModelProvider(this)[FindWayViewModel::class.java]

        return fragmentFindWayBinding.root
    }

    // 1) NaN·Infinity 허용하는 Gson 인스턴스
    private val gsonSafe by lazy {
        GsonBuilder()
            .serializeSpecialFloatingPointValues() // NaN, Infinity 직렬화 허용
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 출발지 EditText에 클릭 리스너
        fragmentFindWayBinding.editTextStart.setOnClickListener {
            Log.d("FindWayFrag", "▶ editTextStart 클릭!")   // ① 여기 로그가 찍혀야 합니다.
            AddressSearchDialog { address ->
                startAddress = address
                fragmentFindWayBinding.editTextStart.setText(address.title)
                // viewModel에 address 저장 등
            }.show(childFragmentManager, "StartAddressDialog")
        }

        // 도착지 EditText에 클릭 리스너
        fragmentFindWayBinding.editTextEnd.setOnClickListener {
            Log.d("FindWayFrag", "▶ editTextEnd 클릭!")   // ① 여기 로그가 찍혀야 합니다.
            AddressSearchDialog { address ->
                endAddress = address
                fragmentFindWayBinding.editTextEnd.setText(address.title)
            }.show(childFragmentManager, "StartAddressDialog")
        }

        // 스왑 버튼 클릭 리스너
        fragmentFindWayBinding.imageButtonSwap.setOnClickListener {
            // 텍스트 먼저 교환
            val tmpText = fragmentFindWayBinding.editTextStart.text.toString()
            fragmentFindWayBinding.editTextStart.setText(fragmentFindWayBinding.editTextEnd.text.toString())
            fragmentFindWayBinding.editTextEnd.setText(tmpText)

            // Address 객체도 교환
            val tmpAddr = startAddress
            startAddress = endAddress
            endAddress   = tmpAddr
        }

        // Adapter 클릭 리스너에서 start/end JSON도 gsonSafe로 생성
        routeAdapter = RouteListAdapter { route, index ->
            val routeJson = rawResponseJson
                ?: error("rawResponseJson 아직 없습니다!")
            val startJson = gsonSafe.toJson(startAddress)
            val endJson   = gsonSafe.toJson(endAddress)

            // 2) 긴 JSON은 1000자 단위로 분할해서 찍으면 Logcat에 다 보입니다
            rawResponseJson
                ?.chunked(1000)
                ?.forEachIndexed { chunkIdx, chunk ->
                    Log.d("FindWayFrag", "▶ rawRespJson[$chunkIdx]: $chunk")
                }
            Log.d("FindWayFrag", "▶ startJson: $startJson")
            Log.d("FindWayFrag", "▶ endJson:   $endJson")

            val frag = RouteMapFragment.newInstance(routeJson, startJson, endJson, index)
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_container, frag)
                .addToBackStack(null)
                .commit()
        }
        // 1) 가장 직관적인 방법
        fragmentFindWayBinding.recyclerViewRouteResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = routeAdapter
        }

        // 길찾기 버튼 클릭 처리 등은 여기에 추가하세요.
        fragmentFindWayBinding.buttonFindWay.setOnClickListener {
            val start = startAddress?.title
            val end = endAddress?.title

            if(start.isNullOrBlank() || end.isNullOrBlank()){
                Toast.makeText(requireContext(), "출발지와 도착지를 모두 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 좌표 확인 로그
            Log.d("FindWayFrag", "▶ 경로 검색: (${start}) → (${end})")

            // 실제 Api 호출 대신 더미 JSON 읽어서 어뎁터에 넣기
//            val dummyRoutes = loadDummyRoutes()
//            if(dummyRoutes.isEmpty()){
//                Toast.makeText(requireContext(), "더미 데이터를 읽지 못했습니다", Toast.LENGTH_SHORT).show()
//            } else{
//                Log.d("FindWatFrag", "▶ 더미 경로 개수: ${dummyRoutes.size}")
//                routeAdapter.submitList(dummyRoutes)
//                Log.d("FindWatFrag", "▶ 더미 경로 : ${dummyRoutes}")
//            }

            // liveData + coroutines 로 비동기 호출
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.searchRoutes(start, end).observe(viewLifecycleOwner) { result ->
                    when (result.status) {
                        Status.LOADING -> {
                            Log.d("FindWayFrag", "▶ [API] 로딩 중…")
                        }
                        Status.SUCCESS -> {
                            val routes = result.data.orEmpty()
                            Log.d("FindWayFrag", "▶ [API] 성공 – 경로 개수=${routes.size}")

                            if (routes.isNotEmpty()) {
                                // 3) NaN 좌표 필터링
                                val cleanedRoutes = routes.map { route ->
                                    route.copy(
                                        overviewPolyline = route.overviewPolyline.filter { pair ->
                                            pair.size == 2
                                                    && !pair[0].isNaN() && !pair[1].isNaN()
                                                    && !pair[0].isInfinite() && !pair[1].isInfinite()
                                        }
                                    )
                                }

                                // 4) origin/destination 은 startAddress 사용
                                val originList = listOf(
                                    startAddress!!.mapx.toString(),
                                    startAddress!!.mapy.toString()
                                )
                                val destList = listOf(
                                    endAddress!!.mapx.toString(),
                                    endAddress!!.mapy.toString()
                                )

                                // 5) RouteResponse 재구성
                                val resp = RouteResponse(
                                    origin      = originList,
                                    destination = destList,
                                    routes      = cleanedRoutes
                                )

                                // 6) 안전한 gson으로 직렬화
                                rawResponseJson = gsonSafe.toJson(resp)

                                Log.d("FindWayFrag", "▶ 응답JSON: $rawResponseJson")
                                // 다시 분할 출력
//                                rawResponseJson
//                                    ?.chunked(1000)
//                                    ?.forEachIndexed { i, chunk ->
//                                        Log.d("FindWayFrag", "▶ 응답JSON[$i]: $chunk")
//                                    }
                            }

                            // 7) 어댑터에 결과 반영
                            routeAdapter.submitList(routes)
                        }
                        Status.ERROR -> {
                            Log.e("FindWayFrag", "▶ [API] 실패 – 메시지='${result.message}'")
                            Toast.makeText(
                                requireContext(),
                                "경로 검색 실패: ${result.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    // assets/dummy_routes.json을 읽어서 List<Route>로 파싱
    private fun loadDummyRoutes(): List<Route>{
        return try {
            // 더미 파일을 통째로 문자열로 읽기
            val jsonText = requireContext().assets
                .open("dummy_routes.json")
                .bufferedReader()
                .use { it.readText() }

            // raw JSON 저장
            rawResponseJson = jsonText

            // 전체를 RouteResponse 로 파싱
            val resp: RouteResponse = Gson().fromJson(jsonText, RouteResponse::class.java)

            // origin/destination → startAddress/endAddress 설정
            val originLat = resp.origin[0].toDouble()
            val originLng = resp.origin[1].toDouble()
            val destLat   = resp.destination[0].toDouble()
            val destLng   = resp.destination[1].toDouble()

            startAddress = Address(
                id = startAddress?.id.orEmpty(),
                title = startAddress?.title ?: "출발지",
                address = startAddress?.address.orEmpty(),
                roadAddress = startAddress?.roadAddress.orEmpty(),
                mapx = originLat,
                mapy = originLng
            )
            endAddress = Address(
                id = endAddress?.id.orEmpty(),
                title = endAddress?.title ?: "도착지",
                address = endAddress?.address.orEmpty(),
                roadAddress = endAddress?.roadAddress.orEmpty(),
                mapx = destLat,
                mapy = destLng
            )

            // routes 만 반환
            resp.routes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
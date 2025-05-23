package com.psg.navimate.View

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.psg.navimate.Model.Address
import com.psg.navimate.databinding.DialogAddressSearchBinding
import com.psg.navimate.util.NaverApiClient
import com.psg.navimate.util.NaverApiService
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 주소 검색 다이얼로그
 * 사용자가 입력한 키워드로 주소를 검색하고,
 * 결과 리스트에서 선택된 주소를 반환합니다.
 *
 * @param onAddressSelected 선택된 Address 객체를 전달받는 콜백
 */
class AddressSearchDialog(
    private val onAddressSelected: (Address) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddressSearchBinding? = null
    private val binding get() = _binding!!

    /**
     * JS에서 호출되는 브릿지
     */
    inner class PostcodeBridge {
        @JavascriptInterface
        fun onSelect(jsonData: String) {
            // JS 스레드 → UI 스레드로 전환
            requireActivity().runOnUiThread {
                val data = JSONObject(jsonData)
                // 아래 필드는 Daum 우편번호 서비스에서 제공하는 주요 값들
                val jibun = data.optString("jibunAddress")
                val road = data.optString("roadAddress")
                val zonecode = data.optString("zonecode")
                // data.x = 경도(longitude), data.y = 위도(latitude)
                val lng = data.optDouble("x")
                val lat = data.optDouble("y")
                val query = data.optString("query")

                val address = Address(
                    id = zonecode.ifBlank { "$lat,$lng" },
                    title = query, //if (road.isNotBlank()) road else jibun,
                    address = jibun,
                    roadAddress = road,
                    // Address.kt 에서 mapx=위도, mapy=경도 로 쓰고 계셨으니
                    mapx = lat,
                    mapy = lng
                )
                onAddressSelected(address)
                dismiss()
            }
        }
    }

    //private val apiService by lazy { NaverApiClient.create(NaverApiService::class.java)}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddressSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AddrSearchDlg", "▶ onViewCreated 호출됨")

        with(binding.webViewPostcode.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true                           // ① DOM storage 허용
            // Mixed content (HTTPS 페이지에서 HTTP 리소스도 로드) 허용
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }

        // 콘솔 로그를 찍어보게끔 WebChromeClient 확장
        binding.webViewPostcode.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Log.d("WebView", "페이지 로드 완료: $url")
            }
        }
        binding.webViewPostcode.addJavascriptInterface(PostcodeBridge(), "Android")

        // Daum 우편번호 위젯 HTML 로드
        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
  <style>
    /* html, body, 그리고 #postcode 모두 화면 꽉 채우기 */
    html, body, #postcode {
      margin: 0;
      padding: 0;
      width: 100%;
      height: 100%;
    }
  </style>
</head>
<body>
  <!-- 이 div 안에 우편번호 위젯이 그려집니다 -->
  <div id="postcode"></div>
  <script>
    new daum.Postcode({
      oncomplete: function(data) {
        Android.onSelect(JSON.stringify(data));
      },
      width : '100%',
      height: '100%'
    }).embed(document.getElementById('postcode'));
  </script>
</body>
</html>
""".trimIndent()


        binding.webViewPostcode.loadDataWithBaseURL(
            "https://postcode.map.daum.net/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }


    override fun onDestroyView() {
        binding.webViewPostcode.removeJavascriptInterface("Android")
        _binding = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            // 배경을 투명하게 해서 dialog_rounded_background 만 남도록하긔
            window.setBackgroundDrawableResource(android.R.color.transparent)

            // 화면 폭의 90% 로 설정
            val params = window.attributes
            val dm = resources.displayMetrics
            val width  = (dm.widthPixels  * 0.9).toInt()
            val height = (dm.heightPixels * 0.8).toInt()
            window.attributes = params
            window.setLayout(width, height)
        }

    }
}
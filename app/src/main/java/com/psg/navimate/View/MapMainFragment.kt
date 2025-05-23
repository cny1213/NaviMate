package com.psg.navimate.View

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.psg.navimate.MainActivity
import com.psg.navimate.databinding.FragmentMapMainBinding

class MapMainFragment : Fragment(), OnMapReadyCallback {

    lateinit var fragmentMapMainBinding: FragmentMapMainBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentMapMainBinding = FragmentMapMainBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        fragmentMapMainBinding.naverMapView.onCreate(savedInstanceState)
        fragmentMapMainBinding.naverMapView.getMapAsync(this)

        return fragmentMapMainBinding.root
    }

    override fun onMapReady(naverMap: NaverMap){

    }

    override fun onStart() {
        super.onStart()
        fragmentMapMainBinding.naverMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        fragmentMapMainBinding.naverMapView.onResume()
    }
    override fun onPause() {
        fragmentMapMainBinding.naverMapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        fragmentMapMainBinding.naverMapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        fragmentMapMainBinding.naverMapView.onDestroy()
        super.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        fragmentMapMainBinding.naverMapView.onLowMemory()
    }

}
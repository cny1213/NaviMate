package com.psg.navimate

import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentManager
import com.psg.navimate.View.FindWayFragment
import com.psg.navimate.View.MapMainFragment
import com.psg.navimate.View.SettingFragment
import com.psg.navimate.databinding.ActivityMainBinding
import com.psg.navimate.util.MainFragmentName

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    // 현재 선택된 Fragment ID 저장
    private var currentSelectedItemId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 스플래쉬 스크린이 나타나게 한다
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        bottomNaviClick()
    }

    private fun initView(){
        // 초기화 시 MapFragment를 표시
        supportFragmentManager.beginTransaction().replace(R.id.main_container, FindWayFragment()).commit()

        binding.mainBottomNavigation.apply {
            clearAnimation()

            // 명시적으로 선택된 항목을 None으로 설정
            selectedItemId = R.id.findway_item
        }

        currentSelectedItemId = R.id.findway_item
    }

    // bottomNavi 클릭 이벤트
    private fun bottomNaviClick(){
        binding.mainBottomNavigation.setOnItemSelectedListener {
            // 현재 선택된 아이템과 동일한 경우 아무 작업도 하지 않음
            if (currentSelectedItemId == it.itemId){
                return@setOnItemSelectedListener true
            }
            when(it.itemId){
                R.id.mapmain_item -> {
                    replaceFragment(MainFragmentName.MAP_MAIN_FRAGMENT, false, null)
                    true
                }
                R.id.findway_item -> {
                    replaceFragment(MainFragmentName.FIND_WAY_FRAGMENT, false, null)
                    true
                }
                R.id.setting_item -> {
                    replaceFragment(MainFragmentName.SETTING_FRAGMENT, false, null)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    // Fragment 교체
    fun replaceFragment(name: MainFragmentName, addToBackStack: Boolean, data: Bundle?){
        // 현재 표시된 프래그먼트와 같은 경우 아무 작업도 하지 않음
        if (currentSelectedItemId == name.id){
            return
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // 전환 애니메이션 추가
        fragmentTransaction.setCustomAnimations(
            android.R.anim.fade_in, // 들어올 때
            android.R.anim.fade_out, // 나갈 때
            android.R.anim.fade_in, // 다시 들어올 때 (뒤로가기)
            android.R.anim.fade_out // 다시 나갈 때 (뒤로가기)
        )

        val fragment = when (name){
            MainFragmentName.MAP_MAIN_FRAGMENT -> MapMainFragment()
            MainFragmentName.FIND_WAY_FRAGMENT -> FindWayFragment()
            MainFragmentName.SETTING_FRAGMENT -> SettingFragment()

        }

        if (data != null){
            fragment.arguments = data
        }

        fragmentTransaction.replace(R.id.main_container, fragment)

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(name.str)
        }
        fragmentTransaction.commit()

        // 현재 표시된 프래그먼트 이름 업데이트
        currentSelectedItemId = name.id
        binding.mainBottomNavigation.selectedItemId = name.id
    }

    fun removeFragment(dataInquiryFragment: MainFragmentName) {
        SystemClock.sleep(200)
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}

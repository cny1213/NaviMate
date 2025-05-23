package com.psg.navimate.View

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.psg.navimate.MainActivity
import com.psg.navimate.R
import com.psg.navimate.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    lateinit var fragmentSettingBinding: FragmentSettingBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentSettingBinding = FragmentSettingBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        return fragmentSettingBinding.root
    }
}
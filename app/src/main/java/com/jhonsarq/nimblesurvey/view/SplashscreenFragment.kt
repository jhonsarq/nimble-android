package com.jhonsarq.nimblesurvey.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jhonsarq.nimblesurvey.databinding.FragmentSplashscreenBinding

@SuppressLint("CustomSplashScreen")
class SplashscreenFragment : Fragment() {
    private var _binding: FragmentSplashscreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashscreenBinding.inflate(inflater, container, false)

        return binding.root
    }
}
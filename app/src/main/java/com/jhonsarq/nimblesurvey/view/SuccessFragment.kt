package com.jhonsarq.nimblesurvey.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.jhonsarq.nimblesurvey.R
import com.jhonsarq.nimblesurvey.databinding.FragmentSuccessBinding
import com.jhonsarq.nimblesurvey.viewmodel.SurveysViewModel
import pl.droidsonroids.gif.GifDrawable

class SuccessFragment : DialogFragment() {
    private var _binding: FragmentSuccessBinding? = null
    private val binding get() = _binding!!
    private val surveysViewModel: SurveysViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSuccessBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gifDrawable = GifDrawable(resources, R.drawable.success)
        gifDrawable.loopCount = 1
        binding.gifImageView.setImageDrawable(gifDrawable)

        surveysViewModel.survey.observe(viewLifecycleOwner) { survey ->
            if(survey != null) {
                binding.text.text = survey.thanksText
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            surveysViewModel.setSuccessClosed(true)

            dismiss()
        }, 3000)
    }
}
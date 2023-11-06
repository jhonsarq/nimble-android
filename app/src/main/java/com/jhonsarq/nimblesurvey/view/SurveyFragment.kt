package com.jhonsarq.nimblesurvey.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.jhonsarq.nimblesurvey.R
import com.jhonsarq.nimblesurvey.databinding.FragmentSurveyBinding
import com.jhonsarq.nimblesurvey.viewmodel.LoaderViewModel
import com.jhonsarq.nimblesurvey.viewmodel.SurveysViewModel
import com.jhonsarq.nimblesurvey.viewmodel.UserViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class SurveyFragment : Fragment() {
    private var _binding: FragmentSurveyBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val surveysViewModel: SurveysViewModel by activityViewModels()
    private val loaderViewModel: LoaderViewModel by activityViewModels()
    private val takeSurveyFragment = TakeSurveyFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSurveyBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loaderViewModel.setLoader(true)

        surveysViewModel.surveyId.observe(viewLifecycleOwner) { surveyId ->
            val user = userViewModel.user.value!!

            surveysViewModel.getSurvey(user.accessToken!!, surveyId!!)
        }

        surveysViewModel.survey.observe(viewLifecycleOwner) { survey ->
            if(survey != null) {
                Picasso.get().load(survey.coverImageUrl).into(binding.image, object : Callback {
                    override fun onSuccess() {
                        loaderViewModel.setLoader(false)
                    }

                    override fun onError(e: Exception?) {
                        loaderViewModel.setLoader(false)
                    }
                })

                binding.title.text = survey.title
                binding.description.text = survey.description
            }
        }

        binding.startSurvey.setOnClickListener {
            if(!takeSurveyFragment.isVisible) {
                takeSurveyFragment.isCancelable = false
                takeSurveyFragment.show(requireActivity().supportFragmentManager, "take_survey_fragment")
            }
        }
    }
}
package com.jhonsarq.nimblesurvey.view

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jhonsarq.nimblesurvey.R
import com.jhonsarq.nimblesurvey.adapters.SurveyAdapter
import com.jhonsarq.nimblesurvey.databinding.FragmentHomeBinding
import com.jhonsarq.nimblesurvey.model.RefreshTokenRequest
import com.jhonsarq.nimblesurvey.model.Survey
import com.jhonsarq.nimblesurvey.utilities.Constants
import com.jhonsarq.nimblesurvey.utilities.Utils
import com.jhonsarq.nimblesurvey.viewmodel.LoaderViewModel
import com.jhonsarq.nimblesurvey.viewmodel.SurveysViewModel
import com.jhonsarq.nimblesurvey.viewmodel.UserViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val loaderViewModel: LoaderViewModel by activityViewModels()
    private val surveysViewModel: SurveysViewModel by activityViewModels()
    private val constants = Constants()
    private val utils = Utils()
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val surveysContainer = binding.surveys

        loaderViewModel.setLoader(true)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if(user?.name != null) {
                val expireDate = user.createdAt!! + user.expiresIn!!
                val currentDate = utils.currentDateToSeconds()

                if(currentDate > expireDate) {
                    val parameters = RefreshTokenRequest("refresh_token", user.refreshToken!!, constants.clientId, constants.clientSecret)

                    userViewModel.refreshToken(user, requireContext(), parameters)
                } else {
                    if(firstLoad) {
                        surveysViewModel.getSurveys(user.accessToken!!)

                        firstLoad = false
                    }
                }
            }
        }

        surveysViewModel.surveys.observe(viewLifecycleOwner) { surveys ->
            if(surveys != null) {
                val surveysCategories: MutableMap<String, MutableList<Survey>> = mutableMapOf()

                for(survey in surveys) {
                    if(!surveysCategories.containsKey(survey.type)) {
                        surveysCategories[survey.type] = mutableListOf()
                    }

                    surveysCategories[survey.type]?.add(survey)
                }

                for((key, value) in surveysCategories) {
                    val textView = TextView(requireContext())
                    val recyclerView = RecyclerView(requireContext())
                    val textLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    val recyclerLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                    textLayoutParams.setMargins(15,0,15, 10)
                    recyclerLayoutParams.setMargins(0,0,0,50)

                    textView.layoutParams = textLayoutParams
                    textView.text = key
                    textView.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.NORMAL)

                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)

                    recyclerView.layoutParams = recyclerLayoutParams
                    recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

                    recyclerView.adapter = SurveyAdapter(value) { id ->
                        surveysViewModel.cleanSurvey()
                        surveysViewModel.setSurveyId(id)

                        findNavController().navigate(R.id.action_homeFragment_to_surveyFragment)
                    }

                    surveysContainer.addView(textView)
                    surveysContainer.addView(recyclerView)
                }
            }

            loaderViewModel.setLoader(false)
        }
    }
}
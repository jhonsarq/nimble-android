package com.jhonsarq.nimblesurvey.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.jhonsarq.nimblesurvey.R
import com.jhonsarq.nimblesurvey.databinding.FragmentLoginBinding
import com.jhonsarq.nimblesurvey.model.LoginRequest
import com.jhonsarq.nimblesurvey.utilities.Constants
import com.jhonsarq.nimblesurvey.utilities.Utils
import com.jhonsarq.nimblesurvey.viewmodel.LoaderViewModel
import com.jhonsarq.nimblesurvey.viewmodel.UserViewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val loaderViewModel: LoaderViewModel by activityViewModels()
    private val constants = Constants()
    private val utils = Utils()
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val back = binding.back
        val logoContainer = binding.logoContainer
        val loginContainer = binding.loginContainer
        val forgotButton = binding.forgotButton
        val loginButton = binding.loginButton

        val fadeLogoAnimation = AlphaAnimation(0f, 1f).apply {
            duration = 1000L
            startOffset = 1000L
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    val layoutParams = logoContainer.layoutParams
                    val transition = AutoTransition()

                    layoutParams.height = 0
                    transition.duration = 1000

                    TransitionManager.beginDelayedTransition(logoContainer, transition)

                    logoContainer.layoutParams = layoutParams

                    val fadeFormAnimation = AlphaAnimation(0f, 1f).apply {
                        duration = 1000L
                        startOffset = 0L
                    }

                    val fadeBackAnimation = AlphaAnimation(1f, 0f).apply {
                        duration = 1000L
                        startOffset = 0L
                    }

                    back.visibility = View.GONE
                    back.startAnimation(fadeBackAnimation)
                    loginContainer.visibility = View.VISIBLE
                    loginContainer.startAnimation(fadeFormAnimation)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        logoContainer.visibility = View.VISIBLE

        if(firstLoad) {
            firstLoad = false
            logoContainer.startAnimation(fadeLogoAnimation)
        } else {
            val layoutParams = logoContainer.layoutParams

            back.visibility = View.GONE
            loginContainer.visibility = View.VISIBLE
            layoutParams.height = 0
            logoContainer.layoutParams = layoutParams
        }

        forgotButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

        loginButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if(email != "" && password != "") {
                loaderViewModel.setLoader(true)

                val parameters = LoginRequest("password", email, password, constants.clientId, constants.clientSecret)

                userViewModel.login(parameters)
            } else {
                Toast.makeText(requireContext(), "You must fill all fields to continue!", Toast.LENGTH_SHORT).show()
            }

            utils.hideKeyboard(requireActivity(), view)
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if(user?.success != null) {
                if(user.success) {
                    if(user.name == null) {
                        userViewModel.getProfile(user, requireContext())
                    }
                } else {
                    Toast.makeText(requireContext(), user.message, Toast.LENGTH_SHORT).show()
                    closeLoader()
                }
            }
        }
    }

    private fun closeLoader() {
        loaderViewModel.setLoader(false)
    }
}
package com.jhonsarq.nimblesurvey.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.jhonsarq.nimblesurvey.databinding.FragmentResetPasswordBinding
import com.jhonsarq.nimblesurvey.model.ResetPasswordEmailRequest
import com.jhonsarq.nimblesurvey.model.ResetPasswordRequest
import com.jhonsarq.nimblesurvey.utilities.Constants
import com.jhonsarq.nimblesurvey.utilities.Utils
import com.jhonsarq.nimblesurvey.viewmodel.LoaderViewModel
import com.jhonsarq.nimblesurvey.viewmodel.UserViewModel

class ResetPasswordFragment : Fragment() {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val loaderViewModel: LoaderViewModel by activityViewModels()
    private val constants = Constants()
    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resetPasswordButton.setOnClickListener {
            val email = binding.email.text.toString().trim()

            if(email != "") {
                loaderViewModel.setLoader(true)

                val parameters = ResetPasswordRequest(ResetPasswordEmailRequest(email), constants.clientId, constants.clientSecret)

                userViewModel.resetPassword(parameters)

                binding.email.setText("")
            } else {
                Toast.makeText(requireContext(), "You must fill email field to continue!", Toast.LENGTH_SHORT).show()
            }

            utils.hideKeyboard(requireActivity(), view)
        }

        userViewModel.message.observe(viewLifecycleOwner) { message ->
            if(message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                loaderViewModel.setLoader(false)
            }
        }
    }
}
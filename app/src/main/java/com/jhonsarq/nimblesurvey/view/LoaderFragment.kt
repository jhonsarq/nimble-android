package com.jhonsarq.nimblesurvey.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.jhonsarq.nimblesurvey.R
import com.jhonsarq.nimblesurvey.databinding.FragmentLoaderBinding
import com.jhonsarq.nimblesurvey.viewmodel.LoaderViewModel

class LoaderFragment : DialogFragment() {
    private var _binding: FragmentLoaderBinding? = null
    private val binding get() = _binding!!
    private val loaderViewModel: LoaderViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoaderBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loaderViewModel.loader.observe(viewLifecycleOwner) { loader ->
            if(loader != null) {
                if(!loader) {
                    loaderViewModel.setLoader(null)
                    dismiss()
                }
            }
        }
    }
}
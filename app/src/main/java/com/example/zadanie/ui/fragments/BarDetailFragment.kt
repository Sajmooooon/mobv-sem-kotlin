package com.example.zadanie.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentDetailBarBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.DetailViewModel

class BarDetailFragment : Fragment() {
//    private lateinit var binding: FragmentDetailBarBinding
    private var _binding: FragmentDetailBarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DetailViewModel
    private val navigationArgs: BarDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        ).get(DetailViewModel::class.java)

//        viewModel.id.observe()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            Navigation.findNavController(view).navigate(R.id.action_to_login)
            return
        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
        }.also { bnd ->
            bnd.back.setOnClickListener { it.findNavController().popBackStack() }

            bnd.mapButton.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "geo:0,0,?q=" +
                                    "${viewModel.bar.value?.lat ?: 0}," +
                                    "${viewModel.bar.value?.lon ?: 0}" +
                                    "(${viewModel.bar.value?.name ?: ""}"
                        )
                    )
                )
            }
            bnd.back.setOnClickListener {
                it.findNavController().popBackStack()
            }
        }

        viewModel.loadBar(navigationArgs.id)

        viewModel.bar.observe(viewLifecycleOwner) {
            viewModel.bar.value?.let {
                binding.value.visibility = View.VISIBLE
            }
        }
//        binding.value.isVisible = true

//        viewModel.loading.observe(viewLifecycleOwner) {
//            if(it){
//                binding.value.isVisible = true
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
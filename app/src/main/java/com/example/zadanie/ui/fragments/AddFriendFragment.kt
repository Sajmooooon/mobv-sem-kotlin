package com.example.zadanie.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentAddFriendBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.AddFriendViewModel


class AddFriendFragment : Fragment() {
//    private lateinit var binding: FragmentAddFriendBinding
    private var _binding: FragmentAddFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewmodel: AddFriendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        ).get(AddFriendViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_add_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        kontrola usera
        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            Navigation.findNavController(view).navigate(R.id.action_to_login)
            return
        }
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewmodel
        }
        binding.back.setOnClickListener {
            it.findNavController().popBackStack()
        }


        viewmodel.friendAdd.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                if (it) {
                    viewmodel.show("You add friend successfully.")
                }
            }
        }

        binding.add.setOnClickListener {
            if (binding.username.text.toString().isNotBlank()) {
                //it.findNavController().popBackStack(R.id.bars_fragment,false)
                viewmodel.add(
                    binding.username.text.toString(),

                )
            }
            else {
            viewmodel.show("Fill in username")
            }
        }

//        binding.friendList.setOnClickListener {
//
////            it.findNavController().navigate(R.id.action_to_locate)
//            it.findNavController().navigate(R.id.action_to_friends)
//        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    }
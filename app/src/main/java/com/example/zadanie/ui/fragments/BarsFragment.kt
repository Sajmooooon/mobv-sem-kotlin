package com.example.zadanie.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentAddFriendBinding
import com.example.zadanie.databinding.FragmentBarsBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.BarsViewModel
import com.example.zadanie.ui.viewmodels.data.MyLocation
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class BarsFragment : Fragment() {
//    private lateinit var binding: FragmentBarsBinding
    private var _binding: FragmentBarsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewmodel: BarsViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                if(viewmodel.locationBtn.value == true){
                    Navigation.findNavController(requireView()).navigate(R.id.action_to_locate)

                }
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewmodel.show("Only approximate location access granted.")
                // Only approximate location access granted.
            }
            else -> {
                viewmodel.show("Location access denied.")
                // No location access granted.
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        ).get(BarsViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            Navigation.findNavController(view).navigate(R.id.action_to_login)
            return
        }
//        zmazat pri uprave sortu
//        viewmodel.refreshData()
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewmodel
        }.also { bnd ->
//            bnd.bottomNavigationView.itemIconTintList = null;
            bnd.bottomNavigation.setOnItemSelectedListener {
                // do stuff
                when(it.itemId){
                    R.id.menuBars ->{
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_bars)
                        return@setOnItemSelectedListener true
                    }
                    R.id.menuFriends ->{
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_friends)
                        return@setOnItemSelectedListener true
                    }
                    R.id.menuLocation ->{
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_locate)
                        return@setOnItemSelectedListener true
                    }
                }
                false
            }
//            bnd.bottomNavigation.selectedItemId(R.id.menuBars)
            bnd.bottomNavigation.getMenu().findItem(R.id.menuBars).setChecked(true);

//            sort
            bnd.sortName.setOnClickListener {
                //                viewmodel.sortByBar()

                viewmodel.sortBy("barAsc","barDesc")
            }
            bnd.sortUsers.setOnClickListener {
                viewmodel.sortBy("usersAsc","usersDesc")
            }

            bnd.sortDistance.setOnClickListener {
                if (checkPermissions()) {
                    loadData()
                }
                else{
                    locationPermissionRequest.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            bnd.logout.setOnClickListener {
                PreferenceData.getInstance().clearData(requireContext())
                Navigation.findNavController(it).navigate(R.id.action_to_login)
            }

            bnd.swiperefresh.setOnRefreshListener {
                viewmodel.refreshData()
            }

//
//            bnd.findBar.setOnClickListener {
//                if (checkPermissions()) {
//                    it.findNavController().navigate(R.id.action_to_locate)
//                } else {
//                    viewmodel.switch()
//                    locationPermissionRequest.launch(
//                        arrayOf(
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                        )
//                    )
//                }
//
//            }
//            bnd.addFriendBtn.setOnClickListener{
//                if (checkPermissions()) {
//
//                    loadData()
//
//
//                }
//                else{
//                    locationPermissionRequest.launch(
//                        arrayOf(
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                        )
//                    )
//                }
//
//                it.findNavController().navigate(R.id.action_to_add)
//            }
        }

        viewmodel.loading.observe(viewLifecycleOwner) {
            binding.swiperefresh.isRefreshing = it
        }
//        observovanie zmeny sortu
        viewmodel.sortType.observe(viewLifecycleOwner){
            viewmodel.sort()
        }
//        pri nacitani novych dat - sort
//        viewmodel.bars.observe(viewLifecycleOwner){
//            viewmodel.sort()
//        }
        viewmodel.message.observe(viewLifecycleOwner) {
            if (PreferenceData.getInstance().getUserItem(requireContext()) == null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
            }
        }
//       ked sa naplni mylocation tak sa sortnu data
        viewmodel.myLocation.observe(viewLifecycleOwner){
//            viewmodel.sortBy("distanceAsc","distanceDesc")
            viewmodel.getDistance()
            viewmodel.sortBy("distanceAsc","distanceDesc")
            viewmodel.loading.postValue(false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadData() {
        if (checkPermissions()) {
            viewmodel.loading.postValue(true)
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder().setDurationMillis(30000)
                    .setMaxUpdateAgeMillis(60000).build(), null
            ).addOnSuccessListener {
                it?.let {
                    viewmodel.myLocation.postValue(MyLocation(it.latitude, it.longitude))

                } ?: viewmodel.loading.postValue(false)
            }

        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
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
import com.example.zadanie.R
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

    //    detekcia ked sa ziskaju permissie na polohu
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                if (viewmodel.locationBtn == true) {
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
                viewmodel.switchToLocation(false)
                // No location access granted.
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//      attach viewmodel to fragment
//      viewmodelprovider utility that provides viewmodel for scope

        viewmodel = ViewModelProvider(
//            prvy parameter scope that owns viewmodelstore
//            druhy je Factory
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

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewmodel
        }.also { bnd ->
            bnd.friendsScreen.setOnClickListener{
                Navigation.findNavController(requireView()).navigate(R.id.action_to_friends)
            }
//            pri kliknuti na bars ikonu refreshnu sa daa
            bnd.barsScreen.setOnClickListener{
                viewmodel.refreshData()
//                Navigation.findNavController(requireView()).navigate(R.id.action_to_bars)
            }
            bnd.locationScreen.setOnClickListener{
                if (checkPermissions()) {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_to_locate)
                } else {
                    viewmodel.switchToLocation(true)
                    locationPermissionRequest.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
//                Navigation.findNavController(requireView()).navigate(R.id.action_to_locate)
            }


//            sort
            bnd.sortName.setOnClickListener {
                viewmodel.sortBy("barAsc", "barDesc")
            }
            bnd.sortUsers.setOnClickListener {
                viewmodel.sortBy("usersAsc", "usersDesc")
            }

//            pri sortDistance nacitaj data do mylocation
            bnd.sortDistance.setOnClickListener {
                loadData()
            }

//            pri logout - vymaz preferencedata a navig. to login
            bnd.logout.setOnClickListener {
                PreferenceData.getInstance().clearData(requireContext())
                Navigation.findNavController(it).navigate(R.id.action_to_login)
            }

//            refresh
            bnd.swiperefresh.setOnRefreshListener {
                viewmodel.refreshData()
            }

        }

//        viewLifecycleOwner reprezentuje fragments view lifecycle -
        viewmodel.loading.observe(viewLifecycleOwner) {
            binding.swiperefresh.isRefreshing = it
        }
//        observovanie zmeny typu sortu
//        pri kliknuti na sort btn sa zacne sortovat pre konkretny typ
        viewmodel.sortType.observe(viewLifecycleOwner) {
            viewmodel.sort()
        }
//        pri nacitani novych dat - sort
        viewmodel.bars.observe(viewLifecycleOwner){

            viewmodel.bars.value?.let {
//                kontrola aby nedoslo k zacyklenu pri sorte
                if (!viewmodel.alreadySorted){
                    viewmodel.sort()

                }
                else{
//                    ak bol prave sortnuty tak prepne na false aby sa dal znova pouzit
                    viewmodel.switchSorted()
                }
            }

        }

        viewmodel.message.observe(viewLifecycleOwner) {
            if (PreferenceData.getInstance().getUserItem(requireContext()) == null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
            }
        }

//       ked sa naplni mylocation tak sa sortnu data
        viewmodel.myLocation.observe(viewLifecycleOwner) {
//            viewmodel.getDistance()
            viewmodel.sortBy("distanceAsc", "distanceDesc")
            viewmodel.loading.postValue(false)
        }
    }

    @SuppressLint("MissingPermission")
//    ak su permissie tak ziska mylocation
    private fun loadData() {
        if (checkPermissions()) {
            viewmodel.loading.postValue(true)
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder().setDurationMillis(30000)
                    .setMaxUpdateAgeMillis(60000).build(), null
            ).addOnSuccessListener {
                it?.let {
                    viewmodel.myLocation.postValue(MyLocation(it.latitude, it.longitude))

                } ?: run {
                    viewmodel.loading.postValue(false)
                    viewmodel.show("Cant get your location")
                }

            }

        }
        else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
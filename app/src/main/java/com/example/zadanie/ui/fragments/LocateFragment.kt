package com.example.zadanie.ui.fragments

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.zadanie.GeofenceBroadcastReceiver
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentLocateBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.LocateViewModel
import com.example.zadanie.ui.viewmodels.data.MyLocation
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import com.example.zadanie.ui.widget.nearbyBars.NearbyBarsEvents
import com.google.android.gms.location.*

class LocateFragment : Fragment() {
//    private lateinit var binding: FragmentLocateBinding

    private var _binding: FragmentLocateBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewmodel: LocateViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient

//  background location - sa vyuziva ked constantly shares location with other users
//  kontrola ci sa ziskala likacia a ak nie vypise sa hlaska
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                // Precise location access granted.
            }
            else -> {
                viewmodel.show("Background location access denied.")
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        ).get(LocateViewModel::class.java)


//        vytvorenie instancie providera
//      provider na ziskanie poslednej polohy zariadenia
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
    }


    fun anim(){
        val anim = binding.lottieAnim
        anim.addAnimatorListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                anim.isVisible = false
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        } )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocateBinding.inflate(inflater, container, false)
        anim()
        return binding.root
    }

    @SuppressLint("MissingPermission")
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
            binding.back.setOnClickListener {
                it.findNavController().popBackStack()
            }


            bnd.swiperefresh.setOnRefreshListener {
                loadData()
            }


            bnd.lottieLoading.setOnClickListener {
                viewmodel.myBar.value?.let {
                    if (checkBackgroundPermissions()) {
                        viewmodel.checkMe()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissionDialog()
                        }
                    }
                }

            }

            bnd.nearbyBars.events = object : NearbyBarsEvents {
                override fun onBarClick(nearbyBar: NearbyBar) {
                    viewmodel.myBar.postValue(nearbyBar)
                }

            }
        }
        viewmodel.loading.observe(viewLifecycleOwner) {
            binding.swiperefresh.isRefreshing = it
        }
        viewmodel.emptyBar.observe(viewLifecycleOwner) {
//            ak sa nenacita ziaden bar, tak sa skryje lottie
            if(viewmodel.emptyBar.value == true){
                binding.lottieLoading.isGone = true
            }
//            ak sa nacita bar, tak za ukonci lottie animacia
            else{
                val anim = binding.lottieLoading
                anim.playAnimation()
                anim.speed = 2F
                anim.setMinAndMaxFrame(40,60)
            }

        }
//        pri zacati REST
        viewmodel.startRest.observe(viewLifecycleOwner) {
           if(viewmodel.startRest.value == true) {
                val anim = binding.lottieLoading
                anim.playAnimation()
                anim.setMinAndMaxFrame(20,40)
//                anim.progress = 1F
            }
        }


//        pri nacitani dat sa stopne animacia
//        viewmodel.myBar.observe(viewLifecycleOwner) {
//            viewmodel.myBar.value?.let {
////                val anim = binding.lottieLoading
////                anim.playAnimation()
////                anim.speed = 2F
////                anim.setMinAndMaxFrame(40,60)
////                anim.progress = 1F
//            }
//        }

//        pri checkine vypise spravu a vytvori fence
        viewmodel.checkedIn.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                if (it) {
                    viewmodel.show("Successfully checked in.")
                    viewmodel.myLocation.value?.let {
                        createFence(it.lat, it.lon)
                    }
                }
            }
        }

//        pri ziskani permissi sa ziska mylocation
        if (checkPermissions()) {
            loadData()
        } else {
            Navigation.findNavController(requireView()).navigate(R.id.action_to_bars)
        }

        viewmodel.message.observe(viewLifecycleOwner) {
            if (PreferenceData.getInstance().getUserItem(requireContext()) == null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
            }


        }

    }

    @SuppressLint("MissingPermission")
    private fun loadData() {
        val anim = binding.lottieLoading
        if (checkPermissions()) {
            viewmodel.loading.postValue(true)
            anim.isVisible = true
            anim.playAnimation()
            anim.setMinAndMaxFrame(0,10)
//            ziskanie presnej lokacie
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder().setDurationMillis(30000)
                    .setMaxUpdateAgeMillis(60000).build(), null
            ).addOnSuccessListener {
                it?.let {
                    anim.playAnimation()
                    anim.setMinAndMaxFrame(10,20)
                    viewmodel.myLocation.postValue(MyLocation(it.latitude, it.longitude))
                } ?: run{
                    anim.isGone = true
                    viewmodel.loading.postValue(false)
                    viewmodel.show("Cant get Location check GPS")

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createFence(lat: Double, lon: Double) {
        if (!checkPermissions()) {
            viewmodel.show("Geofence failed, permissions not granted.")
        }
//        sluzi na handlovanie geofende transition
//        ziska update ked event nastane
        val geofenceIntent = PendingIntent.getBroadcast(
            requireContext(), 0,
            Intent(requireContext(), GeofenceBroadcastReceiver::class.java),
//           ziskanie rovnakeho pendingu
            PendingIntent.FLAG_UPDATE_CURRENT or FLAG_MUTABLE
        )
//        vytvorenie geofence
//        vytvori sa kruh v okolo 300 metrov
//        expiration duration sa nastavi na den
//        transition type - alert ked odide z kruhu
        val request = GeofencingRequest.Builder().apply {
            addGeofence(
                Geofence.Builder()
                    .setRequestId("mygeofence")
                    .setCircularRegion(lat, lon, 300F)
                    .setExpirationDuration(1000L * 60 * 60 * 24)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            )
        }.build()

//        pridanie geofenciungu
//        kontrola ci sa vytvoril geofence alebo nie
        geofencingClient.addGeofences(request, geofenceIntent).run {
            addOnSuccessListener {
//              spusti animaciu pri pridani
                val anim = binding.lottieAnim
                anim.isVisible = true
                anim.resumeAnimation()
//                Navigation.findNavController(requireView()).navigate(R.id.action_to_bars)
            }
            addOnFailureListener {
//                binding.lottieLoading.isGone = false
                viewmodel.show("Geofence failed to create.") //permission is not granted for All times.
                it.printStackTrace()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun permissionDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Background location needed")
                setMessage("Allow background location (All times) for detecting when you leave bar.")
                setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id ->
                        locationPermissionRequest.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        )
                    })
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog.show()
    }

//    vyzaduje sa pri android 10 alebo vyssich - request background location access
    private fun checkBackgroundPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

//    kontrola permissi, Fine - co najpresnejsia lokacia, Coarse - max 3 km
//    kontrola oboch lebo system moze ignorovat samotnu Fine location
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
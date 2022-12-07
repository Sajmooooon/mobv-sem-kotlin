package com.example.zadanie

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.MenuProvider
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.nav_view)
            .setupWithNavController(navController)


//        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
//            if (nd.id == R.id.bars_fragment || nd.id == R.id.friends_fragment || nd.id == R.id.locate_fragment) {
////                navView.visibility = View.VISIBLE
//                Log.d("visible", "true")
//            } else {
//                Log.d("visible", "false")
//            }
//        }

//        addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                // Add menu items here
//                menuInflater.inflate(R.menu.menu, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                // Handle the menu selection
//                return true
//            }
//
//        })
    }



//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
////        val inflater: MenuInflater = menuInflater
////        inflater.inflate(R.menu.menu, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
    }


}


package com.example.weatherapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.fragments.TodayFragment
import com.example.weatherapp.util.LocalKeyStorage
import com.example.weatherapp.viewModel.LocationViewModel
import com.example.weatherapp.viewModel.MainViewModel
//import com.example.weatherapp.utils.permissionUtils
import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    protected var mLastLocation: Location? = null
    lateinit var localKeyStorage : LocalKeyStorage
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController


        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id){
                R.id.aboutFragment -> nav_view.visibility = View.GONE
                R.id.feedbackFragment -> nav_view.visibility = View.GONE
                R.id.locationFragment -> nav_view.visibility = View.GONE
                else -> nav_view.visibility = View.VISIBLE
            }
        }
        setSupportActionBar(topAppBar)
        val actionBar = supportActionBar
        actionBar?.title = " "

        if (!checkPermissions()) {
            requestPermissions()
        }
        else {
            getLastLocation()
        }

        //on click of location text
        txtlocation.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_locationFragment)
        }
        icsrch.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_locationFragment)
        }
        localKeyStorage = LocalKeyStorage(this)
        binding.txtlocation.text = localKeyStorage.getValue(LocalKeyStorage.cityName)

        //Navigation Drawer Toggle
        val drawerToggle : ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            topAppBar,
            (R.string.open),
            (R.string.close)
        ){

        }
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

//        val switch = findViewById<SwitchCompat>(R.id.switch_id)
//        switch.setOnClickListener {
//            if(switch.isChecked){
//                Toast.makeText(this,"Touch the button", Toast.LENGTH_SHORT).show()
//            }
//        }

        val menu = binding.navView.menu
        val menuItem = menu.findItem(R.id.conversion)
        if(menuItem.isChecked){
            localKeyStorage.saveValue(LocalKeyStorage.FAHRENHEIT , "true")
        }
        else{
            localKeyStorage.saveValue(LocalKeyStorage.FAHRENHEIT , "false")
        }

//        val view = MenuItemCompat.getActionView(menuItem)
        val view = menuItem.actionView
        val switch : SwitchCompat = view.findViewById(R.id.switch_id)
        switch.isChecked = localKeyStorage.getValue("isFahrenheit") == "true"
        switch.setOnCheckedChangeListener { _, isChecked ->

            localKeyStorage.saveValue(LocalKeyStorage.FAHRENHEIT , isChecked.toString())

//            val bundle = Bundle()
//            bundle.putBoolean("isCelsius" , isChecked)
            Log.d("togglemain", isChecked.toString())
            navController.navigate(R.id.action_homeFragment_self)
//        }

//        val switchh : SwitchCompat = findViewById(R.id.conSwitch)
//        switchh.isChecked = localKeyStorage.getValue("isFahrenheit") == "true"
//        switchh.setOnCheckedChangeListener { _, isChecked ->
//
//            localKeyStorage.saveValue(LocalKeyStorage.FAHRENHEIT , isChecked.toString())
//            Log.d("togglemain", isChecked.toString())
//            navController.navigate(R.id.action_homeFragment_self)

//            finish()
//            startActivity(intent)

        }



    }
    //Implemented Item Selected listener

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
//            R.id.conversion ->{
//             //   Toast.makeText(this,"Touch the button", Toast.LENGTH_SHORT).show()
//            }

            R.id.about ->{

                navController.navigate(R.id.action_homeFragment_to_aboutFragment)
                drawerLayout.close()

            }
            R.id.license ->{
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))

            }
            R.id.feedback ->{

                navController.navigate(R.id.action_homeFragment_to_feedbackFragment)
                drawerLayout.close()

            }
//            else -> Toast.makeText(this,"Error", Toast.LENGTH_SHORT).show()
        }
        return true
    }


    //location vala part




//    public override fun onStart() {
//        super.onStart()
//
//        if (!checkPermissions()) {
//            requestPermissions()
//        } else {
//            getLastLocation()
//        }
//    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    mLastLocation = task.result

                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)

                }
            }
    }



    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {

        Toast.makeText(this@MainActivity, getString(mainTextStringId), Toast.LENGTH_LONG).show()
    }


    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {

        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE)

    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                })

        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation()
            } else {


                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                    View.OnClickListener {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package",
                            BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })

            }
        }
    }

    companion object {

        private val TAG = "LocationProvider"

        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

}
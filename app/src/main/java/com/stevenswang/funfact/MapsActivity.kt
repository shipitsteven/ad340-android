package com.stevenswang.funfact

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.stevenswang.funfact.databinding.ActivityMapsBinding
import com.stevenswang.funfact.model.Camera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val FINE_LOCATION_RQ = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12f))
        val seattle = LatLng(47.608013, -122.335167)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seattle))
        getLocationData()
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(
                    applicationContext,
                    "$name permission granted from check",
                    Toast.LENGTH_SHORT
                ).show()
                getLastLocation(fusedLocationClient)
            }
            shouldShowRequestPermissionRationale(permission) -> showDialog(
                permission,
                name,
                requestCode
            )
            else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this function")
            setTitle("Permission required")
            setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(
                    this@MapsActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    applicationContext,
                    "$name permission refused from grant",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                getLastLocation(fusedLocationClient)
            }
        }
        when (requestCode) {
            FINE_LOCATION_RQ -> innerCheck("location")
        }
    }

    private fun getLocationData() {
        val api = ApiObject.getApiObject()!!

        CoroutineScope(Dispatchers.IO).launch {
            val response = api.getCamData().awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                withContext(Dispatchers.Main) {
                    checkForPermissions(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        "location",
                        FINE_LOCATION_RQ
                    )
                    repopulateMap(data)
                }
            }
        }
    }

    private fun repopulateMap(data: Camera) {
        mMap.clear()
        for (index: Int in data.Features.indices) {
            val locationArray = data.Features[index].PointCoordinate
            val location = LatLng(locationArray[0], locationArray[1])
            mMap.addMarker(
                MarkerOptions().position(location)
                    .title(data.Features[index].Cameras[0].Description)
            )
        }
    }

    private fun moveMapCameraToUser(location: Location) {
        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .title("My Location")
                .icon(
                    BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
        )
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12f))
        mMap.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    location.latitude,
                    location.longitude
                )
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(locationProvider: FusedLocationProviderClient) {
        locationProvider.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    moveMapCameraToUser(location)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Your location cannot be determined, try opening another Map app to initialize it",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong while retrieving your location :(",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}


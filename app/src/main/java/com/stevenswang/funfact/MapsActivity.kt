package com.stevenswang.funfact

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.libraries.places.api.Places
import com.stevenswang.funfact.databinding.MapContainerBinding
import com.stevenswang.funfact.model.Camera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: MapContainerBinding
    private val FINE_LOCATION_RQ = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initialize(applicationContext, BuildConfig.apiKey)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12f))
        val seattle = LatLng(47.608013, -122.335167)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seattle))
        if (hasInternetConnection() == true) {
            getLocationData()
        } else {
            uiDisplayError("No internet connection")
        }
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
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
            setMessage("$name permission is needed to provide you the best experience while using this app")
            setTitle("Permission required")
            setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    this@MapsActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
            setCancelable(false)
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
                uiDisplayError("$name permission required")
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
                    displayAddressFromLocation(location)
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

    private fun uiDisplayError(cause: String) {
        Toast.makeText(
            applicationContext,
            cause,
            Toast.LENGTH_SHORT
        ).show()
        binding.textAddressContent.text = cause
        binding.textLatContent.text = "n/a"
        binding.textLngContent.text = "n/a"
    }

    private fun hasInternetConnection(): Boolean? {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val initNetworkState = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return initNetworkState
    }

    private fun displayAddressFromLocation(location: Location) {
        val addresses = Geocoder(applicationContext, Locale.US).getFromLocation(
            location.latitude,
            location.longitude,
            3
        )
        binding.textAddressContent.text = addresses[0].getAddressLine(0)
        binding.textLatContent.text = location.latitude.toString()
        binding.textLngContent.text = location.longitude.toString()
    }
}


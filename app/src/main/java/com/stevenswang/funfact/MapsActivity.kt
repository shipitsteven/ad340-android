package com.stevenswang.funfact

import android.content.pm.PackageManager
import android.location.Location
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.stevenswang.funfact.databinding.ActivityMapsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val FINE_LOCATION_RQ = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkForPermissions(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            "location",
            FINE_LOCATION_RQ
        )

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

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getLocationData(mMap)
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // TODO: add markers here?
                Toast.makeText(
                    applicationContext,
                    "$name permission granted from check",
                    Toast.LENGTH_SHORT
                ).show()
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentLocation = location
                            mMap.isMyLocationEnabled = true
                        }
                    }.addOnFailureListener{
                        currentLocation = Location("")
                        currentLocation.latitude = 47.516783365445
                        currentLocation.longitude = -122.392755787503
                    }
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
                currentLocation = Location("")
                currentLocation.latitude = 47.516783365445
                currentLocation.longitude = -122.392755787503
            } else {
                Toast.makeText(
                    applicationContext,
                    "$name permission granted from grant",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        when (requestCode) {
            FINE_LOCATION_RQ -> innerCheck("location")
        }
    }

    private fun getLocationData(map: GoogleMap) {
        // TODO: can be refactored to extract the api request creation step
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiRequest::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val response = api.getCamData().awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                withContext(Dispatchers.Main) {
                    // TODO: refactor to extract map logic out to a separate function
                    mMap.clear()
                    for (index: Int in data.Features.indices) {
                        val locationArray = data.Features[index].PointCoordinate
                        val location = LatLng(locationArray[0], locationArray[1])
                        mMap.addMarker(
                            MarkerOptions().position(location)
                                .title(data.Features[index].Cameras[0].Description)
                        )
                    }

                    // TODO: moving camera center is a little funky
                    val seattle = LatLng(
                        47.516783365445, -122.392755787503
                    )
                    // FIXME: lateinit property currentLocation has not been initialized
                    // TODO: replace this janky fix
                    val myLocation = try {
                        LatLng(currentLocation.latitude, currentLocation.longitude)
                    } catch (e: Exception) {
                        seattle
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(12f))
                }
            }
        }
    }
}


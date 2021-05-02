package com.stevenswang.funfact

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class Traffic : AppCompatActivity() {

    private val tag = "Network state"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traffic)
        val recyclerTraffic = findViewById<RecyclerView>(R.id.recycler_traffic)
        recyclerTraffic.adapter = TrafficAdapter()
        recyclerTraffic.layoutManager = LinearLayoutManager(this)
        recyclerTraffic.setHasFixedSize(true)
        getNetworkState()
    }


    private fun getNetworkState() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val initNetworkState = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (initNetworkState == true) {
            // TODO: start fetching data
            Toast.makeText(applicationContext, "Connected to the internet", Toast.LENGTH_SHORT).show()
        } else {
            // TODO: SHOW some UI that indicate no internet connection
            Toast.makeText(applicationContext, "No internet connection", Toast.LENGTH_LONG).show()
        }

        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {

            override fun onLost(network : Network) {
                Log.e(tag,
                    "The application no longer has a default network. The last default network was $network"
                )
                Toast.makeText(applicationContext, "Network connection lost", Toast.LENGTH_LONG).show()
            }

            override fun onCapabilitiesChanged(network : Network, networkCapabilities : NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if(hasInternet) {
                    Toast.makeText(applicationContext, "Changed connection capabilities", Toast.LENGTH_SHORT).show()
                }
                Log.e(tag, "The default network changed capabilities: $networkCapabilities")
            }
        })
    }
}
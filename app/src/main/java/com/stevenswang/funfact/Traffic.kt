package com.stevenswang.funfact

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.awaitResponse
import java.io.EOFException
import java.io.IOException
import java.lang.Exception

const val BASE_URL = "https://web6.seattle.gov/Travelers/api/Map/"

class Traffic : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traffic)
        getNetworkState()
    }

    private fun getNetworkState() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val initNetworkState = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (initNetworkState == true) {
            val recyclerTraffic = findViewById<RecyclerView>(R.id.recycler_traffic)
            callAPI(recyclerTraffic)
        } else {
            updateUINoInternet()
        }
    }

    private fun callAPI(recyclerView: RecyclerView) {
        val api = ApiObject.getApiObject()!!

        CoroutineScope(Dispatchers.IO).launch {
            val progressBar = findViewById<ProgressBar>(R.id.progress_traffic)
            val loadingText = findViewById<TextView>(R.id.text_loading_traffic)
            try {
                val response = api.getCamData().awaitResponse()
                if (response.isSuccessful) {
                    val data = response.body()!!
                    withContext(Dispatchers.Main) {
                        loadingText.isVisible = false
                        recyclerView.adapter = TrafficAdapter(data)
                        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                        recyclerView.setHasFixedSize(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        loadingText.text = getString(R.string.fetchError)
                    }
                }
            } catch (e: EOFException) {
                withContext(Dispatchers.Main) { loadingText.text = getString(R.string.badURL) }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { loadingText.text = getString(R.string.badJSON) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingText.text = getString(R.string.unknownError)
                }
            }
            withContext(Dispatchers.Main) { progressBar.isVisible = false }
        }
    }

    private fun updateUINoInternet() {
        Toast.makeText(applicationContext, "No internet connection", Toast.LENGTH_LONG).show()
        val progressBar = findViewById<ProgressBar>(R.id.progress_traffic)
        val loadingText = findViewById<TextView>(R.id.text_loading_traffic)
        progressBar.isVisible = false
        loadingText.text = getString(R.string.NoInternetTraffic)
    }
}
package com.stevenswang.funfact

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stevenswang.funfact.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInfo.setOnClickListener {
            Toast.makeText(this, "Info button clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnMovies.setOnClickListener {
            val intent = Intent(this, Movies::class.java)
            startActivity(intent)
        }
    }

    fun btnClicked(view: View) {
        val buttonText: String = (view as Button).text as String
        val toastMessage: String = when (buttonText) {
            "Cities" -> "Seattle is the best city"
//            "Movies" -> "Avatar is the highest grossing film of all time"
            "Parks" -> "Discovery Park is Seattle's biggest park"
            "Traffic" -> "The new Link/Light rail hopes to solve Seattle's traffic problem"
            else -> "Something went wrong."
        }

        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }
}
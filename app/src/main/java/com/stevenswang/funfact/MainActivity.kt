package com.stevenswang.funfact

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.stevenswang.funfact.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textInputUsername: TextInputEditText
    private lateinit var textInputEmail: TextInputEditText
    private lateinit var textInputPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            if(validateEmail(textInputEmail.text.toString().trim())) {
                Toast.makeText(applicationContext, "Valid email, good job", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMovies.setOnClickListener {
            val intent = Intent(this, Movies::class.java)
            startActivity(intent)
        }

        binding.btnTraffic.setOnClickListener {
            val intent = Intent(this, Traffic::class.java)
            startActivity(intent)
        }

        binding.btnMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        textInputUsername = binding.textInputUsername
        textInputEmail = binding.textInputEmail
        textInputPassword = binding.textInputPassword
    }

    fun btnClicked(view: View) {
        val buttonText: String = (view as Button).text as String
        val toastMessage: String = when (buttonText) {
            "Cities" -> "Seattle is the best city"
//            "Movies" -> "Avatar is the highest grossing film of all time"
            "Parks" -> "Discovery Park is Seattle's biggest park"
//            "Traffic" -> "The new Link/Light rail hopes to solve Seattle's traffic problem"
            else -> "Something went wrong."
        }

        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            textInputEmail.error = "Email cannot be empty"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            textInputEmail.error = "Please enter a valid email address"
            return false
        } else {
            textInputEmail.error = null
            return true
        }
    }


}
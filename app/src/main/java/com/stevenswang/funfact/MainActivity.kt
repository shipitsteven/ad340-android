package com.stevenswang.funfact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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
            if (validateEmail(textInputEmail.text.toString().trim())) {
                signIn()
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

        val savedInfo = readFromSharedPreference()
        textInputEmail.setText(savedInfo[0])
        textInputPassword.setText(savedInfo[1])
        textInputUsername.setText(savedInfo[2])
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
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEmail.error = "Please enter a valid email address"
            return false
        } else {
            textInputEmail.error = null
            return true
        }
    }

    private fun signIn() {
        Log.d("FIREBASE", "signIn")

        // 1 - validate name, email, and password entries

        // 2 - save valid entries to shared preferences

        // 3 - sign into Firebase
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(
            textInputEmail.text.toString(),
            textInputPassword.text.toString()
        )
            .addOnCompleteListener(
                this
            ) { task ->
                Log.e("FIREBASE", "signIn:onComplete:" + task.isSuccessful)
                if (task.isSuccessful) {
                    writeToSharedPreference(
                        textInputEmail.text.toString(),
                        textInputPassword.text.toString(),
                        textInputUsername.text.toString()
                    )
                    // update profile
                    val user = FirebaseAuth.getInstance().currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(textInputUsername.text.toString())
                        .build()
                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.e("FIREBASE", "User profile updated.")
                                // Go to FirebaseActivity
                                startActivity(Intent(this, FirebaseActivity::class.java))
                            }
                        }
                } else {
                    Log.e("FIREBASE", "sign-in failed")
                    Toast.makeText(
                        this@MainActivity, "Sign In Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("task", task.exception.toString())
                }
            }
    }

    private fun writeToSharedPreference(email: String, password: String, username: String) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("email", email)
            putString("password", password)
            putString("username", username)
            apply()
        }
    }

    private fun readFromSharedPreference(): List<String> {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val savedInfo: MutableList<String> = mutableListOf()
        savedInfo.add(sharedPref.getString("email", "").toString())
        savedInfo.add(sharedPref.getString("password", "").toString())
        savedInfo.add(sharedPref.getString("username", "").toString())
        return savedInfo
    }


}
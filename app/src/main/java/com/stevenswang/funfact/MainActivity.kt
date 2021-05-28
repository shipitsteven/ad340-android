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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.UserProfileChangeRequest
import com.stevenswang.funfact.databinding.ActivityMainBinding
import java.lang.Exception


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
            if (isFormValid()) {
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
            textInputEmail.error = "Email cannot be blank"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEmail.error = "Please enter a valid email address"
            return false
        } else {
            textInputEmail.error = null
            return true
        }
    }

    private fun isFormValid(): Boolean {
        val validEmail = validateEmail(textInputEmail.text.toString().trim())
        val validUsername =
            validateBlankString(textInputUsername.text.toString().trim(), textInputUsername)
        val validPassword =
            validateBlankString(textInputPassword.text.toString().trim(), textInputPassword)

        return (validEmail && validUsername && validPassword)
    }

    private fun validateBlankString(input: String, textInput: TextInputEditText): Boolean {
        if (input.isBlank()) {
            textInput.error = "${textInput.hint} cannot be blank"
            return false
        } else {
            textInput.error = null
            return true
        }
    }

    private fun signIn() {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(
            textInputEmail.text.toString(),
            textInputPassword.text.toString()
        )
            .addOnCompleteListener(
                this
            ) { task ->
                Log.d("FIREBASE", "signIn:onComplete:" + task.isSuccessful)
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
                                Log.d("FIREBASE", "User profile updated.")
                                // Go to FirebaseActivity
                                startActivity(Intent(this, FirebaseActivity::class.java))
                            }
                        }
                } else {
                    Log.e("FIREBASE", "sign-in failed")
                    Log.e("task", task.exception.toString())
                    displaySignInError(task.exception)
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

    private fun displaySignInError(e: Exception?) {
        when (e) {
            is FirebaseNetworkException -> Toast.makeText(
                this,
                "Network error, please check your internet connection",
                Toast.LENGTH_SHORT
            ).show()
            is FirebaseAuthInvalidUserException -> Toast.makeText(
                this,
                "Unknown user, please contact admin to create an account",
                Toast.LENGTH_SHORT
            ).show()
            is FirebaseAuthInvalidCredentialsException -> Toast.makeText(
                this,
                "Incorrect password",
                Toast.LENGTH_SHORT
            ).show()
            else -> Toast.makeText(this, "Unknown error: ${e?.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
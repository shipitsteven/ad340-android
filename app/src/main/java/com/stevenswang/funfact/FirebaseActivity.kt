package com.stevenswang.funfact

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class FirebaseActivity : AppCompatActivity() {

    private lateinit var myRef: DatabaseReference
    lateinit var listAdapter: UserListAdapter
    var userData: ArrayList<User> = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase)

        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        val userList = findViewById<ListView>(R.id.userList)
        listAdapter = UserListAdapter(applicationContext, userData)
        userList.adapter = listAdapter

        val mDatabase = FirebaseDatabase.getInstance()
        myRef = mDatabase.getReference("users")

        // update database
        if (currentUser != null) {
            writeNewUser(
                currentUser.uid,
                currentUser.displayName.toString(),
                currentUser.email.toString()
            )
        } else {
            Log.e("user", "Current user is null")
        }


        // get list of users
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("onDataChange", "Someone signed in!")
                listAdapter.clear()
                for (userSnapshot in dataSnapshot.children) {
                    Log.d("UserSnapshot", userSnapshot.value.toString())
                    val key = userSnapshot.key
                    val userNameString = if (userSnapshot.child("username").value != null) {
                        userSnapshot.child("username").value.toString()
                    } else {
                        "N/A"
                    }
                    val user = User(
                        userNameString,
                        userSnapshot.child("email").value.toString(),
                        userSnapshot.child("updated").value.toString()
                    )
                    userData.add(user)
                }

                listAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.e("DatabaseError", "loadUsers:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    private fun writeNewUser(userId: String, name: String, email: String) {
        val user = User(name, email, Date().toString())
        myRef.child(userId).setValue(user)
    }

    inner class UserListAdapter(context: Context, private val values: List<User>) :
        ArrayAdapter<User>(context, R.layout.user_item, values) {

        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView = inflater
                .inflate(R.layout.user_item, parent, false)
            val title = rowView.findViewById<TextView>(R.id.item_title)
            title.text = values[position].username
            val subtitle = rowView.findViewById<TextView>(R.id.item_subtitle)
            val updatedText = "Updated: " + values[position].updated
            subtitle.text = updatedText
            return rowView
        }
    }

    inner class User(val username: String, val email: String, val updated: String)

}
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
        Log.e("Context", "Made it to firebase activity")

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
            writeNewUser(currentUser.uid, currentUser.displayName.toString(), currentUser.email.toString())
        }

        // get list of users

        // get list of users
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    // TODO: handle the post
                    Log.e("UserSnapshot", userSnapshot.value.toString())
                    val key = userSnapshot.key
                    val user = User(
                        userSnapshot.child("username").value.toString(),
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
        ArrayAdapter<User>(context, R.layout.user_item) {

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView = inflater
                .inflate(R.layout.user_item, parent, false)
            val title: TextView = rowView.findViewById(R.id.item_title)
            title.text = values[position].username
            val subtitle: TextView = rowView.findViewById(R.id.item_subtitle)
            val updatedText = "Updated: " + values[position].updatedDate
            subtitle.text = updatedText
            return rowView
        }
    }

    inner class User(val username: String, val email: String, val updatedDate: String)

}
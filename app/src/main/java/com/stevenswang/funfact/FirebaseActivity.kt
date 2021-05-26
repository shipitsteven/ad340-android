package com.stevenswang.funfact

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;


class FirebaseActivity : AppCompatActivity() {
    private var myRef: DatabaseReference? = null
    var userList: ListView? = null
    var listAdapter: UserListAdapter? = null
    var userData: ArrayList<User> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        userList = findViewById(R.id.userList)
        listAdapter = UserListAdapter(this, userData)
        userList.setAdapter(listAdapter)
        val mDatabase = FirebaseDatabase.getInstance()
        myRef = mDatabase.getReference("users")

        // update database
        writeNewUser(currentUser!!.uid, currentUser.displayName, currentUser.email)

        // get list of users
        myRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    // TODO: handle the post
                    Log.d(TAG, userSnapshot.value.toString())
                    val key = userSnapshot.key
                    val user = User(
                        userSnapshot.child("username").value.toString(),
                        userSnapshot.child("email").value.toString(),
                        userSnapshot.child("updated").value.toString()
                    )
                    userData.add(user)
                }
                listAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUsers:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    private fun writeNewUser(userId: String, name: String?, email: String?) {
        val user = User(name, email, Date().toString())
        myRef!!.child(userId).setValue(user)
    }

    @IgnoreExtraProperties
    inner class User {
        var username: String? = null
        var email: String? = null
        var updated: String? = null

        constructor() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        constructor(username: String?, email: String?, updated: String) {
            this.username = username
            this.email = email
            this.updated = updated
        }
    }

    inner class UserListAdapter (
        context: Context,
        values: ArrayList<User>
    ) :
        ArrayAdapter<User?>(context, 0, values) {
        private val context: Context
        private val values: ArrayList<User>
        fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rowView: View = inflater.inflate(R.layout.list_item, parent, false)
            val title: TextView = rowView.findViewById(R.id.item_title)
            title.text = values[position].username
            val subtitle: TextView = rowView.findViewById(R.id.item_subtitle)
            subtitle.text = "Updated: " + values[position].updated
            return rowView
        }

        init {
            this.context = context
            this.values = values
        }
    }

    companion object {
        private val TAG = FirebaseActivity::class.java.simpleName
    }
}
package com.dipalmaintiso.carcheck.usermanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.USER_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserActivity : AppCompatActivity() {

    private var groupId: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        groupId = intent.getStringExtra(GROUP_ID)
        userId = intent.getStringExtra(USER_ID)

        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groupName = dataSnapshot.child("groupName").getValue(String::class.java)!!

                val userRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId")
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val username = dataSnapshot.child("username").getValue(String::class.java)!!
                        supportActionBar?.title = "$groupName - $username"
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        supportActionBar?.title = "$groupName - Username"
                        Toast.makeText(applicationContext, "Something went wrong. $databaseError", Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                supportActionBar?.title = "User"
                Toast.makeText(applicationContext, "Something went wrong. $databaseError", Toast.LENGTH_LONG).show()
            }
        })
    }
}
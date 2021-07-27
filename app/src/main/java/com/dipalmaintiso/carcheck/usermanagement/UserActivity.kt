package com.dipalmaintiso.carcheck.usermanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.FAILURE
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.USER_ID
import com.dipalmaintiso.carcheck.views.GroupUsersActivity
import com.dipalmaintiso.carcheck.views.UserGroupsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_user.*

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
                val admin = dataSnapshot.child("users/$userId/administrator").getValue(Boolean::class.java)!!

                adminSwitchUserActivity.isChecked = admin

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

        removeUserButtonUserActivity.setOnClickListener {
            showDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_user, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.save -> {
                saveUserRoleChange(adminSwitchUserActivity.isChecked)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun saveUserRoleChange(admin: Boolean) {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId/administrator")

        ref.setValue(admin)
            .addOnSuccessListener {
                val intent = Intent(applicationContext, GroupUsersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(FAILURE, "")
                intent.putExtra(GROUP_ID, groupId)

                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong. ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDialog() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Are you sure you want to remove this user?")

        builder.setPositiveButton("Remove") { dialog, which ->
            removeUserFromGroupAndRedirect()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun removeUserFromGroupAndRedirect() {
        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")

        val loggedUserId = FirebaseAuth.getInstance().uid

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (userId == loggedUserId) {
                    removeUserFromGroup()

                    val intent = Intent(applicationContext, UserGroupsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(FAILURE, "")

                    startActivity(intent)
                }
                else {
                    removeUserFromGroup()

                    val intent = Intent(applicationContext, GroupUsersActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(FAILURE, "")
                    intent.putExtra(GROUP_ID, groupId)

                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun removeUserFromGroup() {
        FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId").removeValue()
        FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId/groups/$groupId").removeValue()
    }
}
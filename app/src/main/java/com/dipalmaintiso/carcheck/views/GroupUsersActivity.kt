package com.dipalmaintiso.carcheck.views

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.GroupUser
import com.dipalmaintiso.carcheck.rows.GroupUsersRow
import com.dipalmaintiso.carcheck.usermanagement.UserActivity
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.FAILURE
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.USER_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_group_users.*
import java.util.*

class GroupUsersActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    val usersMap = HashMap<String, GroupUser>()
    var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_users)
        groupUsersRecyclerView.adapter = adapter
        groupUsersRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        groupId = intent.getStringExtra(GROUP_ID)
        val failureMessage = intent.getStringExtra(FAILURE)

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                supportActionBar?.title = dataSnapshot.child("groupName").getValue(String::class.java)!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
                supportActionBar?.title = "Users"
                Toast.makeText(applicationContext, "Something went wrong. $databaseError", Toast.LENGTH_LONG).show()
            }
        })

        if (failureMessage != null && failureMessage != "") {
            Toast.makeText(this, "Something went wrong. $failureMessage", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        displayUsers()
        verifyUserAdministrator()
    }

    private fun verifyUserAdministrator() {
        val userId = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        ref.child("users/$userId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!

                if (admin) {
                    adapter.setOnItemClickListener { item, _ ->

                        ref.child("creatorId").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val creatorId = dataSnapshot.getValue(String::class.java)!!
                                val groupUsersRow = item as GroupUsersRow

                                if (creatorId == groupUsersRow.uid) {
                                    Toast.makeText(applicationContext, "Access denied. The creator of the group cannot be removed and their administrator role cannot be revoked.", Toast.LENGTH_LONG).show()
                                }
                                else if (userId == groupUsersRow.uid) {
                                    Toast.makeText(applicationContext, "Access denied. You cannot change your own role. If you wish to leave the group, do so in the previous page.", Toast.LENGTH_LONG).show()
                                }
                                else {
                                    val intent = Intent(applicationContext, UserActivity::class.java)
                                    intent.putExtra(GROUP_ID, groupId)
                                    intent.putExtra(USER_ID, groupUsersRow.uid)

                                    startActivity(intent)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun displayUsers() {
        adapter.clear()
        usersMap.clear()
        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users")

        groupRef.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val groupUser = p0.getValue(GroupUser::class.java) ?: return
                usersMap[p0.key!!] = groupUser
                refreshRecyclerView()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val groupUser = p0.getValue(GroupUser::class.java) ?: return
                usersMap[p0.key!!] = groupUser
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun refreshRecyclerView() {
        adapter.clear()
        usersMap.values.forEach {
            adapter.add(GroupUsersRow(it, groupId))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_group_users, menu)

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!

                if (admin) {
                    val icon = menu?.findItem(R.id.add_user)
                    icon?.isEnabled = true
                    icon?.isVisible = true
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.add_user -> {
                showDialog()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Email of the user to add")

        val input = EditText(this)
        input.hint = " Email"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val email = input.text.toString()

            if (email.isBlank())
                Toast.makeText(this, "Please enter the email of the user to add.", Toast.LENGTH_LONG).show()
            else {
                fetchUserByEmailAndAdd(email)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun fetchUserByEmailAndAdd(email: String) {
        val emailEncoded = email.replace(".", ",")

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/emailToUid/$emailEncoded/uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val uid = dataSnapshot.getValue(String::class.java)!!

                    addUserToGroup(uid)
                }
                else {
                    Toast.makeText(applicationContext, "Something went wrong. Email does not match any user.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun addUserToGroup(userId: String) {
        val userRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId")
        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL)
            .getReference("/groups/$groupId/users/$userId")

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    prepareForIntent("User already in group.")
                } else {
                    val groupUser = GroupUser(userId, false)

                    groupRef.setValue(groupUser)
                        .addOnSuccessListener {
                            userRef.child("/groups/$groupId/gid").setValue(groupId)
                                .addOnSuccessListener {
                                    prepareForIntent("")
                                }
                                .addOnFailureListener {
                                    groupRef.removeValue()
                                    prepareForIntent(it.message.toString())
                                }
                        }
                        .addOnFailureListener {
                            prepareForIntent(it.message.toString())
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun prepareForIntent(message: String) {
        val intent = Intent(this, GroupUsersActivity::class.java)
        intent.putExtra(FAILURE, message)
        intent.putExtra(GROUP_ID, groupId)

        startActivity(intent)
        finish()
    }
}
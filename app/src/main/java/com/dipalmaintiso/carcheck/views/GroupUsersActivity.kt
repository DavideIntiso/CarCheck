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
import com.dipalmaintiso.carcheck.registrationlogin.RegistrationActivity
import com.dipalmaintiso.carcheck.rows.GroupUsersRow
import com.dipalmaintiso.carcheck.utilities.*
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

        verifyUserLoggedIn()

        groupId = intent.getStringExtra(GROUP_ID)
        val failureMessage = intent.getStringExtra(FAILURE)

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                supportActionBar?.title = dataSnapshot.child("groupName").getValue(String::class.java)!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
                supportActionBar?.title = "Users"
                Toast.makeText(applicationContext, "Something went wrong: $databaseError", Toast.LENGTH_LONG).show()
            }
        })

        displayUsers()

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, GroupUsersActivity::class.java)
            intent.putExtra(GROUP_ID, groupId)
            intent.putExtra(VEHICLE_ID, "USERID")
            startActivity(intent)
        }

        if (failureMessage != null && failureMessage != "") {
            Toast.makeText(this, "Something went wrong. $failureMessage", Toast.LENGTH_LONG).show()
        }

    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun displayUsers(){
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
            adapter.add(GroupUsersRow(it))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_group_users, menu)
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

    private fun showDialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Email of the user to add")

        val input = EditText(this)
        input.hint = " Email"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, which ->
            val email = input.text.toString()
            fetchUserByEmailAndAdd(email)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun fetchUserByEmailAndAdd(email: String) {
        val emailEncoded = email.replace(".", ",")

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/emailToUid/$emailEncoded/uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val uid = dataSnapshot.getValue(String::class.java)!!

                    val intent = Intent(applicationContext, GroupUsersActivity::class.java)
                    intent.putExtra(GROUP_ID, groupId)

                    addUserToGroup(groupId, uid, false, applicationContext, intent, null)
                }
                else {
                    Toast.makeText(applicationContext, "Something went wrong. Email does not match any user", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}
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
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.utilities.addUserToGroup
import com.dipalmaintiso.carcheck.models.Group
import com.dipalmaintiso.carcheck.registrationlogin.RegistrationActivity
import com.dipalmaintiso.carcheck.rows.UserGroupsRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_user_groups.*
import java.util.*

class UserGroupsActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    val groupsMap = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_groups)
        userGroupsRecyclerView.adapter = adapter
        userGroupsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        verifyUserLoggedIn()
        displayGroups()

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, GroupVehiclesActivity::class.java)
            val userGroupRow = item as UserGroupsRow
            intent.putExtra(GROUP_ID, userGroupRow.gid)
            startActivity(intent)
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

    private fun displayGroups(){
        val userId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$userId/groups")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                groupsMap.add(p0.key!!)
                refreshRecyclerView()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                groupsMap.add(p0.key!!)
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun refreshRecyclerView(){
        groupsMap.forEach() {
            val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$it")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val groupName = dataSnapshot.child("groupName").getValue(String::class.java)!!
                    adapter.add(UserGroupsRow(groupName, it))
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
        groupsMap.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_user_groups, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.create_group -> {
                showDialog()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose a group name")

        val input = EditText(this)
        input.hint = " Group name"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, which ->
            val groupName = input.text.toString()
            saveGroupToFirebaseDatabase(groupName)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun saveGroupToFirebaseDatabase(groupName: String){
        val creatorId = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups")

        val gid = ref.push().key

        if (null == gid) {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show()
        }

        else {
            val group = Group(gid, groupName, creatorId)

            ref.child(gid).setValue(group)
                .addOnSuccessListener {
                    val result = addUserToGroup(gid, creatorId, true)
                    if (result == "") {
                        val intent = Intent(this, UserGroupsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    else {
                        ref.removeValue()
                        Toast.makeText(this, "Something went wrong: $result", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
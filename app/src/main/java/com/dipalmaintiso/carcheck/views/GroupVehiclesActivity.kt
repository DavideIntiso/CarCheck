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
import com.dipalmaintiso.carcheck.management.DATABASE_URL
import com.dipalmaintiso.carcheck.management.GROUP_ID
import com.dipalmaintiso.carcheck.management.addUserToGroup
import com.dipalmaintiso.carcheck.models.Group
import com.dipalmaintiso.carcheck.models.Vehicle
import com.dipalmaintiso.carcheck.registrationlogin.RegistrationActivity
import com.dipalmaintiso.carcheck.rows.GroupVehiclesRow
import com.dipalmaintiso.carcheck.rows.UserGroupsRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_user_groups.*
import java.util.*

class GroupVehiclesActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    val vehiclesMap = HashMap<String, Vehicle>()
    var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_vehicles)
        userGroupsRecyclerView.adapter = adapter
        userGroupsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        verifyUserLoggedIn()

        groupId = intent.getStringExtra(GROUP_ID)

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                supportActionBar?.title = dataSnapshot.child("groupName").getValue(String::class.java)!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
                supportActionBar?.title = "Vehicles"
                Toast.makeText(applicationContext, "Something went wrong: $databaseError", Toast.LENGTH_LONG).show()
            }
        })

        displayVehicles()

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

    private fun displayVehicles(){
        val groupRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles")
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles")

        groupRef.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val vehicle = p0.getValue(Vehicle::class.java) ?: return
                vehiclesMap[p0.key!!] = vehicle
                refreshRecyclerView()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val vehicle = p0.getValue(Vehicle::class.java) ?: return
                vehiclesMap[p0.key!!] = vehicle
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        vehiclesMap.values.forEach{
            adapter.add(GroupVehiclesRow(it))
        }
    }
}
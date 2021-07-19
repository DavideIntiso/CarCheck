package com.dipalmaintiso.carcheck.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.management.VehicleDataActivity
import com.dipalmaintiso.carcheck.utilities.DATABASE_URL
import com.dipalmaintiso.carcheck.utilities.GROUP_ID
import com.dipalmaintiso.carcheck.models.Vehicle
import com.dipalmaintiso.carcheck.registrationlogin.RegistrationActivity
import com.dipalmaintiso.carcheck.rows.GroupVehiclesRow
import com.dipalmaintiso.carcheck.rows.UserGroupsRow
import com.dipalmaintiso.carcheck.utilities.VEHICLE_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_group_vehicles.*
import java.util.*

class GroupVehiclesActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    val vehiclesMap = ArrayList<String>()
    var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_vehicles)
        groupVehiclesRecyclerView.adapter = adapter
        groupVehiclesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

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
            intent.putExtra(VEHICLE_ID, "")
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

        groupRef.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                vehiclesMap.add(p0.key!!)
                refreshRecyclerView()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                vehiclesMap.add(p0.key!!)
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun refreshRecyclerView(){
        vehiclesMap.forEach() {
            val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles/$it")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val vehicle = dataSnapshot.getValue(Vehicle::class.java)!!
                    adapter.add(GroupVehiclesRow(vehicle))
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
        vehiclesMap.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_group_vehicles, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.add_vehicle -> {
                val intent = Intent(this, VehicleDataActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(GROUP_ID, groupId)
                intent.putExtra(VEHICLE_ID, "new")
                startActivity(intent)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }
}
package com.dipalmaintiso.carcheck.views

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.GroupUser
import com.dipalmaintiso.carcheck.vehiclemanagement.VehicleDataActivity
import com.dipalmaintiso.carcheck.vehiclemanagement.admin
import com.dipalmaintiso.carcheck.models.Vehicle
import com.dipalmaintiso.carcheck.registrationlogin.RegistrationActivity
import com.dipalmaintiso.carcheck.rows.GroupVehiclesRow
import com.dipalmaintiso.carcheck.utilities.*
import com.google.firebase.FirebaseError
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
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_vehicles)
        groupVehiclesRecyclerView.adapter = adapter
        groupVehiclesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        verifyUserLoggedIn()

        groupId = intent.getStringExtra(GROUP_ID)
        val failureMessage = intent.getStringExtra(FAILURE)

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                supportActionBar?.title = dataSnapshot.child("groupName").getValue(String::class.java)!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
                supportActionBar?.title = "Vehicles"
                Toast.makeText(applicationContext, "Something went wrong. $databaseError", Toast.LENGTH_LONG).show()
            }
        })

        displayVehicles()

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, VehicleDataActivity::class.java)
            intent.putExtra(GROUP_ID, groupId)

            val groupVehiclesRow = item as GroupVehiclesRow
            intent.putExtra(VEHICLE_ID, groupVehiclesRow.vid)

            startActivity(intent)
        }

        if (failureMessage != null && failureMessage != "") {
            Toast.makeText(this, "Something went wrong. $failureMessage", Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyUserLoggedIn() {
        userId = FirebaseAuth.getInstance().uid
        if (userId == null) {
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

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                admin = dataSnapshot.child("administrator").getValue(Boolean::class.java)!!

                if (admin) {
                    val icon = menu?.findItem(R.id.add_vehicle)
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
            R.id.add_vehicle -> {
                val intent = Intent(this, VehicleDataActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(GROUP_ID, groupId)
                intent.putExtra(VEHICLE_ID, "new")
                startActivity(intent)
                true
            }
            R.id.users -> {
                val intent = Intent(this, GroupUsersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(GROUP_ID, groupId)
                startActivity(intent)
                true
            }
            R.id.leave_group -> {
                showDialog()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Are you sure you want to leave the group?")

        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/creatorId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val creatorId = dataSnapshot.getValue(String::class.java)

                if (creatorId == userId) {
                    builder.setMessage("This will delete the group and all of its data!")

                    builder.setPositiveButton("Delete") { dialog, which ->
                        deleteGroupAndRedirect()
                    }
                }
                else {
                    builder.setPositiveButton("Leave") { dialog, which ->
                        removeUserFromGroup(groupId, userId)

                        val intent = Intent(applicationContext, UserGroupsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(FAILURE, "")
                        startActivity(intent)
                    }
                }

                builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

                builder.show()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun deleteGroupAndRedirect() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/users")

        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    var groupUser = postSnapshot.getValue(GroupUser::class.java)

                    var uid = groupUser?.uid
                    FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$uid/groups/$groupId").removeValue()
                }
                deleteGroupVehicles()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun deleteGroupVehicles() {
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId/vehicles")

        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    var vid = postSnapshot.child("vid").getValue(String::class.java)

                    FirebaseDatabase.getInstance(DATABASE_URL).getReference("/vehicles/$vid").removeValue()
                }
                FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$groupId").removeValue()

                val intent = Intent(applicationContext, UserGroupsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(FAILURE, "")
                startActivity(intent)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
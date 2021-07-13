package com.dipalmaintiso.carcheck.views

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.marginLeft
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.Group
import com.dipalmaintiso.carcheck.models.User
import com.dipalmaintiso.carcheck.management.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*

class UserGroupsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_groups)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_user_groups, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_settings -> {
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
            var groupName = input.text.toString()
            saveGroupToFirebaseDatabase(groupName)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun saveGroupToFirebaseDatabase(groupName: String){
        val creatorId = FirebaseAuth.getInstance().uid ?: ""
        val gid = UUID.randomUUID().toString()
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/groups/$gid")
        val group = Group(gid, groupName, creatorId)

        ref.setValue(group)
            .addOnSuccessListener {
                val result = addUserToGroup(gid, creatorId, true)
                if (result == "") {
                    val intent = Intent(this, UserGroupsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this, "Something went wrong: $result", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
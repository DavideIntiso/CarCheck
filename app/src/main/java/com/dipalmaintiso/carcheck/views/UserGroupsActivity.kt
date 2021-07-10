package com.dipalmaintiso.carcheck.views

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.marginLeft
import com.dipalmaintiso.carcheck.R

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
            var group_name = input.text.toString()

            // create group with group name and creator's uid
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

}
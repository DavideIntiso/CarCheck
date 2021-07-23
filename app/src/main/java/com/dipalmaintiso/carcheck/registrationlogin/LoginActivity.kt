package com.dipalmaintiso.carcheck.registrationlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.views.UserGroupsActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButtonLogin.setOnClickListener {
            performLogin()
        }

        notRegisteredYetTextViewLogin.setOnClickListener {
            finish()
        }
    }

    private fun performLogin(){
        var email = emailEditTextLogin.text.toString()
        var password = passwordEditTextLogin.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter your email and password.", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                val intent = Intent(this, UserGroupsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed. ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
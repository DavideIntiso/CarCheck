package com.dipalmaintiso.carcheck.registrationlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dipalmaintiso.carcheck.R
import com.dipalmaintiso.carcheck.models.User
import com.dipalmaintiso.carcheck.views.UserGroupsActivity
import com.dipalmaintiso.carcheck.management.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null
    val DEFAULT_PROFILE_PICTURE = "https://firebasestorage.googleapis.com/v0/b/carcheck-af4b2.appspot.com/o/images%2FDefaultProfilePicture.jpg?alt=media&token=6988e804-f53f-4051-b67e-bfb128d1a932"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        registerButtonRegistration.setOnClickListener {
            performRegistration()
        }

        alreadyRegisteredTextViewRegistration.setOnClickListener {
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        choosePhotoButtonRegistration.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            choosePhotoImageViewRegistration.setImageBitmap(bitmap)
            choosePhotoButtonRegistration.alpha = 0f
        }
    }

    private fun performRegistration(){
        var email = emailEditTextRegistration.text.toString()
        var password = passwordEditTextRegistration.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) {
                    return@addOnCompleteListener
                }
                uploadImageToFirebase()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImageToFirebase() {

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")

        if (selectedPhotoUri == null) {
            saveUserToFirebaseDatabase(DEFAULT_PROFILE_PICTURE)
            return
        }
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("/users/$uid")
        val user = User(uid, usernameEditTextRegistration.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, UserGroupsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
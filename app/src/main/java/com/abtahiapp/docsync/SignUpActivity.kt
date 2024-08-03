package com.abtahiapp.docsync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.content.Context
import android.content.SharedPreferences

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val database = FirebaseDatabase.getInstance().reference
                            user?.let {
                                val userId = it.uid
                                val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                                val userMap = mapOf("username" to username)
                                userRef.setValue(userMap)
                                database.child("users").child(it.uid).setValue(username)

                                val emailRef = FirebaseDatabase.getInstance().getReference("emails").child(userId)
                                val emailMap = mapOf("emailID" to email)
                                emailRef.setValue(emailMap)
                                database.child("emails").child(it.uid).setValue(email)
                                saveUserDetails(userId, username, email)

                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveUserDetails(userId: String, username: String, email: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("user_id", userId)
            putString("username", username)
            putString("email", email)
            apply()
        }
    }
}
package com.abtahiapp.docsync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateInput(email, password)) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
//                            val intent = Intent(this, MainActivity::class.java)
//                            startActivity(intent)
//                            finish()
                            val user = auth.currentUser
                            user?.let {
                                val userId = it.uid
                                // Fetch and save user details in SharedPreferences
                                fetchUserDetails(userId)
                            }
                        } else {
                            Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            //Toast.makeText(this, "Wrong Password / Email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun fetchUserDetails(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val username = snapshot.getValue(String::class.java) ?: "Unknown"
            saveUserDetails(userId, username)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserDetails(userId: String, username: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("user_id", userId)
            putString("username", username)
            apply()
        }
    }
}

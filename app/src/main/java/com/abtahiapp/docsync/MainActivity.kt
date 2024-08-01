package com.abtahiapp.docsync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var documentRecyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var documentAdapter: DocumentAdapter
    private lateinit var documents: MutableList<Document>
    private lateinit var username: String
    private lateinit var logoutButton: ImageButton
    private lateinit var userInfoTextView: TextView
    private lateinit var searchButton: Button

    private val ADD_DOCUMENT_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("documents")
        documentRecyclerView = findViewById(R.id.documentRecyclerView)
        addButton = findViewById(R.id.addButton)
        logoutButton = findViewById(R.id.logoutButton)
        userInfoTextView = findViewById(R.id.userInfoTextView)
        searchButton = findViewById(R.id.searchButton)

        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        fetchUsernameFromDatabase { fetchedUsername ->
            username = fetchedUsername
            //val userEmail = auth.currentUser?.email ?: "Unknown"
            //userInfoTextView.text = "Username: $username\nEmail: $userEmail"
            userInfoTextView.text = username

            documents = mutableListOf()
            documentAdapter = DocumentAdapter(documents) { document ->
                val intent = Intent(this, DocumentEditorActivity::class.java)
                val gson = Gson()
                val documentJson = gson.toJson(document)
                intent.putExtra("document", documentJson)
                startActivity(intent)
            }

            documentRecyclerView.layoutManager = LinearLayoutManager(this)
            documentRecyclerView.adapter = documentAdapter

            addButton.setOnClickListener {
                val intent = Intent(this, AddDocSyncActivity::class.java)
                intent.putExtra("username", username)
                startActivityForResult(intent, ADD_DOCUMENT_REQUEST_CODE)
            }

            fetchDocuments()
        }
    }

    private fun fetchUsernameFromDatabase(onUsernameFetched: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val username = snapshot.getValue(String::class.java) ?: "Unknown"
            onUsernameFetched(username)
        }.addOnFailureListener {
            onUsernameFetched("Unknown")
        }
    }

    private fun fetchDocuments() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                documents.clear()
                for (documentSnapshot in snapshot.children) {
                    val document = documentSnapshot.getValue(Document::class.java)
                    document?.let { documents.add(0, it) }
                }
                documentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

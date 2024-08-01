package com.abtahiapp.docsync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.BufferedReader
import java.io.InputStreamReader

class AddDocSyncActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var username: String
    private val REQUEST_CODE_PICK_FILE = 1
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var addButton: Button
    private lateinit var uploadButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doc_sync)

        database = FirebaseDatabase.getInstance().reference.child("documents")
        username = intent.getStringExtra("username") ?: "Unknown"

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        addButton = findViewById(R.id.addButton)
        uploadButton = findViewById(R.id.uploadButton)

        addButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val documentId = database.push().key ?: ""
                val document = Document(
                    id = documentId,
                    title = title,
                    content = content,
                    creatorUsername = username,
                    creationTime = System.currentTimeMillis(),
                    lastEditorUsername = "",
                    lastEditTime = 0L,
                    originalTitle = title,
                    originalContent = content
                )
                database.child(documentId).setValue(document)

                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "text/x-c", "text/x-c++src", "application/json", "text/x-python", "application/javascript", "text/x-java-source", "text/html", "text/css", "text/x-kotlin", "application/pdf"))
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                readFileContent(uri)
            }
        }
    }

    private fun readFileContent(uri: Uri) {
        try {
            val contentResolver = contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = reader.readText()
                    contentEditText.setText(content)
                }
            }
        } catch (e: Exception) {
            Log.e("File Read Error", "Error reading file content: ${e.message}")
        }
    }

}

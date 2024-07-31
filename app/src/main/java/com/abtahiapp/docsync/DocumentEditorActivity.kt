package com.abtahiapp.docsync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.BuildConfig
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentEditorActivity : AppCompatActivity() {

    private lateinit var document: Document
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var creatorTextView: TextView
    private lateinit var creationTimeTextView: TextView
    private lateinit var lastEditorTextView: TextView
    private lateinit var lastEditTimeTextView: TextView
    private lateinit var commitButton: Button
    private lateinit var pastEditorButton: Button
    private lateinit var uploadButton: ImageView
    private lateinit var originalButton: Button
    private lateinit var saveButton: ImageView
    private lateinit var shareButton: ImageView

    private val REQUEST_CODE_PICK_FILE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_editor)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("documents")

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        creatorTextView = findViewById(R.id.creatorTextView)
        creationTimeTextView = findViewById(R.id.creationTimeTextView)
        lastEditorTextView = findViewById(R.id.lastEditorTextView)
        lastEditTimeTextView = findViewById(R.id.lastEditTimeTextView)
        uploadButton = findViewById(R.id.uploadButton)
        commitButton = findViewById(R.id.commitButton)
        pastEditorButton = findViewById(R.id.pastEditorButton)
        originalButton = findViewById(R.id.originalButton)
        saveButton = findViewById(R.id.saveButton)
        shareButton = findViewById(R.id.shareButton)

        saveButton.setOnClickListener {
            saveFileToLocal()
        }

        shareButton.setOnClickListener {
            shareDocument()
        }

        val documentJson = intent.getStringExtra("document")
        if (documentJson != null) {
            val gson = Gson()
            document = gson.fromJson(documentJson, Document::class.java)
        }

        titleEditText.setText(document.title)
        contentEditText.setText(document.content)
        creatorTextView.text = "Created by: ${document.creatorUsername}"
        creationTimeTextView.text = "${formatTimestamp(document.creationTime)}"

        if (document.lastEditorUsername.isEmpty()) {
            lastEditorTextView.text = ""
        } else {
            lastEditorTextView.text = "Last Edited By: ${document.lastEditorUsername}"
        }

        if (document.lastEditTime == 0L) {
            lastEditTimeTextView.text = ""
        } else {
            lastEditTimeTextView.text = "${formatTimestamp(document.lastEditTime)}"
        }

        commitButton.setOnClickListener { updateDocument() }

        pastEditorButton.setOnClickListener {
            showPastEditorsActivity()
        }
        originalButton.setOnClickListener {
            titleEditText.setText(document.originalTitle)
            contentEditText.setText(document.originalContent)
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

    private fun updateDocument() {
        val updatedTitle = titleEditText.text.toString()
        val updatedContent = contentEditText.text.toString()

        fetchUsernameFromDatabase { fetchedUsername ->
            document.lastEditorUsername = fetchedUsername
            document.lastEditTime = System.currentTimeMillis()

            val documentRef = database.child(document.id)
            documentRef.get().addOnSuccessListener { snapshot ->
                val originalTitle = snapshot.child("originalTitle").getValue(String::class.java) ?: ""
                val originalContent = snapshot.child("originalContent").getValue(String::class.java) ?: ""

                val highlightedTitle = getHighlightedText(originalTitle, updatedTitle, isTitle = true)
                val highlightedContent = getHighlightedText(originalContent, updatedContent, isTitle = false)

                document.title = updatedTitle
                document.content = updatedContent
                document.lastEditorUsername = fetchedUsername
                document.lastEditTime = System.currentTimeMillis()

                val updatedPastEditors = document.pastEditors.toMutableList()
                updatedPastEditors.add(PastEditor(
                    username = fetchedUsername,
                    editTime = document.lastEditTime,
                    updatedTitle = highlightedTitle.toString(),
                    updatedContent = highlightedContent.toString()
                ))
                document.pastEditors = updatedPastEditors

                documentRef.setValue(document).addOnCompleteListener {
                    finish()
                }
            }.addOnFailureListener {
                Log.e("Update Document", "Error fetching original document: ${it.message}")
            }
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

    private fun showPastEditorsActivity() {
        val intent = Intent(this, PastEditorActivity::class.java)
        intent.putExtra("originalTitle", document.originalTitle)
        intent.putExtra("originalContent", document.originalContent)
        val gson = Gson()
        intent.putExtra("pastEditors", gson.toJson(document.pastEditors))
        startActivity(intent)
    }

    private fun saveFileToLocal() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title or content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "$title.txt"
        val fileContent = content
        val fileDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(fileDir, fileName)

        try {
            FileOutputStream(file).use {
                it.write(fileContent.toByteArray())
            }
            val filePath = file.absolutePath
            Toast.makeText(this, "File saved successfully at $filePath", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareDocument() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title or content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "$title.txt"
        val fileContent = content
        val fileDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(fileDir, fileName)

        try {
            FileOutputStream(file).use {
                it.write(fileContent.toByteArray())
            }

            val fileUri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "text/plain"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share File"))

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        return if (timestamp != 0L) {
            val sdf = SimpleDateFormat("hh:mm a dd-MM-yy", Locale.getDefault())
            val date = Date(timestamp)
            sdf.format(date)
        } else {
            ""
        }
    }

    private fun getHighlightedText(originalText: String, updatedText: String, isTitle: Boolean): SpannableString {
        val spannableString = SpannableString(updatedText)
        val color = if (isTitle) {
            ContextCompat.getColor(this, R.color.highlight_blue)
        } else {
            ContextCompat.getColor(this, R.color.highlight_red)
        }

        if (originalText != updatedText) {
            spannableString.setSpan(
                ForegroundColorSpan(color),
                0,
                updatedText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannableString
    }
}

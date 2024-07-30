package com.abtahiapp.docsync

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class PastEditorActivity : AppCompatActivity() {

    private lateinit var originalTitle: String
    private lateinit var originalContent: String
    private lateinit var pastEditors: List<PastEditor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_editor)

        val intent = intent
        originalTitle = intent.getStringExtra("originalTitle") ?: ""
        originalContent = intent.getStringExtra("originalContent") ?: ""
        val pastEditorsJson = intent.getStringExtra("pastEditors")

        val gson = Gson()
        pastEditors = gson.fromJson(pastEditorsJson, Array<PastEditor>::class.java).toList()

        val originalTitleTextView = findViewById<TextView>(R.id.originalTitle)
        val originalContentTextView = findViewById<TextView>(R.id.originalContent)
        val pastEditorsRecyclerView = findViewById<RecyclerView>(R.id.pastEditorsRecyclerView)

        originalTitleTextView.text = originalTitle
        originalContentTextView.text = originalContent
        pastEditorsRecyclerView.layoutManager = LinearLayoutManager(this)

        val pastEditorAdapter = PastEditorAdapter(this, pastEditors, originalTitle, originalContent)
        pastEditorsRecyclerView.adapter = pastEditorAdapter

    }
}
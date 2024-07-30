package com.abtahiapp.docsync

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var voiceSearchButton: ImageView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: DocumentAdapter
    private lateinit var documents: MutableList<Document>
    private lateinit var database: DatabaseReference
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchView = findViewById(R.id.searchView)
        voiceSearchButton = findViewById(R.id.voiceSearchButton)
        searchRecyclerView = findViewById(R.id.searchRecyclerView)

        database = FirebaseDatabase.getInstance().reference.child("documents")
        documents = mutableListOf()
        searchAdapter = DocumentAdapter(documents) { document ->
            val intent = Intent(this, DocumentEditorActivity::class.java)
            val gson = Gson()
            val documentJson = gson.toJson(document)
            intent.putExtra("document", documentJson)
            startActivity(intent)
        }

        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val resultText = matches?.firstOrNull() ?: ""
                searchView.setQuery(resultText, true)
            }

            override fun onError(error: Int) {
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        voiceSearchButton.setOnClickListener {
            startVoiceSearch()
        }

        focusAndShowKeyboard()
    }

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VOICE_SEARCH && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val resultText = results?.firstOrNull() ?: ""
            searchView.setQuery(resultText, true)
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            database.get().addOnSuccessListener { snapshot ->
                documents.clear()
                for (documentSnapshot in snapshot.children) {
                    val document = documentSnapshot.getValue(Document::class.java)
                    if (document != null) {
                        if (document.title.contains(query, ignoreCase = true) ||
                            document.content.contains(query, ignoreCase = true) ||
                            document.creatorUsername.contains(query, ignoreCase = true)) {
                            documents.add(document)
                        }
                    }
                }
                if (documents.isEmpty()) {
                    Toast.makeText(this, "No documents found", Toast.LENGTH_SHORT).show()
                }
                searchAdapter.setSearchQuery(query)
                searchAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun focusAndShowKeyboard() {
        searchView.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
    }

    companion object {
        private const val REQUEST_CODE_VOICE_SEARCH = 1
    }
}

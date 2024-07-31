package com.abtahiapp.docsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class EditedVersionFragment : Fragment() {

    private lateinit var pastEditors: List<PastEditor>
    private lateinit var originalTitle: String
    private lateinit var originalContent: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edited_version, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalTitle = arguments?.getString("originalTitle") ?: ""
        originalContent = arguments?.getString("originalContent") ?: ""
        val pastEditorsJson = arguments?.getString("pastEditors") ?: ""
        val gson = Gson()
        pastEditors = gson.fromJson(pastEditorsJson, Array<PastEditor>::class.java).toList()

        val pastEditorsRecyclerView = view.findViewById<RecyclerView>(R.id.pastEditorsRecyclerView)
        pastEditorsRecyclerView.layoutManager = LinearLayoutManager(context)
        val pastEditorAdapter = PastEditorAdapter(requireContext(), pastEditors, originalTitle, originalContent)
        pastEditorsRecyclerView.adapter = pastEditorAdapter
    }

    companion object {
        fun newInstance(originalTitle: String, originalContent: String, pastEditorsJson: String) =
            EditedVersionFragment().apply {
                arguments = Bundle().apply {
                    putString("originalTitle", originalTitle)
                    putString("originalContent", originalContent)
                    putString("pastEditors", pastEditorsJson)
                }
            }
    }
}

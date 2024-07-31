package com.abtahiapp.docsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class OriginalContentFragment : Fragment() {

    private var originalTitle: String? = null
    private var originalContent: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_original_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalTitle = arguments?.getString("originalTitle")
        originalContent = arguments?.getString("originalContent")
        view.findViewById<TextView>(R.id.originalTitle).text = originalTitle
        view.findViewById<TextView>(R.id.originalContent).text = originalContent
    }

    companion object {
        fun newInstance(originalTitle: String, originalContent: String) =
            OriginalContentFragment().apply {
                arguments = Bundle().apply {
                    putString("originalTitle", originalTitle)
                    putString("originalContent", originalContent)
                }
            }
    }
}

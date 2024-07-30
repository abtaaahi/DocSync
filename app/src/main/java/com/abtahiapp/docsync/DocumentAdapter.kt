package com.abtahiapp.docsync

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentAdapter(
    private val documents: List<Document>,
    private val onItemClick: (Document) -> Unit
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    private var searchQuery: String = ""

    fun setSearchQuery(query: String) {
        searchQuery = query
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(documents[position])
    }

    override fun getItemCount(): Int = documents.size

    inner class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        private val creatorTextView: TextView = itemView.findViewById(R.id.creatorTextView)
        private val creationTimeTextView: TextView = itemView.findViewById(R.id.creationTimeTextView)
        private val lastEditorTextView: TextView = itemView.findViewById(R.id.lastEditorTextView)
        private val lastEditTimeTextView: TextView = itemView.findViewById(R.id.lastEditTimeTextView)

        fun bind(document: Document) {
            titleTextView.text = highlightText(document.title)
            contentTextView.text = highlightText(document.content)
//            creatorTextView.text = highlightText("Created by: ${document.creatorUsername}")
            creatorTextView.text = highlightText(document.creatorUsername)
            creationTimeTextView.text = "${formatTimestamp(document.creationTime)}"

            if(document.lastEditorUsername.isEmpty()){
                lastEditorTextView.text = ""
            } else{
                lastEditorTextView.text = "Last Edited By: ${document.lastEditorUsername}"
            }

            if(document.lastEditTime == 0L){
                lastEditTimeTextView.text = ""
            } else{
                lastEditTimeTextView.text = "${formatTimestamp(document.lastEditTime)}"
            }

            itemView.setOnClickListener { onItemClick(document) }
        }

        private fun highlightText(text: String): SpannableString {
            val spannableString = SpannableString(text)
            val index = text.indexOf(searchQuery, ignoreCase = true)
            if (index >= 0) {
                spannableString.setSpan(
                    ForegroundColorSpan(Color.parseColor("#E8AA42")),
                    index,
                    index + searchQuery.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return spannableString
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
    }
}
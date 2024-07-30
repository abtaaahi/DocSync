package com.abtahiapp.docsync

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PastEditorAdapter(
    private val context: Context,
    private val pastEditors: List<PastEditor>,
    private val currentTitle: String,
    private val currentContent: String
) : RecyclerView.Adapter<PastEditorAdapter.PastEditorViewHolder>() {

    private val sortedPastEditors: List<PastEditor> by lazy {
        pastEditors.sortedByDescending { it.editTime }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastEditorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_past_editor, parent, false)
        return PastEditorViewHolder(view)
    }

    override fun onBindViewHolder(holder: PastEditorViewHolder, position: Int) {
        val pastEditor = sortedPastEditors[position]
        holder.bind(pastEditor)
    }

    override fun getItemCount(): Int = sortedPastEditors.size

    inner class PastEditorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val editTimeTextView: TextView = itemView.findViewById(R.id.editTimeTextView)
        private val updatedTitleTextView: TextView = itemView.findViewById(R.id.updatedTitleTextView)
        private val updatedContentTextView: TextView = itemView.findViewById(R.id.updatedContentTextView)

        fun bind(pastEditor: PastEditor) {
            usernameTextView.text = pastEditor.username
            editTimeTextView.text = formatTimestamp(pastEditor.editTime)

            val updatedTitle = getHighlightedText(currentTitle, pastEditor.updatedTitle, isTitle = true)
            val updatedContent = getHighlightedText(currentContent, pastEditor.updatedContent, isTitle = false)

            updatedTitleTextView.text = updatedTitle
            updatedContentTextView.text = updatedContent
        }

        private fun formatTimestamp(timestamp: Long): String {
            return if (timestamp != 0L) {
                val sdf = SimpleDateFormat("hh:mm a dd-MM-yy", Locale.getDefault())
                val date = Date(timestamp)
                sdf.format(date)
            } else {
                "N/A"
            }
        }

        private fun getHighlightedText(originalText: String, updatedText: String, isTitle: Boolean): SpannableString {
            val spannableString = SpannableString(updatedText)

            val color = if (isTitle) {
                ContextCompat.getColor(context, R.color.highlight_blue)
            } else {
                ContextCompat.getColor(context, R.color.highlight_red)
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
}

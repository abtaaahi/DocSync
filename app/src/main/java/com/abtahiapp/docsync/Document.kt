package com.abtahiapp.docsync

data class Document(
    val id: String = "",
    var title: String = "",
    var content: String = "",
    val creatorUsername: String = "",
    val creationTime: Long = 0L,
    var lastEditorUsername: String = "",
    var lastEditTime: Long = 0L,
    var pastEditors: List<PastEditor> = listOf(),
    val originalTitle: String = "",
    val originalContent: String = ""
)

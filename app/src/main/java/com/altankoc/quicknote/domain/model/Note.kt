package com.altankoc.quicknote.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Note(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val imagePath: String?,
    val date: Long
){
    fun getDisplayDate(): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(date))
    }
}

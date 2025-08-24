package com.altankoc.quicknote.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "notes_table")
data class NoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "note_title")
    val title: String,

    @ColumnInfo(name = "note_subtitle")
    val subtitle: String,

    @ColumnInfo(name = "note_description")
    val description: String,

    @ColumnInfo(name = "note_image")
    val imagePath: String?,

    @ColumnInfo(name = "note_date")
    val date: Long = System.currentTimeMillis()

)


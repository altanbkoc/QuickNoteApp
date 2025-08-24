package com.altankoc.quicknote.data.mapper

import com.altankoc.quicknote.data.entity.NoteEntity
import com.altankoc.quicknote.domain.model.Note


fun NoteEntity.toDomainModel(): Note {
    return Note(
        id = this.id,
        title = this.title,
        subtitle = this.subtitle,
        description = this.description,
        imagePath = this.imagePath,
        date = this.date
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = this.id,
        title = this.title,
        subtitle = this.subtitle,
        description = this.description,
        imagePath = this.imagePath,
        date = this.date
    )
}
package com.altankoc.quicknote.data.repository

import com.altankoc.quicknote.data.dao.NoteDao
import com.altankoc.quicknote.data.mapper.toDomainModel
import com.altankoc.quicknote.data.mapper.toEntity
import com.altankoc.quicknote.domain.model.Note
import com.altankoc.quicknote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository{


    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getNoteById(id: String): Note {
        return noteDao.getNoteById(id).toDomainModel()
    }

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

    override suspend fun searchNotes(searchText: String): List<Note> {
        return noteDao.searchNotes(searchText).map { it.toDomainModel() }
    }
}
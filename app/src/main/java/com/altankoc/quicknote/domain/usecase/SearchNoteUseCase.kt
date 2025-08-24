package com.altankoc.quicknote.domain.usecase

import com.altankoc.quicknote.domain.model.Note
import com.altankoc.quicknote.domain.repository.NoteRepository
import javax.inject.Inject

class SearchNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(searchText: String): List<Note> {
        return repository.searchNotes(searchText)
    }
}
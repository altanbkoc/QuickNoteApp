package com.altankoc.quicknote.ui.screens.note_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.quicknote.domain.model.Note
import com.altankoc.quicknote.domain.usecase.DeleteNoteUseCase
import com.altankoc.quicknote.domain.usecase.GetAllNotesUseCase
import com.altankoc.quicknote.domain.usecase.SearchNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val searchNoteUseCase: SearchNoteUseCase
) : ViewModel(){

    private val _state = MutableStateFlow(NoteListState())
    val state: StateFlow<NoteListState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        getAllNotes()
    }

    fun onEvent(event: NoteListEvent) {
        when(event) {
            is NoteListEvent.LoadNotes -> {
                getAllNotes()
            }

            is NoteListEvent.DeleteNote -> {
                deleteNote(event.note)
            }

            is NoteListEvent.SearchNotes -> {
                searchNotes(event.searchText)
            }

            is NoteListEvent.ClearSearch -> {
                clearSearch()
            }
        }
    }

    private fun getAllNotes(){
        viewModelScope.launch {
            getAllNotesUseCase().collect { notes ->
                _state.value = _state.value.copy(
                    notes = notes,
                    filteredNotes = if (_state.value.searchText.isBlank()) notes else _state.value.filteredNotes,
                    isLoading = false
                )
            }
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(note)
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun searchNotes(searchText: String){
        _state.value = _state.value.copy(searchText = searchText)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)

            if(searchText.isBlank()) {
                _state.value = _state.value.copy(
                    filteredNotes = _state.value.notes,
                    isSearching = false
                )
            } else {
                _state.value = _state.value.copy(isSearching = true)

                try {
                    val searchResults = searchNoteUseCase(searchText)
                    val sortedSearchResults = searchResults.sortedByDescending { it.date }

                    _state.value = _state.value.copy(
                        filteredNotes = sortedSearchResults,
                        isSearching = false
                    )
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        filteredNotes = emptyList(),
                        isSearching = false
                    )
                    e.printStackTrace()
                }
            }
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        _state.value = _state.value.copy(
            searchText = "",
            filteredNotes = _state.value.notes,
            isSearching = false
        )
    }
}

data class NoteListState(
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val searchText: String = "",
    val isLoading: Boolean = true,
    val isSearching: Boolean = false
)

sealed class NoteListEvent {
    object LoadNotes : NoteListEvent()
    data class DeleteNote(val note: Note) : NoteListEvent()
    data class SearchNotes(val searchText: String) : NoteListEvent()
    object ClearSearch : NoteListEvent()
}
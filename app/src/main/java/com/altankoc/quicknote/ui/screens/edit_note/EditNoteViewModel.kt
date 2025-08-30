package com.altankoc.quicknote.ui.screens.edit_note

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.quicknote.R
import com.altankoc.quicknote.domain.model.Note
import com.altankoc.quicknote.domain.usecase.DeleteNoteUseCase
import com.altankoc.quicknote.domain.usecase.GetNoteByIdUseCase
import com.altankoc.quicknote.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(EditNoteState())
    val state: StateFlow<EditNoteState> = _state.asStateFlow()

    private val noteId: String = savedStateHandle.get<String>("noteId") ?: ""

    init {
        if (noteId.isNotBlank()) {
            loadNote()
        } else {
            _state.value = _state.value.copy(
                error = application.getString(R.string.error_invalid_note_id)
            )
        }
    }

    fun onEvent(event: EditNoteEvent) {
        when (event) {
            is EditNoteEvent.TitleChanged -> {
                _state.value = _state.value.copy(
                    title = event.title,
                    hasUnsavedChanges = true
                )
            }

            is EditNoteEvent.SubtitleChanged -> {
                _state.value = _state.value.copy(
                    subtitle = event.subtitle,
                    hasUnsavedChanges = true
                )
            }

            is EditNoteEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(
                    description = event.description,
                    hasUnsavedChanges = true
                )
            }

            is EditNoteEvent.ImagePathChanged -> {
                _state.value = _state.value.copy(
                    imagePath = event.imagePath,
                    hasUnsavedChanges = true
                )
            }

            is EditNoteEvent.UpdateNote -> {
                updateNote()
            }

            is EditNoteEvent.DeleteNote -> {
                deleteNote()
            }

            is EditNoteEvent.ClearError -> {
                clearError()
            }
        }
    }

    private fun loadNote() {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val note = getNoteByIdUseCase(noteId)
                _state.value = _state.value.copy(
                    id = note.id,
                    title = note.title,
                    subtitle = note.subtitle,
                    description = note.description,
                    imagePath = note.imagePath,
                    date = note.date,
                    originalNote = note,
                    isLoading = false,
                    hasUnsavedChanges = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = application.getString(R.string.error_load_note, e.message ?: "")
                )
                e.printStackTrace()
            }
        }
    }

    private fun updateNote() {
        val currentState = _state.value

        if (currentState.title.isBlank() && currentState.description.isBlank()) {
            _state.value = _state.value.copy(
                error = application.getString(R.string.error_empty_note)
            )
            return
        }

        _state.value = _state.value.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val updatedNote = Note(
                    id = currentState.id,
                    title = currentState.title.trim(),
                    subtitle = currentState.subtitle.trim(),
                    description = currentState.description.trim(),
                    imagePath = currentState.imagePath?.takeIf { it.isNotBlank() },
                    date = currentState.date
                )

                updateNoteUseCase(updatedNote)

                _state.value = _state.value.copy(
                    isSaving = false,
                    isNoteUpdated = true,
                    hasUnsavedChanges = false,
                    originalNote = updatedNote
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = application.getString(R.string.error_update_note, e.message ?: "")
                )
                e.printStackTrace()
            }
        }
    }

    private fun deleteNote() {
        val currentState = _state.value
        _state.value = _state.value.copy(isDeleting = true)

        viewModelScope.launch {
            try {
                currentState.originalNote?.let { note ->
                    deleteNoteUseCase(note)
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        isNoteDeleted = true
                    )
                } ?: run {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        error = application.getString(R.string.error_delete_note_not_found)
                    )
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isDeleting = false,
                    error = application.getString(R.string.error_delete_note, e.message ?: "")
                )
                e.printStackTrace()
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun hasUnsavedChanges(): Boolean {
        return _state.value.hasUnsavedChanges
    }

}

data class EditNoteState(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val imagePath: String? = null,
    val date: Long = 0L,
    val originalNote: Note? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isNoteUpdated: Boolean = false,
    val isNoteDeleted: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null
) {
    fun canSave(): Boolean = title.isNotBlank() || description.isNotBlank()
}

sealed class EditNoteEvent {
    data class TitleChanged(val title: String) : EditNoteEvent()
    data class SubtitleChanged(val subtitle: String) : EditNoteEvent()
    data class DescriptionChanged(val description: String) : EditNoteEvent()
    data class ImagePathChanged(val imagePath: String?) : EditNoteEvent()
    object UpdateNote : EditNoteEvent()
    object DeleteNote : EditNoteEvent()
    object ClearError : EditNoteEvent()
}
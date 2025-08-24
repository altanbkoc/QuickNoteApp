package com.altankoc.quicknote.ui.screens.add_note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.quicknote.domain.model.Note
import com.altankoc.quicknote.domain.usecase.InsertNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val insertNoteUseCase: InsertNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddNoteState())
    val state: StateFlow<AddNoteState> = _state.asStateFlow()

    fun onEvent(event: AddNoteEvent) {
        when (event) {
            is AddNoteEvent.TitleChanged -> {
                _state.value = _state.value.copy(title = event.title)
            }

            is AddNoteEvent.SubtitleChanged -> {
                _state.value = _state.value.copy(subtitle = event.subtitle)
            }

            is AddNoteEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(description = event.description)
            }

            is AddNoteEvent.ImagePathChanged -> {
                _state.value = _state.value.copy(imagePath = event.imagePath)
            }

            is AddNoteEvent.SaveNote -> {
                saveNote()
            }

            is AddNoteEvent.ClearError -> {
                clearError()
            }
        }
    }

    private fun saveNote() {
        val currentState = _state.value

        if (currentState.title.isBlank() && currentState.description.isBlank()) {
            _state.value = _state.value.copy(
                error = "Please write something in title or description"
            )
            return
        }

        _state.value = _state.value.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val note = Note(
                    id = UUID.randomUUID().toString(),
                    title = currentState.title.trim(),
                    subtitle = currentState.subtitle.trim(),
                    description = currentState.description.trim(),
                    imagePath = currentState.imagePath?.takeIf { it.isNotBlank() },
                    date = System.currentTimeMillis()
                )

                insertNoteUseCase(note)

                _state.value = _state.value.copy(
                    isSaving = false,
                    isNoteSaved = true
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = "Failed to save note: ${e.message}"
                )
                e.printStackTrace()
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

}

data class AddNoteState(
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val imagePath: String? = null,
    val isSaving: Boolean = false,
    val isNoteSaved: Boolean = false,
    val error: String? = null
) {
    fun canSave(): Boolean = title.isNotBlank() || description.isNotBlank()
}

sealed class AddNoteEvent {
    data class TitleChanged(val title: String) : AddNoteEvent()
    data class SubtitleChanged(val subtitle: String) : AddNoteEvent()
    data class DescriptionChanged(val description: String) : AddNoteEvent()
    data class ImagePathChanged(val imagePath: String?) : AddNoteEvent()
    object SaveNote : AddNoteEvent()
    object ClearError : AddNoteEvent()
}
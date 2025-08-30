package com.altankoc.quicknote.ui.screens.navigation

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object AddNote : Screen("add_note")
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String): String {
            return "edit_note/$noteId"
        }
    }
}
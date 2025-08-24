package com.altankoc.quicknote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.altankoc.quicknote.ui.screens.add_note.AddNoteScreen
import com.altankoc.quicknote.ui.screens.edit_note.EditNoteScreen
import com.altankoc.quicknote.ui.screens.note_list.NoteListScreen

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object AddNote : Screen("add_note")
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String): String {
            return "edit_note/$noteId"
        }
    }
}

@Composable
fun QuickNoteNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        // Note List Screen
        composable(route = Screen.NoteList.route) {
            NoteListScreen(
                onAddNoteClick = {
                    navController.navigate(Screen.AddNote.route)
                },
                onEditNoteClick = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                }
            )
        }

        // Add Note Screen
        composable(route = Screen.AddNote.route) {
            AddNoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Note Screen
        composable(
            route = Screen.EditNote.route,
            arguments = listOf(
                androidx.navigation.navArgument("noteId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) {
            EditNoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
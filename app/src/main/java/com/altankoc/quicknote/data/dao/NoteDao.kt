package com.altankoc.quicknote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.altankoc.quicknote.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {


    @Query("SELECT * FROM notes_table ORDER BY note_date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes_table WHERE note_id = :id")
    suspend fun getNoteById(id: String): NoteEntity

    @Query("SELECT * FROM notes_table WHERE note_title LIKE '%' || :searchText || '%' OR note_subtitle LIKE '%' || :searchText || '%'")
    suspend fun searchNotes(searchText: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(noteEntity: NoteEntity)

    @Delete
    suspend fun deleteNote(noteEntity: NoteEntity)

    @Update
    suspend fun updateNote(noteEntity: NoteEntity)



}
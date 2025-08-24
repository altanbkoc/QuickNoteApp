package com.altankoc.quicknote.di

import com.altankoc.quicknote.data.dao.NoteDao
import com.altankoc.quicknote.data.repository.NoteRepositoryImpl
import com.altankoc.quicknote.domain.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(noteDao)
    }
}
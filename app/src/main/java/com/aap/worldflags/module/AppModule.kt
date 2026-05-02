package com.aap.worldflags.module

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aap.worldflags.R
import com.aap.worldflags.data.MediaData
import com.aap.worldflags.repo.GameDataCreationRepository
import com.aap.worldflags.repo.GameDataCreationRepositoryImpl
import com.aap.worldflags.repo.GamePlayRepository
import com.aap.worldflags.repo.GamePlayRepositoryImpl
import com.aap.worldflags.repo.PastGameRepository
import com.aap.worldflags.repo.PastGameRepositoryImpl
import com.aap.worldflags.room.GameDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDb(application: Application): GameDatabase {
        val db = Room.databaseBuilder(
            application.applicationContext,
            GameDatabase::class.java,
            "game_database.db"
        )
            .setQueryCallback(queryCallback = DebugDBCallback(), executor = Executors.newSingleThreadExecutor())
            .fallbackToDestructiveMigration(dropAllTables = true).build()
        return db

    }

    class DebugDBCallback: RoomDatabase.QueryCallback {
        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
            android.util.Log.d("YYYY", "SQL Query: $sqlQuery SQL Args: $bindArgs")

        }
    }

    @Singleton
    @Provides
    fun provideDataStore(application: Application): DataStore<Preferences> = application.dataStore

    private val Context.dataStore by preferencesDataStore(
        name = "game_prefs"
    )

    @Singleton
    @Provides
    fun providePastGameRepository(gameDatabase: GameDatabase): PastGameRepository {
        return PastGameRepositoryImpl(gameDatabase.pastScoresDao())
    }

    @Singleton
    @Provides
    fun provideGameRepository(
        gameDatabase: GameDatabase,
        gameCreationRepository: GameDataCreationRepository,
        dataStore: DataStore<Preferences>
    ): GamePlayRepository {
        return GamePlayRepositoryImpl(gameDatabase, gameCreationRepository, dataStore)
    }

    @Singleton
    @Provides
    fun provideGameCreationRepository(@ApplicationContext appContext: Context): GameDataCreationRepository {
        return GameDataCreationRepositoryImpl(appContext)
    }

    @Singleton
    @Provides
    fun provideMediaData(@ApplicationContext appContext: Context): MediaData {
        return MediaData(MediaPlayer.create(appContext, R.raw.correct), MediaPlayer.create(appContext, R.raw.error))
    }

}


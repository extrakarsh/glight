package com.example.glight.di

import android.content.Context
import androidx.room.Room
import com.example.glight.BuildConfig
import com.example.glight.data.ai.OpenRouterAiProvider
import com.example.glight.data.auth.AuthRepositoryImpl
import com.example.glight.data.local.AppDatabase
import com.example.glight.data.local.dao.ComplaintDao
import com.example.glight.data.local.dao.PoleDao
import com.example.glight.data.local.dao.SyncQueueDao
import com.example.glight.data.remote.FirebaseRemoteDataSource
import com.example.glight.data.remote.RemoteDataSource
import com.example.glight.data.repository.ComplaintRepositoryImpl
import com.example.glight.data.repository.PoleRepositoryImpl
import com.example.glight.data.sync.SyncManager
import com.example.glight.domain.ai.AiProvider
import com.example.glight.domain.repository.AuthRepository
import com.example.glight.domain.repository.ComplaintRepository
import com.example.glight.domain.repository.PoleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "glight_database")
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun providePoleDao(db: AppDatabase): PoleDao = db.poleDao()

    @Provides
    fun provideComplaintDao(db: AppDatabase): ComplaintDao = db.complaintDao()

    @Provides
    fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase? {
        return try {
            val url = BuildConfig.FIREBASE_DATABASE_URL.ifBlank { null }
            if (url != null) {
                FirebaseDatabase.getInstance(url)
            } else {
                FirebaseDatabase.getInstance()
            }
        } catch (_: Exception) {
            null
        }
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(firebaseDatabase: FirebaseDatabase?): RemoteDataSource {
        return FirebaseRemoteDataSource(firebaseDatabase)
    }

    @Provides
    @Singleton
    fun provideAiProvider(): AiProvider {
        return OpenRouterAiProvider(
            apiKey = BuildConfig.OPENROUTER_API_KEY,
            model = BuildConfig.OPENROUTER_MODEL
        )
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth?): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideSyncManager(syncQueueDao: SyncQueueDao, remoteDataSource: RemoteDataSource): SyncManager {
        return SyncManager(syncQueueDao, remoteDataSource)
    }

    @Provides
    @Singleton
    fun providePoleRepository(
        poleDao: PoleDao,
        remoteDataSource: RemoteDataSource,
        syncManager: SyncManager
    ): PoleRepository {
        return PoleRepositoryImpl(poleDao, remoteDataSource, syncManager)
    }

    @Provides
    @Singleton
    fun provideComplaintRepository(
        complaintDao: ComplaintDao,
        remoteDataSource: RemoteDataSource,
        syncManager: SyncManager
    ): ComplaintRepository {
        return ComplaintRepositoryImpl(complaintDao, remoteDataSource, syncManager)
    }
}

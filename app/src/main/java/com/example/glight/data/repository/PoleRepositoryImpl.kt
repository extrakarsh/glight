package com.example.glight.data.repository

import com.example.glight.data.local.InitialDataProvider
import com.example.glight.data.local.dao.PoleDao
import com.example.glight.data.mapper.toDomainModel
import com.example.glight.data.mapper.toEntity
import com.example.glight.data.remote.RemoteDataSource
import com.example.glight.data.sync.SyncManager
import com.example.glight.domain.model.Pole
import com.example.glight.domain.repository.PoleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoleRepositoryImpl @Inject constructor(
    private val poleDao: PoleDao,
    private val remoteDataSource: RemoteDataSource,
    private val syncManager: SyncManager
) : PoleRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            seedIfEmpty()
        }
    }

    override fun getPoles(): Flow<List<Pole>> {
        return poleDao.getAllPoles().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun syncPoles(): Flow<Unit> {
        return kotlinx.coroutines.flow.flow {
            refreshPoles()
            emit(Unit)
        }
    }

    override suspend fun getPoleById(id: String): Pole? {
        return poleDao.getPoleById(id)?.toDomainModel()
    }

    override suspend fun refreshPoles() {
        try {
            val result = remoteDataSource.getPoles()
            result.onSuccess { remotePoles ->
                if (remotePoles.isNotEmpty()) {
                    poleDao.insertPoles(remotePoles.map { it.toEntity() })
                }
            }
        } catch (_: Exception) { }
        seedIfEmpty()
    }

    override suspend fun updatePoleStatus(poleId: String, status: String) {
        try {
            val currentPole = poleDao.getPoleById(poleId)
            if (currentPole != null) {
                poleDao.insertPole(currentPole.copy(status = status))
            }
            syncManager.enqueue(
                entityType = "POLE",
                entityId = poleId,
                operation = "UPDATE_POLE_STATUS",
                payload = status
            )
        } catch (_: Exception) { }
    }

    private suspend fun seedIfEmpty() {
        if (poleDao.getAllPoles().first().isEmpty()) {
            poleDao.insertPoles(InitialDataProvider.getInitialPoles().map { it.toEntity() })
        }
    }
}

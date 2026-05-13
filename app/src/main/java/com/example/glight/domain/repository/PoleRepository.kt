package com.example.glight.domain.repository

import com.example.glight.domain.model.Pole
import kotlinx.coroutines.flow.Flow

interface PoleRepository {
    fun getPoles(): Flow<List<Pole>>
    fun syncPoles(): Flow<Unit>
    suspend fun getPoleById(id: String): Pole?
    suspend fun refreshPoles()
    suspend fun updatePoleStatus(poleId: String, status: String)
}

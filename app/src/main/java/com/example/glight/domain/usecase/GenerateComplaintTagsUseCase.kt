package com.example.glight.domain.usecase

import com.example.glight.domain.ai.AiProvider
import javax.inject.Inject

class GenerateComplaintTagsUseCase @Inject constructor(
    private val aiProvider: AiProvider
) {
    suspend operator fun invoke(description: String, reportedStatus: String): List<String> {
        return aiProvider.generateTags(description, reportedStatus)
    }

    suspend fun suggestedFix(description: String, reportedStatus: String): String {
        return aiProvider.suggestFix(description, reportedStatus)
    }

    fun monthlyEnergySummary(daytimeReports: Int, estimatedKwhSaved: Double, fixedComplaints: Int): String {
        return aiProvider.monthlyEnergySummary(daytimeReports, estimatedKwhSaved, fixedComplaints)
    }
}

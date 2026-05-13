package com.example.glight.domain.ai

interface AiProvider {
    suspend fun generateTags(description: String, reportedStatus: String): List<String>
    suspend fun suggestFix(description: String, reportedStatus: String): String
    suspend fun categorizeComplaint(description: String, reportedStatus: String): String
    suspend fun detectAnomalies(complaintHistory: List<String>): List<String>
    fun monthlyEnergySummary(daytimeReports: Int, estimatedKwhSaved: Double, fixedComplaints: Int): String
}

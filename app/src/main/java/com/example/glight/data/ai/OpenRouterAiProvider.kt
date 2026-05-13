package com.example.glight.data.ai

import com.example.glight.domain.ai.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class OpenRouterAiProvider @Inject constructor(
    private val apiKey: String,
    private val model: String
) : AiProvider {

    override suspend fun generateTags(description: String, reportedStatus: String): List<String> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext fallbackTags(reportedStatus, description)
            runCatching {
                chat(
                    """
                    You are an assistant for Grameen-Light, a civic streetlight audit app.
                    Status reported: $reportedStatus
                    Notes: $description
                    Generate 1-3 short relevant tags.
                    Return only tags separated by commas.
                    """.trimIndent(),
                    maxTokens = 60
                ).split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
                    .take(3)
                    .ifEmpty { fallbackTags(reportedStatus, description) }
            }.getOrElse { fallbackTags(reportedStatus, description) }
        }

    override suspend fun suggestFix(description: String, reportedStatus: String): String =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext fallbackSuggestion(reportedStatus)
            runCatching {
                chat(
                    """
                    You assist Panchayat repair teams for Grameen-Light streetlight reports.
                    Status: $reportedStatus
                    Notes: $description
                    Suggest one likely cause and one short repair action in under 25 words.
                    """.trimIndent(),
                    maxTokens = 90
                ).ifBlank { fallbackSuggestion(reportedStatus) }
            }.getOrElse { fallbackSuggestion(reportedStatus) }
        }

    override suspend fun categorizeComplaint(description: String, reportedStatus: String): String {
        return when {
            "BURNING_DAYTIME" in reportedStatus -> "Energy Waste"
            "FUSED" in reportedStatus -> "Safety Hazard"
            else -> "Routine Audit"
        }
    }

    override suspend fun detectAnomalies(complaintHistory: List<String>): List<String> {
        val anomalies = mutableListOf<String>()
        val fusedCount = complaintHistory.count { "FUSED" in it }
        val daytimeCount = complaintHistory.count { "BURNING_DAYTIME" in it }
        if (fusedCount > 3) anomalies.add("High fuse rate detected - possible supply line issue")
        if (daytimeCount > 2) anomalies.add("Recurring daytime burns - timer/sensor inspection recommended")
        return anomalies
    }

    override fun monthlyEnergySummary(daytimeReports: Int, estimatedKwhSaved: Double, fixedComplaints: Int): String {
        return if (daytimeReports == 0) {
            "No daytime burning reports this month. Start a day audit to build savings data."
        } else {
            "Community reports prevented an estimated ${"%.1f".format(estimatedKwhSaved)} kWh of waste this month from $daytimeReports daytime alerts. $fixedComplaints issues have been resolved."
        }
    }

    private fun chat(prompt: String, maxTokens: Int): String {
        val connection = (URL("https://openrouter.ai/api/v1/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("HTTP-Referer", "https://grameen-light.local")
            setRequestProperty("X-Title", "Grameen-Light")
        }

        val body = JSONObject()
            .put("model", model.ifBlank { DEFAULT_MODEL })
            .put("temperature", 0.2)
            .put("max_tokens", maxTokens)
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", "Return concise, practical civic repair guidance."))
                    .put(JSONObject().put("role", "user").put("content", prompt))
            )

        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val responseText = if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }
            error("OpenRouter request failed with HTTP ${connection.responseCode}")
        }

        return JSONObject(responseText)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .optString("content")
            .trim()
    }

    private fun fallbackTags(reportedStatus: String, description: String): List<String> {
        val normalized = "$reportedStatus $description".lowercase()
        return when {
            "burn" in normalized || "day" in normalized -> listOf("Daytime Waste", "Switching Error")
            "flicker" in normalized -> listOf("Flickering", "Wiring Check")
            "fused" in normalized || "not" in normalized -> listOf("Bulb Fused", "Night Safety")
            else -> listOf("Streetlight Audit")
        }
    }

    private fun fallbackSuggestion(reportedStatus: String): String = when (reportedStatus) {
        "BURNING_DAYTIME" -> "Likely timer or manual switch issue. Verify switch schedule and photocell control."
        "FUSED" -> "Likely bulb or wiring fault. Inspect holder, replace bulb, and test supply."
        else -> "Pole marked working. Confirm night operation during the next audit round."
    }

    private companion object {
        const val DEFAULT_MODEL = "openrouter/free"
    }
}

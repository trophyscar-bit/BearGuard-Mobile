package com.bearguard.mobile.giftcode

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

/**
 * Ported 1:1 from Bearguard-Win's GiftCodeClient.java (fg-app/.../misc/GiftCodeClient.java) --
 * pure HTTP, no screen/emulator dependency, so nothing needed changing except the HTTP client
 * (java.net.http.HttpClient isn't available below API 34; OkHttp instead) and JSON parsing
 * (org.json instead of Jackson -- both response shapes here are trivial).
 */
class GiftCodeClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    data class GiftCodeEntry(val code: String, val discoveredOn: LocalDate?) {
        fun displayDate(): String =
            if (discoveredOn == null) "Discovery date unknown"
            else "Discovered " + SOURCE_DATE.format(discoveredOn)
    }

    fun fetchActiveCodes(): List<GiftCodeEntry> {
        val request = Request.Builder()
            .url(SOURCE_URL)
            .header("X-API-Key", API_KEY)
            .header("Accept", "application/json")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Gift code source returned HTTP ${response.code}")
            }
            return parseResponse(response.body?.string() ?: "")
        }
    }

    fun parseResponse(json: String): List<GiftCodeEntry> {
        val codes = JSONObject(json).optJSONArray("codes")
            ?: throw IOException("Gift code source response has no codes array")

        val unique = LinkedHashMap<String, GiftCodeEntry>()
        for (i in 0 until codes.length()) {
            val entry = parseEntry(codes.optString(i, ""))
            if (entry != null) {
                unique.putIfAbsent(entry.code, entry)
            }
        }
        return unique.values.toList()
    }

    private fun parseEntry(raw: String?): GiftCodeEntry? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null

        val split = value.lastIndexOf(' ')
        if (split < 1) return GiftCodeEntry(value, null)

        val code = value.substring(0, split).trim()
        val rawDate = value.substring(split + 1).trim()
        return try {
            GiftCodeEntry(code, LocalDate.parse(rawDate, SOURCE_DATE))
        } catch (ignored: DateTimeParseException) {
            GiftCodeEntry(value, null)
        }
    }

    companion object {
        const val SOURCE_URL = "https://gift-code-api.whiteout-bot.com/giftcode_api.php"
        private const val API_KEY = "super_secret_bot_token_nobody_will_ever_find"
        private val SOURCE_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
    }
}

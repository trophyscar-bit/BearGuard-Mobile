package com.bearguard.mobile.giftcode

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Ported 1:1 from Bearguard-Win's GiftCodeRedeemer.java (fg-app/.../misc/GiftCodeRedeemer.java) --
 * same signed-request shape (MD5(sorted-fields + secret)), same classify() outcome logic, hitting
 * the same official Century Games endpoint. Only the HTTP client changed (OkHttp instead of
 * java.net.http.HttpClient, which needs API 34+; minSdk here is 30).
 */
class GiftCodeRedeemer(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    enum class RedeemOutcome { REDEEMED, ALREADY_REDEEMED, FAILED, RETRYABLE_ERROR }

    data class RedeemResult(val message: String, val outcome: RedeemOutcome, val terminal: Boolean) {
        companion object {
            fun failed(message: String) = RedeemResult(message, RedeemOutcome.FAILED, true)
            fun retryable(message: String) = RedeemResult(message, RedeemOutcome.RETRYABLE_ERROR, false)
        }
    }

    fun redeem(playerId: String, region: String, giftCode: String?): RedeemResult {
        if (!isDigits(playerId) || !isDigits(region) || giftCode.isNullOrBlank()) {
            return RedeemResult.failed("Invalid player ID, region, or gift code")
        }

        return try {
            val response = post(signed(requestFields(playerId, region, giftCode, Instant.now().epochSecond)))
            classify(response.optString("msg", "Unknown redemption response"))
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            RedeemResult.retryable("Redemption interrupted")
        } catch (exception: Exception) {
            RedeemResult.retryable(exception.message ?: exception.javaClass.simpleName)
        }
    }

    fun requestFields(playerId: String, region: String, giftCode: String, epochSeconds: Long): Map<String, String> =
        linkedMapOf(
            "fid" to playerId,
            "cdk" to giftCode,
            "kid" to region,
            "time" to epochSeconds.toString(),
        )

    fun signed(fields: Map<String, String>): Map<String, String> {
        val sorted = fields.entries.sortedBy { it.key }
        val payload = sorted.joinToString("&") { "${it.key}=${it.value}" }
        val signed = LinkedHashMap<String, String>()
        signed["sign"] = md5(payload + SIGNING_SECRET)
        signed.putAll(fields)
        return signed
    }

    private fun post(fields: Map<String, String>): JSONObject {
        val bodyBuilder = FormBody.Builder()
        fields.forEach { (key, value) -> bodyBuilder.add(key, value) }

        val request = Request.Builder()
            .url(REDEEM_URL)
            .header("Accept", "application/json, text/plain, */*")
            .header("Origin", "https://wos-giftcode.centurygame.com")
            .header("Referer", "https://wos-giftcode.centurygame.com/")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 Chrome/134 Safari/537.36")
            .post(bodyBuilder.build())
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("gift_code returned HTTP ${response.code}")
            }
            return JSONObject(response.body?.string() ?: "{}")
        }
    }

    private fun md5(value: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun isDigits(value: String?): Boolean = value != null && value.matches(Regex("\\d+"))

    companion object {
        private const val REDEEM_URL = "https://wos-giftcode-api.centurygame.com/api/gift_code"
        private const val SIGNING_SECRET = "tB87#kPtkxqOS2"

        fun classify(rawMessage: String?): RedeemResult {
            val message = if (rawMessage.isNullOrBlank()) "Unknown response" else rawMessage.trim()
            val normalized = message.uppercase().replace('_', ' ')
            return when {
                normalized.startsWith("SUCCESS") -> RedeemResult(message, RedeemOutcome.REDEEMED, true)
                normalized.startsWith("RECEIVED") || normalized.contains("SAME TYPE EXCHANGE") ||
                    normalized.contains("ALREADY") -> RedeemResult(message, RedeemOutcome.ALREADY_REDEEMED, true)
                normalized.contains("EXPIRED") || normalized.contains("NOT FOUND") ||
                    normalized.contains("LIMIT") || normalized.contains("REQUIREMENT") ||
                    normalized.contains("SPEND MORE") || normalized.contains("TIME ERROR") ||
                    normalized.contains("ROLE NOT EXIST") || normalized.contains("PLAYER NOT EXIST") ->
                    RedeemResult.failed(message)
                else -> RedeemResult.retryable(message)
            }
        }
    }
}

package uklot.connectionltd.alotbot.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class StartupRuleClient(
    private val endpoint: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchSecondRule(
        simLocale: String,
        locale: String,
        uuid: String,
        deviceNameAndModel: String,
        timezone: String
    ): String? = withContext(Dispatchers.IO) {
        val payload = JsonObject().apply {
            addProperty("cdes", simLocale)
            addProperty("rvcc", locale)
            addProperty("qahb", uuid)
            addProperty("jmmj", deviceNameAndModel)
            addProperty("pokj", timezone)
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@use null

                val json = JsonParser.parseString(body).asJsonObject
                val rule = json.get("second_rule")?.asString?.trim().orEmpty()
                if (rule.isBlank() || !isHttpUrl(rule)) null else rule
            }
        }.getOrNull()
    }

    private fun isHttpUrl(value: String): Boolean {
        return try {
            val uri = java.net.URI(value)
            !uri.host.isNullOrBlank() &&
                (uri.scheme.equals("http", ignoreCase = true) ||
                    uri.scheme.equals("https", ignoreCase = true))
        } catch (_: Exception) {
            false
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

package uklot.connectionltd.alotbot.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.delay
import uklot.connectionltd.alotbot.core.AppConfig
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class FalApiClient {
    companion object {
        private const val TAG = "MelBall/FalApiClient"
    }

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generate(prompt: String, sourceBitmap: Bitmap?): Bitmap {
        Log.d(TAG, "Starting generate; withImage=${sourceBitmap != null}, promptLength=${prompt.length}")
        val payload = JsonObject().apply {
            addProperty("prompt", prompt)
            addProperty("num_inference_steps", 30)
            addProperty("num_images", 1)
            addProperty("enable_safety_checker", true)
            addProperty("guidance_scale", 3.5)
            if (sourceBitmap != null) {
                val uploadedImageUrl = uploadImageToFalStorage(sourceBitmap)
                addProperty("image_url", uploadedImageUrl)
                addProperty("strength", 0.25)
                Log.d(TAG, "Uploaded source image url=$uploadedImageUrl")
            }
        }

        val endpoint = if (sourceBitmap != null) {
            "fal-ai/flux-pro/kontext"
        } else {
            "fal-ai/flux-pro/kontext/text-to-image"
        }
        Log.d(TAG, "Selected endpoint=$endpoint")

        val queueResponse = postJson("https://queue.fal.run/$endpoint", payload, endpoint)
        val directImage = extractImageUrl(queueResponse)
        if (directImage != null) {
            Log.d(TAG, "Direct image URL received from enqueue response")
            return downloadBitmap(directImage)
        }

        val responseUrl = queueResponse.get("response_url")?.asString
        val statusUrl = queueResponse.get("status_url")?.asString
        val requestId = queueResponse.get("request_id")?.asString

        repeat(120) { attempt ->
            delay(1500)
            Log.d(TAG, "Polling attempt=${attempt + 1} for endpoint=$endpoint")

            val response = when {
                !statusUrl.isNullOrBlank() -> getJson(statusUrl, allowInProgress = true)
                !responseUrl.isNullOrBlank() -> getJson(responseUrl, allowInProgress = true)
                !requestId.isNullOrBlank() -> {
                    getJson(
                        url = "https://queue.fal.run/$endpoint/requests/$requestId/status",
                        allowInProgress = true
                    )
                }

                else -> throw IllegalStateException("FAL queue returned no polling URLs")
            }

            var maybeImage = extractImageUrl(response)
            if (maybeImage == null && response.has("response_url")) {
                val latestResponseUrl = response.get("response_url")?.asString
                if (!latestResponseUrl.isNullOrBlank()) {
                    val responsePayload = getJson(latestResponseUrl, allowInProgress = true)
                    maybeImage = extractImageUrl(responsePayload)
                }
            }
            if (maybeImage != null) {
                Log.d(TAG, "Image URL received on poll attempt=${attempt + 1}")
                return downloadBitmap(maybeImage)
            }
        }

        Log.e(TAG, "Generation timeout for endpoint=$endpoint")
        throw IllegalStateException("Generation timeout. Try again.")
    }

    private fun extractImageUrl(json: JsonObject): String? {
        if (json.has("images")) {
            val images = json.getAsJsonArray("images")
            if (images.size() > 0) {
                return images[0].asJsonObject.get("url")?.asString
            }
        }

        if (json.has("response")) {
            val nested = json.getAsJsonObject("response")
            if (nested.has("images")) {
                val images = nested.getAsJsonArray("images")
                if (images.size() > 0) {
                    return images[0].asJsonObject.get("url")?.asString
                }
            }
        }
        return null
    }

    private fun postJson(url: String, payload: JsonObject, endpoint: String): JsonObject {
        val request = Request.Builder()
            .url(url)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Key ${AppConfig.falApiKey}")
            .addHeader("Content-Type", "application/json")
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                Log.d(TAG, "enqueue response endpoint=$endpoint code=${response.code}")
                if (!response.isSuccessful) {
                    Log.e(TAG, "enqueue failed endpoint=$endpoint code=${response.code} body=$body")
                    throw IllegalStateException("FAL request failed (${response.code}): $body")
                }
                return gson.fromJson(body, JsonObject::class.java)
            }
        }.getOrElse { error ->
            Log.e(TAG, "enqueue exception endpoint=$endpoint message=${error.message}", error)
            throw error
        }
    }

    private fun getJson(url: String, allowInProgress: Boolean = false): JsonObject {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Key ${AppConfig.falApiKey}")
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    if (
                        allowInProgress &&
                        response.code == 400 &&
                        body.contains("still in progress", ignoreCase = true)
                    ) {
                        Log.d(TAG, "Polling not ready yet url=$url code=${response.code}")
                        return gson.fromJson(body, JsonObject::class.java)
                    }
                    Log.e(TAG, "polling failed url=$url code=${response.code} body=$body")
                    throw IllegalStateException("FAL polling failed (${response.code}): $body")
                }
                return gson.fromJson(body, JsonObject::class.java)
            }
        }.getOrElse { error ->
            Log.e(TAG, "polling exception url=$url message=${error.message}", error)
            throw error
        }
    }

    private fun downloadBitmap(url: String): Bitmap {
        Log.d(TAG, "Downloading image from urlPrefix=${url.take(80)}")
        if (url.startsWith("data:image")) {
            val base64Part = url.substringAfter("base64,")
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Failed to decode base64 image")
        }

        val request = Request.Builder().url(url).get().build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "image download failed code=${response.code}")
                    throw IllegalStateException("Image download failed: ${response.code}")
                }
                return BitmapFactory.decodeStream(response.body?.byteStream())
                    ?: throw IllegalStateException("Failed to decode generated image")
            }
        }.getOrElse { error ->
            Log.e(TAG, "image download exception message=${error.message}", error)
            throw error
        }
    }

    private fun uploadImageToFalStorage(bitmap: Bitmap): String {
        val resized = bitmap.resizeIfNeeded(maxSide = 1024)
        val bytes = resized.toPngBytes()

        val initiatePayload = JsonObject().apply {
            addProperty("content_type", "image/png")
            addProperty("file_name", "melball_${System.currentTimeMillis()}.png")
        }

        val initiateRequest = Request.Builder()
            .url("https://rest.fal.ai/storage/upload/initiate?storage_type=fal-cdn-v3")
            .post(gson.toJson(initiatePayload).toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Key ${AppConfig.falApiKey}")
            .addHeader("Content-Type", "application/json")
            .build()

        val initiateResponse = client.newCall(initiateRequest).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.e(TAG, "storage initiate failed code=${response.code} body=$body")
                throw IllegalStateException("Storage initiate failed (${response.code}): $body")
            }
            gson.fromJson(body, JsonObject::class.java)
        }

        val uploadUrl = initiateResponse.get("upload_url")?.asString
            ?: throw IllegalStateException("Missing upload_url from storage initiate")
        val fileUrl = initiateResponse.get("file_url")?.asString
            ?: throw IllegalStateException("Missing file_url from storage initiate")

        val uploadRequest = Request.Builder()
            .url(uploadUrl)
            .put(bytes.toRequestBody("image/png".toMediaType()))
            .addHeader("Content-Type", "image/png")
            .build()

        client.newCall(uploadRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                Log.e(TAG, "storage upload failed code=${response.code} body=$body")
                throw IllegalStateException("Storage upload failed (${response.code}): $body")
            }
        }

        return fileUrl
    }

    private fun Bitmap.toDataUrl(): String {
        val output = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, output)
        val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        return "data:image/png;base64,$base64"
    }

    private fun Bitmap.toPngBytes(): ByteArray {
        val output = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, output)
        return output.toByteArray()
    }

    private fun Bitmap.resizeIfNeeded(maxSide: Int): Bitmap {
        val width = width
        val height = height
        val longest = maxOf(width, height)
        if (longest <= maxSide) return this

        val scale = maxSide.toFloat() / longest.toFloat()
        val newWidth = (width * scale).toInt().coerceAtLeast(1)
        val newHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
    }
}

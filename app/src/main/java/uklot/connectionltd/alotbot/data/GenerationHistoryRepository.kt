package uklot.connectionltd.alotbot.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import uklot.connectionltd.alotbot.model.BallType
import uklot.connectionltd.alotbot.model.GenerationRecord
import java.io.File
import java.io.FileOutputStream

class GenerationHistoryRepository(private val context: Context) {
    private val prefs = PreferencesStore(context)
    private val gson = Gson()
    private val historyDir: File by lazy {
        File(context.filesDir, "generations").apply { mkdirs() }
    }

    fun add(bitmap: Bitmap, prompt: String, ballType: BallType): GenerationRecord {
        val timestamp = System.currentTimeMillis()
        val file = File(historyDir, "$timestamp.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }

        val record = GenerationRecord(
            timestamp = timestamp,
            ballType = ballType,
            prompt = prompt,
            imagePath = file.absolutePath
        )

        val current = getAll().toMutableList()
        current.add(0, record)
        saveAll(current)
        return record
    }

    fun getAll(): List<GenerationRecord> {
        val type = object : TypeToken<List<GenerationRecord>>() {}.type
        return gson.fromJson<List<GenerationRecord>>(prefs.historyJson, type).orEmpty()
            .filter { File(it.imagePath).exists() }
            .sortedByDescending { it.timestamp }
    }

    fun clear() {
        getAll().forEach { File(it.imagePath).delete() }
        saveAll(emptyList())
    }

    fun deleteByImagePath(imagePath: String) {
        val current = getAll().toMutableList()
        current.removeAll { it.imagePath == imagePath }
        File(imagePath).delete()
        saveAll(current)
    }

    fun loadBitmap(path: String): Bitmap? = BitmapFactory.decodeFile(path)

    private fun saveAll(records: List<GenerationRecord>) {
        prefs.historyJson = gson.toJson(records)
    }
}

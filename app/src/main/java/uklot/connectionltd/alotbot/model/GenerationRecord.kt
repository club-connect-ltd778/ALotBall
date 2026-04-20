package uklot.connectionltd.alotbot.model

data class GenerationRecord(
    val timestamp: Long,
    val ballType: BallType,
    val prompt: String,
    val imagePath: String
)

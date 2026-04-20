package uklot.connectionltd.alotbot.core

import uklot.connectionltd.alotbot.model.BallType

object PromptFactory {
    fun build(ballType: BallType, userInput: String): String {
        return ballType.promptTemplate.replace("{USER_TEXT}", userInput.trim())
    }
}

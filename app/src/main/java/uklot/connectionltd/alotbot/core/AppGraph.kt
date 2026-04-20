package uklot.connectionltd.alotbot.core

import android.content.Context
import uklot.connectionltd.alotbot.data.GenerationHistoryRepository
import uklot.connectionltd.alotbot.data.PreferencesStore
import uklot.connectionltd.alotbot.network.FalApiClient
import uklot.connectionltd.alotbot.network.StartupRuleClient

object AppGraph {
    fun prefs(context: Context): PreferencesStore = PreferencesStore(context.applicationContext)

    fun historyRepository(context: Context): GenerationHistoryRepository =
        GenerationHistoryRepository(context.applicationContext)

    fun falApiClient(): FalApiClient = FalApiClient()

    fun startupRuleClient(): StartupRuleClient = StartupRuleClient(AppConfig.startupRulesUrl)
}

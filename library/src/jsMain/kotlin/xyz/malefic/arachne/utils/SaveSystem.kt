package xyz.malefic.arachne.utils

import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set

@Serializable
data class GameSaveData(
    val version: String = "1.0",
    val timestamp: Long = 0,
    val playerData: Map<String, String> = emptyMap(),
    val worldData: Map<String, String> = emptyMap(),
    val customData: Map<String, String> = emptyMap(),
)

object SaveSystem {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    fun save(
        slotName: String,
        data: GameSaveData,
    ): Boolean =
        try {
            val jsonString = json.encodeToString(data)
            localStorage["arachne_save_$slotName"] = jsonString
            true
        } catch (e: Exception) {
            console.error("Failed to save game", e)
            false
        }

    fun load(slotName: String): GameSaveData? {
        return try {
            val jsonString = localStorage["arachne_save_$slotName"] ?: return null
            json.decodeFromString<GameSaveData>(jsonString)
        } catch (e: Exception) {
            console.error("Failed to load game", e)
            null
        }
    }

    fun deleteSave(slotName: String) {
        localStorage.removeItem("arachne_save_$slotName")
    }

    fun hasSave(slotName: String) = localStorage["arachne_save_$slotName"] != null

    fun listSaves(): List<String> {
        val saves = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i) ?: continue
            if (key.startsWith("arachne_save_")) {
                saves.add(key.removePrefix("arachne_save_"))
            }
        }
        return saves
    }

    fun saveToSlot(
        slot: Int,
        playerData: Map<String, String> = emptyMap(),
        worldData: Map<String, String> = emptyMap(),
        customData: Map<String, String> = emptyMap(),
    ): Boolean {
        val saveData =
            GameSaveData(
                timestamp =
                    kotlinx.browser.window.performance
                        .now()
                        .toLong(),
                playerData = playerData,
                worldData = worldData,
                customData = customData,
            )
        return save("slot$slot", saveData)
    }

    fun loadFromSlot(slot: Int) = load("slot$slot")

    fun quickSave(
        playerData: Map<String, String> = emptyMap(),
        worldData: Map<String, String> = emptyMap(),
        customData: Map<String, String> = emptyMap(),
    ) = saveToSlot(0, playerData, worldData, customData)

    fun quickLoad() = loadFromSlot(0)
}

class SaveBuilder {
    private val playerData = mutableMapOf<String, String>()
    private val worldData = mutableMapOf<String, String>()
    private val customData = mutableMapOf<String, String>()

    fun addPlayerData(
        key: String,
        value: String,
    ) = apply { playerData[key] = value }

    fun addPlayerData(
        key: String,
        value: Any,
    ) = apply { playerData[key] = value.toString() }

    fun addWorldData(
        key: String,
        value: String,
    ) = apply { worldData[key] = value }

    fun addWorldData(
        key: String,
        value: Any,
    ) = apply { worldData[key] = value.toString() }

    fun addCustomData(
        key: String,
        value: String,
    ) = apply { customData[key] = value }

    fun addCustomData(
        key: String,
        value: Any,
    ) = apply { customData[key] = value.toString() }

    fun save(slotName: String): Boolean {
        val saveData =
            GameSaveData(
                timestamp =
                    kotlinx.browser.window.performance
                        .now()
                        .toLong(),
                playerData = playerData,
                worldData = worldData,
                customData = customData,
            )
        return SaveSystem.save(slotName, saveData)
    }

    fun saveToSlot(slot: Int) = save("slot$slot")
}

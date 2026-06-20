package at.flauschigesalex.animated_holo.core

import at.flauschigesalex.animated_holo.core.holo.asTextDisplay
import at.flauschigesalex.animated_holo.core.utils.scheduleAsync
import at.flauschigesalex.animated_holo.lib.data.HologramConfiguration
import at.flauschigesalex.lib.base.file.FileManager
import at.flauschigesalex.lib.base.file.json.JsonManager
import at.flauschigesalex.lib.base.file.ResourceManager
import at.flauschigesalex.lib.base.file.json.readJson
import kotlinx.serialization.json.Json

internal object Configuration {
    internal const val VERSION = 1

    private val file: FileManager = FileManager(AnimatedHoloPlugin.instance.dataFolder, "config.json")
    private var json: JsonManager

    init {
        this.attemptCreateConfig()
        this.json = file.readJson() ?: JsonManager()
    }
    
    internal fun reload() {
        this.json = file.readJson() ?: this.json
    }

    @Deprecated("Internal")
    internal val configVersion: Int = json.getInt("_version") ?: 1
    
    var dependencyMissingNoWarn: Set<String>
        get() = json.getStringList("warn.suppress.missing.dependencies").toSet()
        set(value) {
            json.put("warn.suppress.missing.dependencies", value.toList())
            this.saveConfig(true)
        }
    
    var holograms: Set<HologramConfiguration>
        get() = json.getJsonList("holograms").map {
            Json.decodeFromString<HologramConfiguration>(it.toString())
        }.toMutableSet()
        set(value) {
            json.put("holograms", value.map { Json.encodeToString(it) })
            this.saveConfig(true)
        }
    
    fun updateHolo(holo: HologramConfiguration) {
        holograms -= holo
        holograms += holo
        
        holo.asTextDisplay(true)
    }
    
    // SAVE CONFIG
    
    fun saveConfig(saveAsync: Boolean) {
        if (json.isOriginalContent()) return

        if (saveAsync) {
            scheduleAsync {
                this@Configuration.saveConfig(false)
            }
            return
        }
            
        file.createFile()
        file.write(json)
    }

    private fun attemptCreateConfig() {
        if (file.file.isDirectory)
            file.delete()

        json = JsonManager()
        
        if (file.exists) return
        file.createFile()
        
        ResourceManager("default-config.json")?.let { default ->
            json = default.readJson() ?: return@let null
            file.write(json)
            return@let json
        } ?: JsonManager()

        json.putIfAbsent("_version", VERSION)
        file.write(json)
    }
}
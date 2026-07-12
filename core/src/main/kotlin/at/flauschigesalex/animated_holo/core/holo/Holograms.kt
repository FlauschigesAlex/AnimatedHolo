package at.flauschigesalex.animated_holo.core.holo

import at.flauschigesalex.animated_holo.core.Configuration
import at.flauschigesalex.animated_holo.core.holo.Holograms.entities
import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.position.toLocation
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import kotlin.collections.joinToString

object Holograms : Iterable<HologramConfiguration> {
    val entities = mutableMapOf<HologramConfiguration, TextDisplay>()
    override fun iterator(): Iterator<HologramConfiguration> = entities.keys.iterator()
    
    init {
        Configuration.holograms.forEach(::updateHologram)
    }
    
    fun updateHologram(holo: HologramConfiguration) {
        holo.asTextDisplay()
    }
}

internal val HologramConfiguration.richText: String
    get() = this.richLines.joinToString("<newline><reset>").validate(this)

val HologramConfiguration.textDisplayOrNull: TextDisplay?
    get() = entities[this]

fun HologramConfiguration.asTextDisplay(forceTerminate: Boolean = false): TextDisplay {
    var entity = entities[this]
    
    if (forceTerminate || entity?.isDead != false) {
        entity?.remove()
        entities -= this
    }
    
    entity = entities[this]
    entity?.let { return it }

    val location = position.toLocation()

    val textDisplay = location.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay
    
    textDisplay.text(MiniMessage.miniMessage().deserialize(richText))
    textDisplay.billboard = billboard
    textDisplay.backgroundColor = backgroundColor
    
    textDisplay.alignment = textAlign
    textDisplay.isShadowed = textShadow
    
    textDisplay.interpolationDuration = 1
    
    textDisplay.isPersistent = false
    textDisplay.viewRange = if (visible.not()) 0f else (visibilityRange / 70.0).toFloat()
    
    textDisplay.transformation = Transformation(
        Vector3f(),
        AxisAngle4f(),
        Vector3f(scale),
        AxisAngle4f()
    )
    
    entities[this] = textDisplay
    return textDisplay
}

fun HologramConfiguration.remove() {
    entities[this]?.remove()
    entities -= this
}

fun String.validate(config: HologramConfiguration) = this.replace("\n", "<newline>").let { 
    if (it.isBlank() || MiniMessage.miniMessage().escapeTags(it).isBlank())
        return@let config.id
    else return@let it
}
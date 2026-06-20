package at.flauschigesalex.animated_holo.core.holo

import at.flauschigesalex.animated_holo.core.Configuration
import at.flauschigesalex.animated_holo.core.holo.Holograms.entities
import at.flauschigesalex.animated_holo.lib.data.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.data.position.toLocation
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

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
    textDisplay.text(MiniMessage.miniMessage().deserialize(richLines.joinToString("<newline><reset>").validate()))
    textDisplay.billboard = billboard
    textDisplay.backgroundColor = backgroundColor
    
    textDisplay.alignment = textAlign
    textDisplay.isShadowed = textShadow
    
    textDisplay.isPersistent = false
    textDisplay.viewRange = (visibilityRange / 64.0).toFloat()
    
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

fun String.validate() = this.replace("\n", "<newline>")
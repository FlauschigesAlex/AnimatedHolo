package at.flauschigesalex.animated_holo.lib.data

import at.flauschigesalex.animated_holo.lib.data.attributes.HologramAttribute
import at.flauschigesalex.animated_holo.lib.data.position.Position
import at.flauschigesalex.animated_holo.lib.utils.BukkitColor
import kotlinx.serialization.Serializable
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

@Serializable
data class HologramConfiguration(
    val id: String,
    var position: Position,
    val richLines: MutableList<String> = mutableListOf(id),

    private val attributes: MutableSet<HologramAttribute> = HologramAttribute.DEFAULT_ATTRIBUTES.toMutableSet(),

    var scale: Float = 1f,
    var visibilityRange: Double = 30.0,

    var billboard: Display.Billboard = Display.Billboard.CENTER,
    var backgroundColor: BukkitColor? = null,

    var textAlign: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
    var textShadow: Boolean = true
) {
    companion object;
    
    fun <HA: HologramAttribute> getAttribute(clazz: Class<HA>): HA? = attributes.filterIsInstance(clazz).firstOrNull()
    
    fun <HA: HologramAttribute> setAttribute(attribute: HA) {
        attributes.removeIf { it::class.java == attribute::class.java }
        attributes.add(attribute)
    }
    
    override fun equals(other: Any?): Boolean = other is HologramConfiguration && other.id == id
    override fun hashCode(): Int = id.hashCode()
}
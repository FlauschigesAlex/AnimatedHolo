package at.flauschigesalex.animated_holo.lib.holo

import at.flauschigesalex.animated_holo.lib.holo.animation.HologramAnimation
import at.flauschigesalex.animated_holo.lib.holo.attributes.HologramAttribute
import at.flauschigesalex.animated_holo.lib.holo.position.Position
import at.flauschigesalex.animated_holo.lib.utils.BukkitColor
import kotlinx.serialization.Serializable
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

@Serializable
data class HologramConfiguration(
    val id: String,
    var position: Position,
    val richLines: MutableList<String> = mutableListOf(id),

    var animation: HologramAnimation<*>? = HologramAnimation.defaultAnimation,
    private val attributes: MutableSet<HologramAttribute> = HologramAttribute.DEFAULT_ATTRIBUTES.toMutableSet(),

    var scale: Float = 1f,
    var visibilityRange: Double = 50.0,

    var billboard: Display.Billboard = Display.Billboard.CENTER,
    var backgroundColor: BukkitColor? = null,

    var textAlign: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
    var textShadow: Boolean = true
) {
    companion object;
    
    fun <HA: HologramAttribute> getAttribute(clazz: Class<HA>): HA? = attributes.filterIsInstance(clazz).firstOrNull()

    fun <HA: HologramAttribute> removeAttribute(attribute: HA) = this.removeAttribute(attribute::class.java)
    fun <HA: HologramAttribute> removeAttribute(attributeClass: Class<HA>) {
        attributes.removeIf { it::class.java == attributeClass }
    }
    
    fun <HA: HologramAttribute> setAttribute(attribute: HA) {
        this.removeAttribute(attribute)
        attributes.add(attribute)
    }
    
    override fun equals(other: Any?): Boolean = other is HologramConfiguration && other.id == id
    override fun hashCode(): Int = id.hashCode()
}
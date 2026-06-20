package at.flauschigesalex.animated_holo.lib.holo.animation.raytrace

import at.flauschigesalex.animated_holo.lib.data.HologramConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

@Suppress("unused")
/**
 * Called when a player clicks on a hovered hologram.
 */
class RayTraceClickEvent(player: Player, val clicked: HologramConfiguration) : PlayerEvent(player) {
    companion object {
        @JvmField
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
}
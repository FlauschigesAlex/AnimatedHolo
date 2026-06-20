package at.flauschigesalex.animated_holo.core.holo.animation.raytrace

import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.lib.data.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.animation.raytrace.RayTraceChangeEvent
import at.flauschigesalex.animated_holo.lib.holo.animation.raytrace.RayTraceClickEvent
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

@Suppress("unused")
internal object RayTrace : PaperListener() {
    
    internal val tracing = mutableMapOf<UUID, HologramConfiguration>()

    @EventHandler
    private fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        
        val result = player.world.rayTraceEntities(player.eyeLocation, player.eyeLocation.direction, 100.0, 1.0) {
            it is TextDisplay && Holograms.entities.values.contains(it)
        }
        val entity = result?.hitEntity
        val holo = Holograms.entities.toList().find { it.second == entity }?.first

        val previous = tracing[player.uniqueId]
        if (previous == holo) return

        if (holo == null) tracing.remove(player.uniqueId)
        else tracing[player.uniqueId] = holo
        
        RayTraceChangeEvent(player, previous, holo).callEvent()
    }
    
    @EventHandler
    private fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (event.action.isLeftClick.not() && event.action.isRightClick.not()) return
        
        val traced = player.tracedHologram() ?: return
        
        RayTraceClickEvent(player, traced).callEvent()
    }
}

fun Player.tracedHologram(): HologramConfiguration? = RayTrace.tracing[this.uniqueId]
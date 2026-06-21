package at.flauschigesalex.animated_holo.core.holo.raytrace

import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.core.holo.isHoloDebug
import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.attributes.HoverOffsetAttribute
import at.flauschigesalex.animated_holo.lib.holo.attributes.HoverInteractionRangeMultiplierAttribute
import at.flauschigesalex.animated_holo.lib.holo.position.toLocation
import at.flauschigesalex.animated_holo.lib.holo.raytrace._events.RayTraceChangeEvent
import at.flauschigesalex.animated_holo.lib.holo.raytrace._events.RayTraceClickEvent
import at.flauschigesalex.animated_holo.lib.holo.attributes.HoverMaxInteractionRangeAttribute
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*
import kotlin.math.min

@Suppress("unused")
internal object RayTrace : PaperListener() {
    
    internal val tracing = mutableMapOf<UUID, HologramConfiguration>()

    @EventHandler
    private fun onMove(event: PlayerMoveEvent) {
        val player = event.player

        val lineOffset = .1f
        
        val nearbyRangeDistanceBase = 1.1
        val nearbyRangeDistanceMultiplier = .02
        
        val maxRayTraceDistance = 100
        val spacingMultiplierBase = 1.4
        var spacingMultiplier = spacingMultiplierBase

        val startLoc = player.eyeLocation
        while (true) {
            val t = startLoc.toVector().add(startLoc.direction.multiply(spacingMultiplier))
            val loc = t.toLocation(player.world)
            if (loc.distance(startLoc) > maxRayTraceDistance) break

            spacingMultiplier += spacingMultiplierBase
            
            if (player.isHoloDebug) loc.world.spawnParticle(Particle.CRIT, loc, 1, 0.0, 0.0, 0.0, 0.0, null)
            
            val range = nearbyRangeDistanceBase + (startLoc.distance(loc) * nearbyRangeDistanceMultiplier)
            val holo = Holograms.filter { config ->
                var offset = config.getAttribute(HoverOffsetAttribute::class.java)?.value ?: 0f
                offset += (lineOffset * config.richLines.size)
                
                val position = config.position.toLocation().add(0.0, offset.toDouble(), 0.0)
                
                val maxRange = config.getAttribute(HoverMaxInteractionRangeAttribute::class.java)?.value ?: Int.MAX_VALUE
                if (position.distance(startLoc) > min(config.visibilityRange, maxRange.toDouble()))
                    return@filter false
                
                val effectiveRange = range * (config.getAttribute(HoverInteractionRangeMultiplierAttribute::class.java)?.value ?: 1f)
                
                return@filter effectiveRange.toFloat() >= position.distance(loc)
            }.minByOrNull { it.position.toLocation().distance(loc) }
            
            if (holo == null) continue
            
            if (player.isHoloDebug) player.sendActionBar(Component.text(holo.id))
            
            val previous = tracing.put(player.uniqueId, holo)
            if (previous == holo) return
            
            RayTraceChangeEvent(player, previous, holo).callEvent()
            return
        }

        if (player.isHoloDebug) player.sendActionBar(Component.text("null"))

        val previous = tracing.remove(player.uniqueId) ?: return
        RayTraceChangeEvent(player, previous, null).callEvent()
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
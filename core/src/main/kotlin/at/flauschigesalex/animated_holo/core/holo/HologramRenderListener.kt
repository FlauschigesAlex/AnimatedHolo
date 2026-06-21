package at.flauschigesalex.animated_holo.core.holo

import at.flauschigesalex.animated_holo.lib.holo.position.toLocation
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent

@Suppress("unused")
private class HologramRenderListener : PaperListener() {
    
    @EventHandler
    private fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        if (event.hasChangedBlock().not()) return

        player.updateNearbyHolograms()
    }
    
    @EventHandler
    private fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        if (event.hasChangedBlock().not()) return

        player.updateNearbyHolograms()
    }
    
    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        player.updateNearbyHolograms()
    }
    
    @EventHandler
    private fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player

        player.updateNearbyHolograms()
    }
    
    fun Player.updateNearbyHolograms() {
        Holograms.filter { it.position.name == this.world.name }.filter {
            it.position.toLocation().distance(this.location) < it.visibilityRange
        }.forEach(Holograms::updateHologram)
    }
}
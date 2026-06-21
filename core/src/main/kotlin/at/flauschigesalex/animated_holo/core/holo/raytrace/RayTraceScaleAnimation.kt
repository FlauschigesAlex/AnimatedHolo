package at.flauschigesalex.animated_holo.core.holo.raytrace

import at.flauschigesalex.animated_holo.core.external_api.Dependency
import at.flauschigesalex.animated_holo.core.external_api.DependencyCheck
import at.flauschigesalex.animated_holo.core.holo.asTextDisplay
import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.attributes.HoverScaleAttribute
import at.flauschigesalex.animated_holo.lib.holo.attributes.HoverTransitionAttribute
import at.flauschigesalex.animated_holo.lib.holo.animation.raytrace.RayTraceChangeEvent
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.joml.Vector3f
import java.lang.reflect.Type

@Suppress("unused")
private class HologramAnimationListener : PaperListener() {

    @EventHandler
    private fun onRayTrace(event: RayTraceChangeEvent) {
        val player = event.player

        DependencyCheck.require(Dependency.PROTOCOL_LIB) {
            event.previous?.also {
                val transition = it.getAttribute(HoverTransitionAttribute::class.java)?.value?.toInt() ?: 0
                player.spoofScale(it, it.scale, transition)
            }
            event.current?.also {
                val multiplier = it.getAttribute(HoverScaleAttribute::class.java)?.value ?: return@require
                val transition = it.getAttribute(HoverTransitionAttribute::class.java)?.value?.toInt() ?: 0
                player.spoofScale(it, it.scale * multiplier, transition)
            }
        }
    }
    
    private fun Player.spoofScale(holo: HologramConfiguration, scale: Float, delayTicks: Int = 0) {
        val entity = holo.asTextDisplay()
        
        val protocolManager = ProtocolLibrary.getProtocolManager()
        val watcher = WrappedDataWatcher.getEntityWatcher(entity)

        val scaleSerializer = watcher
            .toDataValueCollection()
            .firstOrNull { it.index == 12 }
            ?.serializer!!

        val intSerializer = WrappedDataWatcher.Registry.get(
            Int::class.javaObjectType as Type
        )

        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA)

        packet.integers.write(0, entity.entityId)
        packet.dataValueCollectionModifier.write(0, listOf(
                WrappedDataValue(8, intSerializer, 0),
                WrappedDataValue(9, intSerializer, delayTicks),
                WrappedDataValue(12, scaleSerializer, Vector3f(scale, scale, scale))
        ))

        protocolManager.sendServerPacket(player, packet)
    }
    
}
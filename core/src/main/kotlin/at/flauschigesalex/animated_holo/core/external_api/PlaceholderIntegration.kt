package at.flauschigesalex.animated_holo.core.external_api

import at.flauschigesalex.animated_holo.core.AnimatedHoloPlugin
import at.flauschigesalex.animated_holo.core.holo.Holograms
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import org.bukkit.entity.TextDisplay

import at.flauschigesalex.animated_holo.core.external_api.Dependency.*
import at.flauschigesalex.animated_holo.core.holo.richText

class PlaceholderIntegration private constructor(plugin: AnimatedHoloPlugin) {
    
    companion object {
        fun enable(plugin: AnimatedHoloPlugin) = DependencyCheck.require(PROTOCOL_LIB, PLACEHOLDER_API) { PlaceholderIntegration(plugin) }
    }
    
    init {
        val packetManager = ProtocolLibrary.getProtocolManager()
        
        packetManager.addPacketListener(object : PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)
                val entity = player.world.entities.find { it.entityId == entityId } ?: return
                
                val holo = Holograms.entities.toList().find { it.second == entity }?.first ?: return
                if (entity !is TextDisplay) return
                
                val text = holo.richText
                val component = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, text))
                val serialized = JSONComponentSerializer.json().serialize(component)

                val data = packet.dataValueCollectionModifier.readSafely(0)?.toMutableList() ?: return
                val position = data.indexOfFirst { it.value is WrappedChatComponent }
                if (position == -1) return
                
                val metadata = data[position]
                
                data[position] = WrappedDataValue.fromWrappedValue(
                    metadata.index,
                    WrappedDataWatcher.Registry.getChatComponentSerializer(),
                    WrappedChatComponent.fromJson(serialized)
                )
                
                packet.dataValueCollectionModifier.write(0, data)
            }
        })
    }
}
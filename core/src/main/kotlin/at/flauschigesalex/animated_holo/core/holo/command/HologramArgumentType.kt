package at.flauschigesalex.animated_holo.core.holo.command

import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.lib.data.HologramConfiguration
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

internal object HologramArgumentType : CommandArgumentType<HologramConfiguration>() {
    override fun suggestType(value: String, sender: Audience): Boolean = Holograms.find { it.id.startsWith(value) } != null
    override suspend fun parse(value: String, sender: Audience): HologramConfiguration? = Holograms.find { it.id.equals(value, true) }
    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = Holograms.map { it.id }.filter { it.startsWith(provided, true) }
}
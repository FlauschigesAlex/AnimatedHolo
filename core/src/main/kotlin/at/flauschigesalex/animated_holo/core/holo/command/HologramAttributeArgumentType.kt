package at.flauschigesalex.animated_holo.core.holo.command

import at.flauschigesalex.animated_holo.lib.holo.attributes.HologramAttribute
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

internal object HologramAttributeArgumentType : CommandArgumentType<Class<out HologramAttribute>>() {
    override fun suggestType(value: String, sender: Audience): Boolean = HologramAttribute.entries.any { it.simpleName.startsWith(value, true) }
    override suspend fun parse(value: String, sender: Audience): Class<out HologramAttribute>? = HologramAttribute.entries.filter { it.simpleName.startsWith(value, true) }.takeIf { it.size == 1 }?.first()
    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = HologramAttribute.entries.map { it.simpleName }.filter { it.startsWith(provided, true) }
}
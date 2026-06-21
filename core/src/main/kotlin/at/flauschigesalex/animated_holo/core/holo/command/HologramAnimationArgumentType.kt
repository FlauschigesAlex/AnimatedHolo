package at.flauschigesalex.animated_holo.core.holo.command

import at.flauschigesalex.animated_holo.lib.holo.animation.HologramAnimation
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

internal object HologramAnimationArgumentType : CommandArgumentType<HologramAnimation<*>>() {
    override fun suggestType(value: String, sender: Audience): Boolean =
        HologramAnimation.entries.any { it.javaClass.simpleName.startsWith(value, true) }
    
    override suspend fun parse(value: String, sender: Audience): HologramAnimation<*>? =
        HologramAnimation.entries.filter { it.javaClass.simpleName.startsWith(value, true) }.takeIf { it.size == 1 }?.first()
    
    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> =
        HologramAnimation.entries.map { it.javaClass.simpleName }.filter { it.startsWith(provided, true) }
}
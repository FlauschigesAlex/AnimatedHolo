package at.flauschigesalex.animated_holo.lib.utils

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import at.flauschigesalex.lib.minecraft.paper.base.utils.name
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color


internal object BukkitColorSerializer: KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.asARGB())
    override fun deserialize(decoder: Decoder): Color = Color.fromARGB(decoder.decodeInt())
}

typealias BukkitColor = @Serializable(BukkitColorSerializer::class) Color

object ColorArgumentType : CommandArgumentType<BukkitColor>() {
    private val hexRegexSuggest = Regex("^#?([A-Fa-f0-9]{1,8})?$")
    private val hexRegex = Regex("^#?([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{4}|[A-Fa-f0-9]{3})$")
    
    override fun suggestType(value: String, sender: Audience): Boolean {
        if (value.matches(hexRegexSuggest)) return true
        value.toIntOrNull()?.let { return true }
        return NamedTextColor.NAMES.values().any { it.name.startsWith(value, true) }
    }

    override suspend fun parse(value: String, sender: Audience): BukkitColor? {
        if (value.matches(hexRegex)) return parseHexColor(value)
        value.toIntOrNull()?.let { return BukkitColor.fromARGB(it) }
        return NamedTextColor.NAMES.values().find { it.name.equals(value, true) }?.value()?.let { BukkitColor.fromRGB(it) }
    }

    private fun parseHexColor(value: String): BukkitColor? {
        val hex = value.removePrefix("#")
        val argb = when (hex.length) {
            3 -> "FF${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}"
            4 -> "${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}"
            6 -> "FF$hex"
            8 -> hex
            else -> return null
        }

        return BukkitColor.fromARGB(argb.toLong(16).toInt())
    }
}

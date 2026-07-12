package at.flauschigesalex.animated_holo.core.utils

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

object Colors {
    val gradient = TextColor.fromHexString("#eb5834")!! to NamedTextColor.GOLD
    val highlight = gradient.first
    val error: NamedTextColor = NamedTextColor.RED
    val errorHighlight = TextColor.fromHexString("#ff2020")!!
}

fun TextColor.asRichString(): String = "<color:${this.asHexString()}>"
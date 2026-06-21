package at.flauschigesalex.animated_holo.lib.holo.attributes

import kotlinx.serialization.Serializable

@Serializable
class HoverScaleAttribute(
    override val value: Float
) : HologramAttribute {
    companion object;
}
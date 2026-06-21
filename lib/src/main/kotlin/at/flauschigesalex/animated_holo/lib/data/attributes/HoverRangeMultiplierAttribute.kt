package at.flauschigesalex.animated_holo.lib.data.attributes

import kotlinx.serialization.Serializable

@Serializable
class HoverRangeMultiplierAttribute(
    override val value: Float
) : HologramAttribute {
    companion object;
}
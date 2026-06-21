package at.flauschigesalex.animated_holo.lib.holo.attributes

import kotlinx.serialization.Serializable

@Serializable
class HoverOffsetAttribute(
    override val value: Float
) : HologramAttribute {
    companion object;
}
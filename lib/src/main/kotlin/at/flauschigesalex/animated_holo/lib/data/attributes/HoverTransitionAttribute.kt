package at.flauschigesalex.animated_holo.lib.data.attributes

import kotlinx.serialization.Serializable

@Serializable
class HoverTransitionAttribute(
    override val value: Float,
) : HologramAttribute {
    companion object;
}
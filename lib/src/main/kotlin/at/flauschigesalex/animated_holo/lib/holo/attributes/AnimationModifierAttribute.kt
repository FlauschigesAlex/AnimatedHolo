package at.flauschigesalex.animated_holo.lib.holo.attributes

import kotlinx.serialization.Serializable

@Serializable
class AnimationModifierAttribute(
    override val value: Float
) : HologramAttribute {
    companion object;
}
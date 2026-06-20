package at.flauschigesalex.animated_holo.lib.data.attributes

import kotlinx.serialization.Serializable

@Serializable
class HoverScaleAttribute(
    override val value: String
) : HologramAttribute {
    companion object;
    
    override fun isValid(): Boolean = value.toFloatOrNull() != null
}